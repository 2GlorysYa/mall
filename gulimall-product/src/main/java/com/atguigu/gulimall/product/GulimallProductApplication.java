package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 整合MyBatis Plus
 *   1。导入依赖
 *          mybtais-plus-boot-starter
*    2. 配置
*           1-配置数据源
 *              导入数据库驱动
 *              在application.yml配置数据源
*           2-配置mybtatis-plus
 *              使用@MapperScan告诉mybatis dao接口在哪里
 *              告诉mybatisplus，sql映射文件的位置
 *
 *  3. 逻辑删除
 *      3。1 配置全局逻辑删除规则 （可省略）-> 在application.yml配置文件
 *      3。2 配置逻辑删除的组件bean（可省略）
 *      3。3 给实体加上逻辑删除注解@TableLogic
 *
 *
 *  4。统一的异常处理
 * @ControllerAdvice
     * 1。写一个异常处理类，打上该注解
     * 2。使用@ExceptionHandler标注方法能处理的异常
 *
 *  校验
 *  问题引入：除了前端填写form时会进行前端的校验，后端也应该进行一次校验
 *  1。给校验字段添加校验注解 javax.validation.constraints, 并定义自己的message提示
 *  2。开启校验功能 @Valid
 *  3. 给校验的bean后紧跟一个BindingResult，获取校验结果
 *  对每一个实体都添加校验太麻烦了，于是使用统一的异常处理@ControllerAdvice来处理校验后
 *  要想使用这个，就得让原先save方法抛出异常而不是收集error信息，所以去掉BindingResult
 *  4。分组校验
 *      1。给校验注解标注什么情况去校验 @NotNull(groups = {})
 *      2。给方法指定要执行的校验分组 @Validated(AddGroup.class)
 *      3。默认没有指定分组的校验注解，在分组情况下不生效，只会在@Validated生效（默认分组default）
 *  5。自定义校验
 *      1。编写一个自定义校验注解
 *      2。编写一个自定义的校验器
 *      3。关联自定义的校验器和校验注解
 *
 *  6.模版引擎
 *  1。thymeleaf-starter：关闭缓存
 *  2。静态资源都放在static文件夹下就可以按照路径直接访问
 *  3。页面放在templates下，直接访问
 *      springboot，访问项目时默认会找index，所以直接点10000端口就会跳到页面上
 *  4。页面修改不重启服务器实时更新
 *      1.引入dev-tools
 *      2.修改完页面 ctr+shift+f9 重新自动编译页面
 *
 *  5.整合springCache简化缓存开发
 *      1。引入依赖
 *      2。写配置，配置使用redis作为缓存（application.properties）
 *      3。测试使用缓存
 *          @Cahcheable 触发将数据保存到缓存
 *          @CacheEvict 触发将数据从缓存删除
 *          @CachePut 不影响方法执行而删除缓存
 *          @Caching 组合以上多个操作
 *          @CacheConfig 在类级别共享缓存的相同配置
 *          1。开启缓存功能 @EnableCaching
 *          2。使用注解
 *      4。原理
 *      自动配置了RedisCacheManager->初始化所有缓存-》每个缓存决定使用什么配置
 *      -》如果redisChacheConfiguration有就用已经有的，否则就用默认配置
 *      想改缓存配置，只需要给容器放一个RedisCacheConfiguration就行
 *      就会应用到当前redisCacheManager管理的所有缓存分区中（实际redis是没有分区的，这个分区是我们人为划分）
 *
 */
@EnableRedisHttpSession // 启用redis作为session存储
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }
}
