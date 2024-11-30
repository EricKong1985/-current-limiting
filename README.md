
# 项目名称
基于用户等级QPS访问限流管理系统

技术栈:SpringBoot,Redis,Kafka,Flink,Docker,Maven

## 实现思路
1. SpringBoot Controller接收用户请求;
2. 请请求信息(Path,userId,QPS限制)发送到Kafka;
3. KafkaSteam和Flink进行计算每一分钟内容已经请求的数量;
4. 假如计算结果大于用户的QPS限制(limit),将结果(当前分钟和请求量)发送到Kafka
5. Kafka订阅上面的消息，更新Redis
6. SpringBoot Controller接收用户请求，判断用户是否被限流，如果被限流，返回错误信息，否则继续执行业务逻辑

### 环境要求
- JDK22
- Docker for Desktop(Windows)

### 安装步骤
1. 拉取代码

`git clone https://github.com/EricKong1985/current-limiting`

2. 进入项目根目录,部署Redis,Zookeeper,Kafka,Flink，kafka UI

`docker-compose up`

3. 运行项目,启动SpringBoot项目

`mvn clean install`

`mvn spring-boot:run`

4. 假如报错类似，Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make field private static final long java.util.Properties.serialVersionUID accessible: module java.base does not "opens java.util" to unnamed module需要给通过添加 JVM 参数如下

--add-exports=java.base/sun.nio.ch=ALL-UNNAMED

--add-opens=java.base/java.lang=ALL-UNNAMED 

--add-opens=java.base/java.lang.reflect=ALL-UNNAMED  

--add-opens=java.base/java.io=ALL-UNNAMED 

--add-opens java.base/java.util=ALL-UNNAMED

--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED

5. 假如项目对你有用,请给个Star,谢谢！


