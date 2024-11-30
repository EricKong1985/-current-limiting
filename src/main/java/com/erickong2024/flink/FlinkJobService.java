package com.erickong2024.flink;

import com.alibaba.fastjson.JSON;
import com.erickong2024.model.User;
import com.erickong2024.model.UserRequest;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Service
public class FlinkJobService {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

//    @PostConstruct
    public void startFlinkJob() throws Exception {
        // Flink 环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka 配置
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        // 消费 Kafka 数据
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("access_topic", new SimpleStringSchema(), kafkaProps);

        // 处理数据流
        DataStream<String> stream = env.addSource(consumer);

        // 处理流数据并进行映射
        DataStream<Tuple4<Long, String, String, Long>> resultStream = stream
                .map(new MapFunction<String, Tuple4<Long, String, String, Long>>() {
                    @Override
                    public Tuple4<Long, String, String, Long> map(String value) throws Exception {
                        // 解析 JSON 数据
                        UserRequest userRequest = JSON.parseObject(value, UserRequest.class);
                        User user = userRequest.getUser();
                        String userId = String.valueOf(user.getId());
                        Long min = userRequest.getTimestamp();
                        String path = userRequest.getPath();

                        // 返回包含 min, userId, path, 计数的 Tuple4
                        return Tuple4.of(min, userId, path, 1L);
                    }
                })
                // 按 min, userId 和 path 分组
                .keyBy(0, 1, 2)
                // 使用 10 秒的时间窗口
                .timeWindow(Time.seconds(10))
                // 对第四个元素（计数）进行求和
                .sum(3);

        // 发送到 Kafka
        FlinkKafkaProducer<Tuple4<Long, String, String, Long>> producer = new FlinkKafkaProducer<>(
                "output_topic",
                new KafkaSerializationSchema<Tuple4<Long, String, String, Long>>() {
                    @Override
                    public ProducerRecord<byte[], byte[]> serialize(Tuple4<Long, String, String, Long> element, Long timestamp) {
                        // 将 Tuple4 转为 JSON 字符串
                        String jsonString = JSON.toJSONString(element);
                        return new ProducerRecord<>("status_topic", jsonString.getBytes());
                    }
                },
                kafkaProps,
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE
        );

        // 将结果发送到 Kafka
        resultStream.addSink(producer);

        // 执行 Flink 作业
        env.execute("Flink Kafka Job");
    }
}
