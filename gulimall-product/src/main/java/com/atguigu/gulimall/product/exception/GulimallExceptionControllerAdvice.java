package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


// 集中处理所有异常

@Slf4j  // 日志记录
// @RestController
// @ResponseBody 设定返回值为JSON
// @controlleradvice注解给所有controller增强，添加统一配置
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    // 统一的异常处理可以处理各种异常，此方法处理校验异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题{}, 异常类型：{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult(); // 拿到校验结果
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }));
        // 最后给前端返回一个校验错误提示JSON, 错误信息都被封装到BizCodeEnume这个枚举类里
        // 先放一个400信息，再put真正的校验错误信息（字段 + message）
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(),
                BizCodeEnume.VAILD_EXCEPTION.getMsg()).put("data", errorMap);
    }

    // 此方法处理其他所有异常 Throwable
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("错误", throwable);
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),
                BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
