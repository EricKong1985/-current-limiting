package com.erickong2024.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class StringUtils {
    // 获取自1970年以来的分钟数
    public static String getMinutesSince1970() {
        // 获取当前时间，截断到分钟，并转换为自1970年以来的毫秒数，然后除以60000得到分钟数
        long minutesSinceEpoch = Instant.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilli() / 60000;
        // 将分钟数转换为字符串并返回
        return String.valueOf(minutesSinceEpoch);
    }

    // 构建Redis键的方法
    public static String buildRedisKey(int userId, String path, long mins) {
        return "user:" + userId + ":requests:" + ":path:" + path + ":mins:" + mins;
    }
}
