package com.atguigu.common.constant;

public class ProductConstant {

    // 以后业务需要用到是否是基本属性或销售属性的判断，都用这个枚举
    public enum AttrEnum {
        // 定义枚举值
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");

        // 定义枚举成员变量
        private int code;

        private String msg;

        // 定义枚举构造器
        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return this.code;
        }

        public String getMsg() {
            return this.msg;
        }

    }

    public enum StatusEnum {
        NEW_SPU(0,"新建"),
        SPU_UP(1,"商品上架"),
        SPU_DOWN(2,"商品下架");


        private int code;

        private String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        StatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }
}
