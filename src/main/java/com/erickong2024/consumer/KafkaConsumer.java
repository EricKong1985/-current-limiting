package com.erickong2024.consumer;


import com.alibaba.fastjson.JSON;
import com.erickong2024.model.UserTrafficCount;
import com.erickong2024.service.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@EnableKafka
public class KafkaConsumer {

    @Autowired
    private RedisService redisService;

    @KafkaListener(topics = "status_topic", groupId = "traffic-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message, Acknowledgment acknowledgment) {
        try {
            if (StringUtils.isAllEmpty(message)) return;
            UserTrafficCount userTrafficCount = JSON.parseObject(message, UserTrafficCount.class);
            System.out.println("接收Flink返回的统计信息:" + JSON.toJSONString(userTrafficCount));
            String userId = userTrafficCount.getUserId();
            Long count = userTrafficCount.getCount();
            String path = userTrafficCount.getPath();
            Long min = Long.valueOf(userTrafficCount.getMin());
            redisService.setUserAccessedCount(Integer.valueOf(userId), count.intValue(), min, path);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 手动提交偏移量
            acknowledgment.acknowledge();
        }
    }


}

