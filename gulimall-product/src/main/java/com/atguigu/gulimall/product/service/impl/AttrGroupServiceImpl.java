package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        // 属性分组页面中，查询框里是参数名，也就是说随便输入一个参数名aaa，都要根据这个名来查询匹配
        // 首先一定会有id，所以查询一定带id，其次查询参数名key可能匹配属性组id或属性组名，所以and再or
        // 再查看network里，发现请求header中参数携带了key：aaa, 所以后台要根据key取出
        String key = (String) params.get("key");

        // QueryWrapper 条件查询拼装器，用于拼接 where后的查询条件
        // else语句里id一定不为0，首先拼接一下id
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        // 再判断一下读进来的key是否为null, 如果不是，就把key拼接上去
        if (!StringUtils.isEmpty(key)) {
            // 知识点：为了避免并列查询时因为优先级带来的错误查询结果，or(),and()提供lambda写法, 传入一个consumer
            // A and B or C 不等于 A and (B or C)
            // .eq之间默认使用【and】连接
            // like是双百分号通配符%%, 也有左右单独的百分号likeL likeR
            // 以下语句就是 // select * from pms_attr_group where catelog_id = ?
            // and (attr_group_id=key or attr_group_name like %key%)
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        // 自定义的Query对象可以接收前端传过来的参数map，并把里面的参数解析出来封装到MP的IPage对象里
        if (catelogId == 0) {
            // this.page()内部调用baseMapper.selectPage()进行查询，并返回查询结果封装在IPage
            // 第一个参数为IPage，第二个参数为查询条件
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    new QueryWrapper<AttrGroupEntity>());
            // PageUtils会把IPage里的 列表数据 总记录数 每页记录数 当前页数这4个信息取出
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);


            // 构造IPage，传入自定义的wrapper
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        }
    }

    // 根据分类Id查出所有的分组以及这些分组里面的属性
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrByCatelogId(Long catelogId) {
        // 1.查出分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));

        // 2.查出所有属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(group->{
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, attrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrsVo.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        //1、查出当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
        List<SpuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        return vos;
    }
}