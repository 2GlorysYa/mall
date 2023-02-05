package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
         MemberEntity entity = new MemberEntity();
        // 设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(memberLevelEntity.getId());

        // 检查手机号 用户名是否唯一
        checkPhone(vo.getPhone());
        checkUserName(vo.getUserName());

        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());

        // 对密码盐值加密MD5
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        entity.setPassword(bCryptPasswordEncoder.encode(vo.getPassword()));
        // 其他的默认信息
        // entity.setCity("湖南 长沙");
        // entity.setCreateTime(new Date());
        // entity.setStatus(0);
        entity.setNickname(vo.getUserName());
        // entity.setBirth(new Date());
        // entity.setEmail("xxx@gmail.com");
        // entity.setGender(1);
        // entity.setJob("JAVA");

        baseMapper.insert(entity);
    }

    @Override
    public void checkPhone(String phone) throws PhoneExistException{
        MemberDao memberDao = this.baseMapper;
        Integer mobile = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        // 如果查出数据库已经存在这个手机号，则手机不唯一，抛出异常
        if (mobile > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserName(String username) throws UsernameExistException{
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    // 普通登录
    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAccount = vo.getLoginAccount();
        //以用户名或电话号登录的进行查询, 只用查询一个，this.selectOne也行
        MemberEntity entity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount));
        if (entity!=null){
            // 因为注册时已经设定为数据库存储MD5盐值加密后的密码，所以当匹配时需要调match
            // 第一个参数是页面传的明文密码，第二个是数据库保存的加密密码
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(vo.getPassword(), entity.getPassword());
            if (matches){
                entity.setPassword("");
                return entity;
            }
        }
        return null;
    }

    // 社交登录
    @Override
    public MemberEntity login(SocialUser socialUser) {
        // 登录和注册合并逻辑
        // socialUser里的id就是social_uid
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getId()));
        //1 如果之前未登陆过，则查询其社交信息进行注册（第一次登录就是要求注册）
        if (memberEntity == null) {
            // 如果下面远程查询社交用户个人信息因为网络等相关问题没有获取，则try捕获异常
            // 不应该影响set令牌等关键信息
            // 必须new出来，不然null会引发空指针异常，你无法往null里set内容
            memberEntity = new MemberEntity();
            try {

                Map<String, String> queryAccessToken = new HashMap<>();
                queryAccessToken.put("access_token", socialUser.getAccessToken());

                Map<String, String> queryHeaders = new HashMap<>();
                queryHeaders.put("'Content-Type", "application/json;charset=UTF-8");
                // //调用gitee api接口获取用户信息，https://gitee.com/api/v5/user 官方api文档
                // 这里重复了，对于gitee，拿到token的时候也能拿到社交用户信息并封装到实体
                HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get",queryHeaders , queryAccessToken);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    // 这个JSON对象什么样的数据都可以直接获取
                    JSONObject jsonObject = JSON.parseObject(json);
                    memberEntity.setSocialUid(jsonObject.getString("id"));
                    memberEntity.setNickname(jsonObject.getString("name"));
                    memberEntity.setUsername(jsonObject.getString("name"));
                    memberEntity.setEmail(jsonObject.getString("email"));
                }
            } catch (Exception e) {
                log.warn("社交登录时远程调用出错 [尝试修复]");
            }

            memberEntity.setAccessToken(socialUser.getAccessToken());
            memberEntity.setExpiresIn(socialUser.getExpiresIn());

            // 注册 -- 登录成功
            this.save(memberEntity);
            return memberEntity;

            //封装用户信息并保存
            // uid = new MemberEntity();
            // uid.setAccessToken(socialUserTo.getAccess_token());
            // uid.setSocialUid(socialUserTo.getId());
            // uid.setExpiresIn(socialUserTo.getExpires_in());
            // MemberLevelEntity defaultLevel = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
            // uid.setLevelId(defaultLevel.getId());

            // 第三方信息
            // JSONObject jsonObject = JSON.parseObject(json);
            // //获得昵称，头像
            // String name = jsonObject.getString("name");
            // String profile_image_url = jsonObject.getString("avatar_url");
            // // 这个service查询的
            // uid.setNickname(name);
            // uid.setHeader(profile_image_url);

            // this.save(uid);
        } else {

            // 如果用户已注册, 就更新令牌信息并返回这个记录
            MemberEntity update = new MemberEntity();   // 其实不用new实体，直接在查出来的实体上修改
            //2 否则更新令牌等信息并返回
            update.setAccessToken(socialUser.getAccessToken());
            update.setSocialUid(socialUser.getId());
            update.setExpiresIn(socialUser.getExpiresIn());
            this.updateById(update);
            // 返回更新的实体（只包含令牌和过期时间）
            memberEntity.setAccessToken(socialUser.getAccessToken());
            memberEntity.setExpiresIn(socialUser.getExpiresIn());
            return memberEntity;
        }
    }

}