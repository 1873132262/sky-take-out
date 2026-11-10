package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null || !StringUtils.hasText(userLoginDTO.getCode())) {
            log.error("微信登录凭证code为空");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        String openid = getOpenid(userLoginDTO.getCode());
        User user = userMapper.getByOpenid(openid);

        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 调用微信接口服务，获取微信用户的openid
     * @param code
     * @return
     */
    String getOpenid(String code) {
        if (!StringUtils.hasText(weChatProperties.getAppid())
                || !StringUtils.hasText(weChatProperties.getSecret())) {
            log.error("微信登录配置缺失，请配置WECHAT_APPID和WECHAT_SECRET");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("grant_type", "authorization_code");
        map.put("js_code", code);
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        return parseOpenid(json);
    }

    String parseOpenid(String json) {
        if (!StringUtils.hasText(json)) {
            log.error("微信登录接口响应为空");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(json);
        } catch (Exception e) {
            log.error("解析微信登录响应失败", e);
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        String openid = jsonObject == null ? null : jsonObject.getString("openid");
        if (!StringUtils.hasText(openid)) {
            Integer errcode = jsonObject == null ? null : jsonObject.getInteger("errcode");
            String errmsg = jsonObject == null ? null : jsonObject.getString("errmsg");
            log.error("微信登录失败，errcode：{}，errmsg：{}", errcode, errmsg);
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        return openid;
    }
}
