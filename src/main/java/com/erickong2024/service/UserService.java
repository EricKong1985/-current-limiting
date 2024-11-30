package com.erickong2024.service;


import com.erickong2024.model.User;
import com.erickong2024.model.UserRequest;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    // Redis 模板，用于存取用户请求次数等信息
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisService redisService;

    // 存储用户信息，通常会从数据库或缓存中获取，简化版本直接放在内存中
    private static final Map<String, User> userDatabase = new HashMap<>();

    // 示例用户数据（通常从数据库中查询）
    static {
        userDatabase.put("user1_token", new User("1", "user1_token", 1000)); // 每分钟最多 1000 次请求
        userDatabase.put("user2_token", new User("2", "user2_token", 500));  // 每分钟最多 500 次请求
        userDatabase.put("user3_token", new User("3", "user3_token", 500));  // 每分钟最多 500 次请求
    }

    // 根据 token 获取用户信息
    public User getUserByToken(String token) {
        User user = userDatabase.get(token);
        if (user == null) {
            throw new IllegalArgumentException("User not found for token: " + token);
        }
        return user;
    }

    public boolean isRequestAllowed(UserRequest userRequest) {
        User user = getUserByToken(userRequest.getToken());
        Integer userId = Integer.valueOf(user.getId());
        String path = userRequest.getPath();
        int accessCount = redisService.getUserAccessedCount(userId, path);
        return accessCount < user.getLimit();
    }
}
