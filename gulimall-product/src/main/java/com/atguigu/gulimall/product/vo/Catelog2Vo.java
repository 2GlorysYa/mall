package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 2级分类vo
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {

    private String catelog1Id;  // 1级父分类ID

    private List<Catelog3Vo> catalog3List;  // 三级子分类

    private String id;

    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo{

        private String catelog2Id; // 父分类，2级分类id

        private String id;

        private String name;

    }
}
