package com.erickong2024.consumer;


import com.alibaba.fastjson.JSON;
import com.erickong2024.model.UserTrafficCount;
import com.erickong2024.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;


@EnableKafka
public class KafkaConsumer {

    @Autowired
    private RedisService redisService;



    //从status_topic这个Topic中获取统计结果放入Redis
    @KafkaListener(topics = "status_topic")
    public void listen(String message) {
        UserTrafficCount userTrafficCount = JSON.parseObject(message, UserTrafficCount.class);
        String userId = userTrafficCount.getUserId();
        Long count = userTrafficCount.getCount();
        String path = userTrafficCount.getPath();
        Long min = Long.valueOf(userTrafficCount.getMin());
        redisService.setUserAccessedCount(Integer.valueOf(userId), count.intValue(), min, path);
    }


}

