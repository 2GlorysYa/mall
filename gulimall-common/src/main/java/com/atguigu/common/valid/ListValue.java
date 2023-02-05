package com.atguigu.common.valid;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

// 自定义校验注解必须符合JSR303规范，所以以下元注解必须打上
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME) // 注解生命周期，保留在运行期
@Documented
@Constraint(
        // 指定校验器, 自己写一个校验器继承ConstraintValidator泛型接口, 这样就建立了校验注解和校验器的关联
        validatedBy = {ListValueConstraintValidator.class}  // 可以传入多个校验器来校验不同类型字段
)
public @interface ListValue {
    // 定义注解参数和默认值
    // 默认提示信息，会去配置文件中提取出message，该配置文件写在resources里
    // 注意这就是官方写法，尽管配置文件的路径不是这个
    String message() default "{com.atguigu.common.valid.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int[] vals() default {};
}
