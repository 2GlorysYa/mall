package com.atguigu.gulimall.ware.config;


import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan("com.atguigu.gulimall.ware.dao")
@Configuration
public class WareMyBatisConfig {

    // 引入分页插件, 解决页面显示页数不正确问题
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后的操作，默认false继续请求，true调回首页
        paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，默认500条，-1不受限制
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
