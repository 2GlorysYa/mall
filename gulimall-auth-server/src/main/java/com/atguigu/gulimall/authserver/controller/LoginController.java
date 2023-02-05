package com.atguigu.gulimall.authserver.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.ValidateCode;
import com.atguigu.gulimall.authserver.feign.MemberFeignService;
import com.atguigu.gulimall.authserver.feign.ThirdPartFeignService;
import com.atguigu.gulimall.authserver.vo.MemberResponseVo;
import com.atguigu.gulimall.authserver.vo.UserLoginVo;
import com.atguigu.gulimall.authserver.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.alibaba.fastjson.TypeReference;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    // 发送验证码
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        // TODO 1.接口防刷
        // 如果redis已经存了此验证码，首先取出，并根据存入时的系统时间判断以否已经超过了60秒
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                // 返回验证码频率太高异常
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 如果超过了60s，表明可以发送，并生成验证码， 验证码存在redis里 key: phone, value: code_系统时间
        int rCode = ValidateCode.generateValidateCode(6);
        String code = String.valueOf(rCode) + "_" + System.currentTimeMillis();
        // redis缓存验证码，并设置过期时间，防止同一phone在60秒内再次发送验证码
        // 因为只要刷新同一个页面，就又能马上点击发送验证码，所以要防刷
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
                code,
                10, // 10分钟内有效
                TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }



    // 转发不会丢失参数，因为只有一次请求，而重定向会丢失参数，因为是两次请求
    // SpringMVC的RedirectAttributes帮助传递重定向后的参数，有两个可选的方法
    // 1.addAttribute方法本质就是把参数拼接在新的url上
    // 2.addFlashAttribute方法不会将参数体现在url上，而是保存在session里
    // inspect-》application-》storage-》cookies能查看sessionID
    // 前端提交的【表单数据】可以通过RequestParam接收，也可以用@ModelAttribute声明的一个JAVA对象接收。
    // 也可以不用声明注解，会自动组装这个对象
    // 但如果前端传过来的JSON数据，那就是POST请求，数据被封装在请求体，此时必须使用@RequestBody来接收
    // SpringMVC会使用JACKSON转换器来解析JSON，具体是靠匹配JSON中的key，之后调用实体的set方法来把值set进实体属性
    @PostMapping("/register")
    public String register(@Valid UserRegistVo registerVo,
                           BindingResult result,    // 校验结果
                           RedirectAttributes attributes) {
        // if (result.hasErrors()) {
        //     // 等同于return "reg", 这不过直接返回拼串后的视图，而forward写法是重新映射到controller上
        //     return "forward:/reg.html";
        // }
        // // 重定向或转发到当前服务下，不用写完整url，auth.gulimall.com/
        // // 但这种重定向过去显示的url就是当前ip:端口，因此这里需要完整地显示域名，所以加上完整路径
        // return "redirect:/login.html";

        //1.注册首先判断校验信息是否通过
        Map<String, String> errors = new HashMap<>();
        if (result.hasErrors()){
            // 1.1 如果校验不通过，则封装校验结果
            result.getFieldErrors().forEach(item->{
                // 获取错误的属性名和错误信息
                errors.put(item.getField(), item.getDefaultMessage());
                //1.2 将错误信息封装到session中
                attributes.addFlashAttribute("errors", errors);
            });
            // 1.2 重定向到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        } else {
            // 2.若JSR303校验通过
            // 再判断验证码是否正确
            String code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
            // 2.1 如果对应手机的验证码不为空且与提交的相等-》验证码正确
            if (!StringUtils.isEmpty(code) && registerVo.getCode().equals(code.split("_")[0])) {
                // 2.1.1 验证通过，删除令牌，使得验证后的验证码失效
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
                // 2.1.2 远程调用会员服务注册
                R r = memberFeignService.register(registerVo);
                if (r.getCode() == 0) {
                    //调用成功，重定向登录页
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    //调用失败，返回注册页并显示错误信息
                    String msg = (String) r.get("msg");
                    errors.put("msg", msg);
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }else {
                //2.2 验证码错误, 转到注册页
                errors.put("code", "验证码错误");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }
    }

    // 注意这个方法一定在loginPage执行完后才会进入，未登录时总会先进loginPage
    @PostMapping("/login") // member
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session) {
        // 远程服务，登录后返回一个R，取出里面封装的返回值MemberResponseVo
        R login = memberFeignService.login(vo);

        if (login.getCode() == 0) {
            // 登录成功，返回的用户信息放到session中
            MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>(){});
            // 即当创建了一个session时，http响应就会携带一个指定的Cookie并且value是这个返回的会员对象
            // 因此下次访问浏览器会携带上这个cookie
            // 具体的set-cookie会通过Spring-session的Cookie序列化器转换为json字符串
            // 但如果是没有登录的情况下，也可以查看session，但get的对象会是空
            // 因此如果已登录，那么get的对象一定不为空
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com/";
        }else {
            // 登录失败重回登录页面，携带错误信息
            String msg = (String) login.get("msg");
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", msg);
            // 将错误原因封装到RedirectAttributes里，这样被重定向的第二个请求携带过去
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    // 对任何访问/login / index的请求均先检查是否已登录过，如果已登录，就跳转到首页
    // 方法是检查session中的loginUser属性，如果是已登录过的该属性里会被set值
    @GetMapping({"/login.html","/","/index","/index.html"}) // auth
    public String loginPage(HttpSession session){
        // 从会话从获取loginUser
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);// "loginUser";
        // 如果loginUser属性为空，说明还未登录过，跳转到登录页
        if(attribute == null){
            return "login";
        }
        System.out.println("已登陆过，重定向到首页");
        return "redirect:http://gulimall.com";
    }
}
