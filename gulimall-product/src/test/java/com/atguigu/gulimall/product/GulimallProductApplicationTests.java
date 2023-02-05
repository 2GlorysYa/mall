package com.atguigu.gulimall.product;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    // @Autowired
    // private OSSClient ossClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test2() {
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrBySpuId(3L);
        System.out.println(saleAttrsBySpuId);
    }

    @Test
    public void test() {
        List<SpuItemAttrGroupVo>attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(3L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }



    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> ops =stringRedisTemplate.opsForValue();
        // 保存，给一个uuid
        ops.set("hello", "world" + UUID.randomUUID().toString());
        // 查询
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径:{}", Arrays.asList(catelogPath));
    }

    // @Test
    // public void testUpload() throws FileNotFoundException {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        // String endpoint = "oss-cn-shanghai.aliyuncs.com";
// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        阿里云ram（资源访问空值）账户 通过这组id和密钥登录
//         String accessKeyId = "LTAI5tAH1JYtoSLbUkQJGWtc";
        // String accessKeySecret = "RHNY3xHtK6jEPpWepuwifOIHTNyIgs";

// 创建OSSClient实例。
//         OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
//         InputStream inputStream = new FileInputStream("/Users/isshin/Desktop/Screen Shot 2021-12-28 at 15.14.00.png");
// 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
//         ossClient.putObject("gulimall-yibo", "pic1.jpg", inputStream);

// 关闭OSSClient。
//         ossClient.shutdown();
//         System.out.println("上传完成 ");
//     }

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        // brandEntity.setName("华为");
        // brandService.save(brandEntity);
        // System.out.println("保存成功。。");
        // brandEntity.setBrandId(1L);
        // brandEntity.setDescript("华为");
        // brandService.updateById(brandEntity);

        // queryWapper是查询条件，泛型是brandEntity，eq表示查询id = 1
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        list.forEach((item)-> {
            System.out.println(item);
        });
    }

}
