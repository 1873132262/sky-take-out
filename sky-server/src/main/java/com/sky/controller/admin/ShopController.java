package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置店铺营业状态
     *
     * @param status 0打烊，1营业
     * @return 操作结果
     */
    @PutMapping("/{status}")
    public Result<Void> setStatus(@PathVariable Integer status) {
        if (!StatusConstant.ENABLE.equals(status) && !StatusConstant.DISABLE.equals(status)) {
            return Result.error("店铺状态只能是0或1");
        }
        log.info("设置店铺营业状态为：{}", StatusConstant.ENABLE.equals(status) ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS_KEY, status);
        return Result.success();
    }
}
