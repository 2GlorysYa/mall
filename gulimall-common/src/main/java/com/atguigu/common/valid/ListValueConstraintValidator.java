package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义校验器，在@ListValue注解中，根据@Constraint注解里的validatedBy参数，所填应该是一个ConstraintValidator
 * 所以就自定义一个类实现ConstraintValidator接口
 * 并且泛型的第一个参数是指定的注解，第二个参数是要检验的字段类型
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> set = new HashSet<>();

    // 初始化方法, 应该先获取打在@ListValue里的值，比如@ListValue(vals = {0,1})
    // 就是字段必须是0或者1
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }

    }

    /**
     * 判断是否校验成功
     * @param value 需要校验的字段的值
     * @param constraintValidatorContext 校验上下文
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        // 判断这个值是否在set里
        return set.contains(value);
    }
}
