package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 会员
 *
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 23:07:17
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/register") // member
    public R register(@RequestBody MemberRegisterVo registerVo) {
        try {
            memberService.register(registerVo);
            // 异常机制：通过捕获对应的自定义异常判断出现何种错误并封装错误信息
        } catch (UsernameExistException userException) { //用户已存在
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException phoneException) {   // 手机已经注册
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    // 社交登录
    @PostMapping("/oauth2/login") // member
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception{
        MemberEntity entity = memberService.login(socialUser);
        if (entity != null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }

    // 普通登录
    // 远程服务都是http+json，所以需要@RequestBody
    // auth服务的loginController拿到前端的数据封装到Vo，再通过feign进行http调用并把数据封装到json传给这个方法
    @PostMapping("/login") // member
    public R login(@RequestBody MemberLoginVo loginVo) {
        MemberEntity entity = memberService.login(loginVo);
        if (entity != null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }



    // 测试一下feign
    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member", memberEntity).put("coupons", membercoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
        public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
