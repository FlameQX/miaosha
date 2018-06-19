package com.example.miaosha_1.service;

import com.example.miaosha_1.dao.MiaoshaUserDao;
import com.example.miaosha_1.entity.MiaoshaUser;
import com.example.miaosha_1.exception.GlobalException;
import com.example.miaosha_1.redis.MiaoshaUserKey;
import com.example.miaosha_1.redis.RedisService;
import com.example.miaosha_1.redis.UserKey;
import com.example.miaosha_1.result.CodeMsg;
import com.example.miaosha_1.util.MD5Util;
import com.example.miaosha_1.util.UUIDUtil;
import com.example.miaosha_1.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public static final String COOKI_NAME_TOKEN = "token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;
    @Autowired
    RedisService redisService;

    public MiaoshaUser getById(long id) {
        return miaoshaUserDao.getById(id);
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo){
        if (loginVo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String fromPass = loginVo.getPassword();
        String mobile = loginVo.getMobile();
        //判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if( user ==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String daPass = user.getPassword();
        String slatDb = user.getSalt();
        if ( !MD5Util.formPassToDBPass(fromPass, slatDb).equals(daPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;

    }

    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        //延长有效期
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
