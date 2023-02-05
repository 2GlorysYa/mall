package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    // 要查哪个表，服务层就需要注入对应的Dao
    // 但因为使用了mybtais-plus，所以继承的ServiceImp1中所声明的泛型都会继承于BaseMapper<T>，并获得CRUD方法
    // ServiceImp1里声明的变量 M baseMapper 是泛型变量, 这里M就成为了CategoryDao 所以这里可以无需注入category，直接使用baseMapper也行
    // 当然手动注入也是一样的
    // @Autowired
    // CategoryDao categoryDao;
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1。查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);    // 没有查询条件就是查询所有
        // 2。组装成父子的树形结构
        // 2.1找到所有的一级分类，在表中有一个字段ParentId可以分出一级分类, 仅一级分类的parentId=0，而2，3级有各自的parentId

        List<CategoryEntity> level1Menus = entities.stream().filter((categoryEntity) -> {  // 把集合转换成流，再过滤出0级，最后组装起来
            return categoryEntity.getParentCid() == 0;
        }).map((menu -> {
            menu.setChildren(getChildrens(menu, entities)); // 对每个一级菜单递归设置他的子菜单, Entity使用了@Data注解
            return menu;
        })).sorted((menu1, menu2)->{    // 对一级菜单排序，利用数据表的sort字段，值为1-20，sorted传入一个comparator
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    //
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @CacheEvict： 失效模式, 当后台重新请求首页数据，缓存会被刷新
     * value：删除这个区的缓存，key：对应的key名
     * beforeInvocation：这个属性比较关键，他的意思是是否在执行对应方法之前删除缓存，默认 false（即执行方法之后再删除缓存）
     * 当我们遇到需要在执行方法前删除缓存，也就是不管方法执行是否成功都需要清除缓存
     * 那我们就可以把beforeInvocation的值改为true
     * 1。同时进行多种缓存操作 @Caching (evict同时清除多个缓存)
     * 2。批量删除这个分区的所有缓存 @CacheEvict（..., allEntries = true）
     * 3。存储同一类型的数据，都指定成同一个分区
     */
    // @CacheEvict(value = "category", key = "'getLevel1Catagories'") // spel表达式字符串一定要加单引号
    // @Caching(evict = {
    //         @CacheEvict(value = "category", key = "'getLevel1Catagories'"), // 单引号
    //         @CacheEvict(value= "category", key = "'getCatalogJson'")
    // })
    @CacheEvict(value="category", allEntries = true)    // 批量删除3级分类和1级分类菜单的缓存
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        // 先更新自己
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    /**
     * 1。每一个需要缓存的数据我们都来指定要放到哪个名字的缓存【相当于缓存的分区】
     * 2。当前方法的结果需要缓存，如果缓存中有，方法不用调用，如果缓存中没有，就会调用方法，最后将方法结果放入缓存
     *      也就是在后台更新了1级分类的数据后，redis里就会发现缓存已删除，之后首页再次刷新时，缓存的数据就会更新回来
     * 3。默认行为
     *      1。如果缓存有，则方法不调用
     *      2。 key默认自动生成，以缓存名字开头
     *      3。 缓存的value，默认使用jdk序列化机制，将序列化后数据存入redis（注意不是JSON而是Byte数组）
     *      4。 默认ttl时间 -1
     *
     * 自定义：
     *  1。指定生成缓存用的key，key属性指定，接收一个spel
     *  2。指定缓存的数据存活时间ttl, 在配置文件指定存活时间
     *  3。将数据序列化为JSON
     *
     *  4。 spring-Cache的不足：
     *  （1）读模式
     * 　　（a）缓存穿透：查询一个null数据。
     * 　　　　解决方案：缓存空数据，可通过spring.cache.redis.cache-null-values=true
     * 　　（b）缓存击穿：大量并发进来同时查询一个正好过期的数据。
     * 　　　　解决方案：加锁 ? 默认是无加锁的;使用sync = true来解决击穿问题，相当加本地锁，只允许一个线程进入，其他堵塞
     *              不需要加分布式锁
     * 　　（c）缓存雪崩：大量的key同时过期。
     * 　　　　解决方案：加随机时间。加上过期时间：spring.cache.redis.time-to-live=3600000
     *          因为存储数据的时间点就不是固定一起的，所以可以指定相同的过期时间，这样也不会同时过期
     * （2）写模式
     * 　　（a）读写加锁。
     * 　　（b）引入Canal，感知到MySQL的更新去更新Redis
     * 　　（c）读多写多，直接去数据库查询就行
     *   总结：对于常规数据，读多写少的，并且即时性，一致性要求不高的数据
     */
    @Cacheable(value = {"category"}, key = "#root.method.name") // 将方法名作为key名
    @Override
    public List<CategoryEntity> getLevel1Catagories() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid",0));
    }


    // key指定缓存的key名，value指定存放的区 value = cacheNames
    // 改造之前的getCatalogJson方法，获取分类的json数据不需要写先查缓存的代码，也不需要从数据库查到后写进缓存的代码
    // 该用@Cacheable注解实现相同的功能
    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        System.out.println("查询了数据库.....");
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Catagorys = getParent_cid(selectList, 0L);

        //2、封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Catagorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1、每一个的一级分类，查到这个以及分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    if(level3Catalog!=null){
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catelog3Vo category3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    // TODO 产生堆外内存溢出 outofDirectMemoryError
    // springboot2.0默认使用lettuce操作redius的客户端，他使用netty进行网络通信
    // lettuce bug导致netty堆外内存溢出，如果没有指定堆外内存，默认使用-Xmx300m，就是你自己设定在vm option里的
    // 解决方案：1。升级lettuce客户端 2。切换使用jedis
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        //给缓存中放json字符串，拿出的json字符串，还要逆转为能用的对象类型：【序列化与反序列化】

        /**
         * 1、空结果缓存：解决缓存穿透
         * 2、设置过期时间（加随机值）：解决缓存雪崩
         * 3、加锁：解决缓存击穿
         */

        //1、加入缓存逻辑，缓存中存的数据是json字符串
        //JSO跨语言、跨平台兼容
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if(StringUtils.isEmpty(catalogJson)){
            //2、缓存中没有，查询数据库
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
            System.out.println("缓存不命中....将要查询数据库....");
            return catalogJsonFromDb;
        }
        System.out.println("缓存命中....直接返回.....");
        //转为我们指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    public  Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        //1、锁的名字。锁的粒度，越细越快。
        // 如果只是缓存catalogJson这个数据，那么锁的名称就应该命名为catalogJson-lock
        // 而不要随便命名为lock，因为如果以后要给其他数据加锁也命名为lock，那并法读写的时候就很奇怪了
        // 因为两个数据命名明明是没有关系的，但一个数据的读写却要等待另一个数据的读写完成释放锁
        //锁的粒度：具体缓存的是某个数据，11-号商品：product-11-lock product-12-lock
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;
    }

    public  Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        //1、占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        // 过期时间5分钟
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock",uuid,300,TimeUnit.SECONDS);

        if(lock){
            System.out.println("获取分布式锁成功.....");
            //加锁成功....执行业务
            //2、设置过期时间,必须和加锁是同步的，原子的
            //redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDB;
            try{
                dataFromDB = getDataFromDB();
            }finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                Integer lock1 = redisTemplate.execute(new DefaultRedisScript<Integer>(script, Integer.class)
                        , Arrays.asList("lock"), uuid);
            }

            //获取值对比+对比成功删除=原子操作  lua脚本解锁
        /*String lockValue = redisTemplate.opsForValue().get("lock");
        if(uuid.equals(lockValue)){
            //删除自己的锁
            redisTemplate.delete("lock");//删除锁
        }*/

            return dataFromDB;
        }else{
            //加锁失败....重试。synchronized
            //休眠100ms重试
            System.out.println("获取分布式锁失败，等待重试.....");
            try {
                Thread.sleep(200);
            }catch (Exception e){
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();//自旋的方式
        }
    }


    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if(!StringUtils.isEmpty(catalogJson)){
            //缓存不为null直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
            return result;
        }

        System.out.println("查询了数据库.....");

        /**
         * 1、将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //1、查询所有一级分类
        List<CategoryEntity> level1Catagorys = getParent_cid(selectList, 0L);

        //2、封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Catagorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1、每一个的一级分类，查到这个以及分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    if(level3Catalog!=null){
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catelog3Vo category3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        //3、查到的数据再放入缓存，将对象转为json放到缓存中
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);

        return parent_cid;
    }



    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDB() { //1、查询所有一级分类

        // 只要是同一把锁，就能锁住需要这个锁的所有线程
        // 1。synchronized(this) :springboot所有组件在容器中都是单例的

        /**
         * 1.将数据库的多次查询变为一次，剩下的数据通过遍历得到并封装
         * 查询条件null就是select *
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Catagorys = getParent_cid(selectList, 0L);

        //2、封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Catagorys.stream().collect(
                Collectors.toMap(k -> k.getCatId().toString(), v -> {

            // 1、每一个的一级分类，查到这个以及分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList,v.getCatId());

            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList,l2.getCatId());
                    if(level3Catalog!=null){
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catelog3Vo category3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById((catelogId));
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    // 递归查找所有菜单的子菜单
    // 参数为root当前菜单，all查询出来的所有菜单（1000个）
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}