package com.erickong2024.flink;

import com.alibaba.fastjson.JSON;
import com.erickong2024.config.KafkaProducerConfig;
import com.erickong2024.model.User;
import com.erickong2024.model.UserRequest;
import com.erickong2024.model.UserTrafficCount;
import com.erickong2024.util.StringUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.print.attribute.standard.JobName;
import java.util.Properties;

@Service
public class FlinkJobService {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.income-group-id}")
    private String consumerGroupId;


    @Autowired
    private KafkaProducerConfig kafkaProducerConfig;

    //    @PostConstruct
    public void startFlinkJob() throws Exception {
        // Flink 环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        // Kafka 配置
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        kafkaProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        // 设置偏移量从最早的消息开始消费
        kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 消费 Kafka 数据
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("access_topic", new SimpleStringSchema(), kafkaProps);
        consumer.setCommitOffsetsOnCheckpoints(true);
        // 处理数据流
        DataStream<String> stream = env.addSource(consumer);

        // 处理流数据并进行映射
        DataStream<Tuple5<Long, String, String, Integer, Long>> resultStream = stream
                .map(new MapFunction<String, Tuple5<Long, String, String, Integer, Long>>() {
                    @Override
                    public Tuple5<Long, String, String, Integer, Long> map(String value) throws Exception {
                        // 解析 JSON 数据
                        UserRequest userRequest = JSON.parseObject(value, UserRequest.class);
                        String time = StringUtils.convertMinutesToTime(String.valueOf(userRequest.getTimestamp()));
                        System.out.println("Flink获取消息，消息时间:" + time + ",内容:" + JSON.toJSONString(userRequest));
                        User user = userRequest.getUser();
                        String userId = String.valueOf(user.getId());
                        Long min = userRequest.getTimestamp();
                        String path = userRequest.getPath();
                        int limit = userRequest.getUser().getLimit();
                        // 返回包含 min, userId, path, 计数的 Tuple4
                        Tuple5<Long, String, String, Integer, Long> result = Tuple5.of(min, userId, path, limit, 1L);
//                        System.out.println(result);
                        return result;
                    }
                })
                // 分配时间戳并设置水印
                .assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<Tuple5<Long, String, String, Integer, Long>>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public long extractTimestamp(Tuple5<Long, String, String, Integer, Long> element, long previousElementTimestamp) {
                        return element.f0;  // 使用事件的时间戳作为元素的时间戳
                    }

                    @Override
                    public Watermark getCurrentWatermark() {
                        // 返回当前水印，假设水印延迟为 1 秒
                        return new Watermark(System.currentTimeMillis() - 1000);
                    }
                })
                // 按 min, userId 和 path 分组
                .keyBy(0, 1, 2)
                // 使用 10 秒的时间窗口
//                .timeWindow(Time.seconds(10))
                // 对第五个元素（计数）进行求和
                .sum(4);

        // 发送到 Kafka
        FlinkKafkaProducer<Tuple5<Long, String, String, Integer, Long>> producer = new FlinkKafkaProducer<>(
                "status_topic",
                new KafkaSerializationSchema<Tuple5<Long, String, String, Integer, Long>>() {
                    @Override
                    public ProducerRecord<byte[], byte[]> serialize(Tuple5<Long, String, String, Integer, Long> element, Long timestamp) {
                        // 将 Tuple4 转为 JSON 字符串
                        Integer limit = element.f3.intValue();
                        Integer count = element.f4.intValue();
                        Integer min = element.f0.intValue();
                        String path = element.f2;
                        String userId = element.f1;
                        UserTrafficCount userTrafficCount = new UserTrafficCount(String.valueOf(userId), String.valueOf(min), count, path);
                        String jsonString = JSON.toJSONString(userTrafficCount);
                        if (count < limit) {
                            System.out.println("还没有到达限制:" + jsonString);
                            return new ProducerRecord<>("status_topic", "".getBytes());
                        } else {
                            System.out.println("到达限制:" + jsonString);
                            return new ProducerRecord<>("status_topic", jsonString.getBytes());
                        }

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
