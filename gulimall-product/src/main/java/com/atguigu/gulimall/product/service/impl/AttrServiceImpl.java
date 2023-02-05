package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;



@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        // 1。保存基本数据
        // 如果使用set方法set属性就需要一个个属性分别set，太麻烦
        // 于是使用Spring提供的BeanUtils将源对象的属性全部set到目标对象，前提是两边有共同的属性
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);
        // 2。保存关联关系, 是sale属性还是base属性用common下定义的枚举类判断
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
                && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            relationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq(
                "attr_type", "base".equalsIgnoreCase(type) ?
                        ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : // 如果type是base，val就是1
                        ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode()); // 否则val是0
        if (catelogId != 0) {
            // 根据type是销售属性（0）或者基本属性（1）来判断where等式右边的内容是哪个
            queryWrapper.eq("catelog_Id", catelogId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        // 先分页，再处理分页数据
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        // 为分页查询结果的每一条结果都设置分类和分组名，转化到AttrRespVo对象，这个对象只比attrEntity多几个属性
        List<AttrRespVo> respVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            // 把attrEntity属性值都拷贝到attrRespVO
            BeanUtils.copyProperties(attrEntity,attrRespVo);

            // 设置分类和属性分组的名字
            if ("base".equalsIgnoreCase((type))) {
                AttrAttrgroupRelationEntity attrId = relationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",
                                attrEntity.getAttrId()));
                // 如果第一次没有录入信息，那么查出来会是null
                if (attrId != null && attrId.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
                    attrRespVo.setGroupName(attrEntity.getAttrName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVos);
        return pageUtils;
    }

    // 给属性获取加入缓存，其中key为第一个参数的值，也即每个attrId
    @Cacheable(value = "attr", key = "'attrinfo:'+#root.args[0]")
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVo);

        // 如果这个attrEntity的属性是基本属性，才有必要设置分组信息
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 1。设置分组信息
            AttrAttrgroupRelationEntity attrgroupRelation = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq(
                    "attr_id", attrId
            ));
            if (attrgroupRelation != null) {
                respVo.setAttrGroupId(attrgroupRelation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelation.getAttrGroupId());
                if (attrGroupEntity != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        // 2。设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        respVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            respVo.setCatelogName(categoryEntity.getName());
        }

        return respVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        // 如果这个attrEntity是基本属性，才去做修改分组的操作
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 1。修改分组关联
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId((attr.getAttrGroupId()));
            relationEntity.setAttrId((attr.getAttrId()));

            // 先用count统计下relation里有没有关联分组属性，如果>0则关联到
            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq(
                    "attr_id", attr.getAttrId()
            ));
            if (count > 0) {
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq(
                        "attr_id", attr.getAttrId()
                ));
            } else {
                relationDao.insert(relationEntity);
            }
        }
    }

    /**
     * 根据分组id查找关联的基本属性
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_group_id", attrgroupId));
        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        // 对entities集合做非空判断，否则sql查询时where in () 空会报错
        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }

        Collection<AttrEntity> attrEntities = this.listByIds(attrIds);
        return (List<AttrEntity>) attrEntities;
    }

    // 批量删除关系
    // SQL语句：delete from pms_attr_attrgroup_relation where
    // (attr_id=1 and attr_group_id=1) or (attr_id=3 and attr_group_id=2) or (...)
    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {

        List<AttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((item) ->{
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity); // 属性对拷
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(entities);
    }

    /**
     * 获取当前分组没有关联的所有属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 1。当前分组只能关联自己所属分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2。当前分组只能关联别的分组没有引用的属性
        // 2。1 所以先找到当前分类下的所有其他分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map((item)->{
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        // 2。2 再找这些分组关联的属性
        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>()
                .in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        // 2。3 最后从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId).eq("attr_type",
                        ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }

        // 还需要加入模糊查询，先判断页面传过来的key是否为空
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            // and (attr_id = key or attr_name like %key%)
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),wrapper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;

    }

    /**
     * 在指定的所有属性集合里挑出检索属性
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> selectSearchAttrsIds(List<Long> attrIds) {
        /**
         * SELECT attr_id  FROM pms_attr WHERE attr_id in (?) AND search_type=1
         */
        return baseMapper.selectSearchAttrsIds(attrIds);
    }
}