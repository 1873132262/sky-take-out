package com.sky.constant;

/**
 * Redis缓存常量
 */
public class RedisConstant {

    public static final String DISH_CATEGORY_KEY_PREFIX = "dish_";
    public static final String DISH_CATEGORY_KEY_PATTERN = DISH_CATEGORY_KEY_PREFIX + "*";
    public static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    public static final long DISH_CACHE_TTL_MINUTES = 30L;
    public static final long EMPTY_DISH_CACHE_TTL_MINUTES = 5L;

    private RedisConstant() {
    }
}
