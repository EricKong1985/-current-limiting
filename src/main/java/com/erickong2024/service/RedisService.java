package com.erickong2024.service;


import java.util.Date;
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
        // 计算过期时间：mins + 1分钟（即 + 1）
        long expirationTime = (mins + 1) * 60 * 1000;  // 转换为毫秒，并加1分钟
        // 获取当前时间戳
        long currentTime = System.currentTimeMillis(); // 当前时间戳（毫秒）
        // 计算设置的过期时间：需要1分钟之后的实际时间
        long expireAt = expirationTime;  // 目标过期时间，mins之后的1分钟
        // 设置该键的过期时间
        redisTemplate.expireAt(key, new Date(expireAt));
    }
}