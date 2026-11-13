package com.sky.controller.user;

import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId 分类id
     * @return 起售中的菜品及口味
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    @SuppressWarnings("unchecked")
    public Result<List<DishVO>> list(Long categoryId) {
        if (categoryId == null) {
            return Result.error("分类id不能为空");
        }

        String key = RedisConstant.DISH_CATEGORY_KEY_PREFIX + categoryId;

        // 缓存读取失败时回退到数据库，避免Redis故障影响菜品浏览
        try {
            List<DishVO> cachedList = (List<DishVO>) redisTemplate.opsForValue().get(key);
            if (cachedList != null) {
                return Result.success(cachedList);
            }
        } catch (Exception e) {
            log.warn("读取菜品缓存失败，将回退到数据库，key={}", key, e);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);

        List<DishVO> list = dishService.listWithFlavor(dish);

        // 空列表也进行短时间缓存，避免相同请求持续穿透到数据库
        long ttlMinutes = list.isEmpty()
                ? RedisConstant.EMPTY_DISH_CACHE_TTL_MINUTES
                : RedisConstant.DISH_CACHE_TTL_MINUTES;
        try {
            redisTemplate.opsForValue().set(key, list, ttlMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入菜品缓存失败，不影响本次数据库查询结果，key={}", key, e);
        }

        return Result.success(list);
    }
}
