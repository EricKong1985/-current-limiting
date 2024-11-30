package com.erickong2024.service;


import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.erickong2024.util.StringUtils;

import static com.erickong2024.util.StringUtils.buildRedisKey;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 获取用户访问次数
    public int getUserAccessedCount(int userId, String path) {
        // 使用buildRedisKey方法构建Redis键
        long mins = Long.parseLong(StringUtils.getMinutesSince1970());
        String key = buildRedisKey(userId, path, mins);
        // 从Redis中获取该键对应的值
        String countValue = redisTemplate.opsForValue().get(key);
        // 如果值为null，则返回0，否则将值转换为整数并返回
        int count = countValue == null ? 0 : Integer.parseInt(countValue);
        return count;
    }

    // 设置用户访问次数
    public void setUserAccessedCount(int userId, int count, long mins, String path) {
        // 使用buildRedisKey方法构建Redis键
        String key = buildRedisKey(userId, path, mins);
        // 将访问次数存储到Redis中
        redisTemplate.opsForValue().set(key, String.valueOf(count));
        // 设置该键的过期时间为1分钟
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);
    }
}