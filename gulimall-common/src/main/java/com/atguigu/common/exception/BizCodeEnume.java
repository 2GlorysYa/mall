package com.atguigu.common.exception;


/**
 * 错误码和信息错误定义类
 * 1 错误码定义规则为5位数字
 * 2 前两位表示业务场景，最后三位是错误码
 * 3 维护错误码后需要维护错误描述，将他们定义为枚举形式
 *
 * 错误码列表：
 * 10 通用
 *  001 参数格式校验
 *  002 短信验证码频率太高
 * 11 商品
 * 12 订单
 * 13 购物车
 * 14 物流
 * 15 用户
 */
public enum BizCodeEnume {

    // 枚举类内定义的枚举对象
    // 易错！枚举类之间必须用逗号分割！
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VAILD_EXCEPTION(10001, "参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，稍后再试"),
    USER_EXIST_EXCEPTION(15001, "用户已存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机已存在"),
    LOGINACCT_PASSWORD_EXCEPTION(15003, "账号或密码错误");


    private int code;

    private String msg;

    // 枚举类构造方法
     BizCodeEnume(int code, String msg) {
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
