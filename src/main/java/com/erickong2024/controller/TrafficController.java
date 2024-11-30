package com.erickong2024.controller;

import com.alibaba.fastjson.JSON;
import com.erickong2024.config.KafkaProducerConfig;
import com.erickong2024.dto.Resp;
import com.erickong2024.model.User;
import com.erickong2024.model.UserRequest;
import com.erickong2024.service.UserService;
import com.erickong2024.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestController
@RequestMapping("/current-limiting/")
public class TrafficController {

    @Autowired
    private KafkaProducerConfig kafkaProducerConfig;

    @Autowired
    private UserService userService;

    // 处理 API1 的 GET 请求
    @GetMapping("/api1")
    public ResponseEntity<String> handleApi1Request(
            @RequestHeader(value = "token", required = false) String token) {
//        System.out.println("api1");
        UserRequest userRequest = new UserRequest();
        userRequest.setPath("api1");
        userRequest.setToken(token);
        return handleRequest(userRequest);
    }

    @GetMapping("/ok")
    public ResponseEntity<String> handleOkRequest() {
        return ResponseEntity.ok("ok");
    }

    // 处理 API2 的 POST 请求
    @PostMapping("/api2")
    public ResponseEntity<String> handleApi2Request(@RequestBody UserRequest userRequest) {
        userRequest.setPath("api2");
        return handleRequest(userRequest);
    }

    // 处理 API3 的 PUT 请求
    @PutMapping("/api3")
    public ResponseEntity<String> handleApi3Request(@RequestBody UserRequest userRequest) {
        userRequest.setPath("api3");

        return handleRequest(userRequest);
    }

    // 通用请求处理逻辑
    private ResponseEntity<String> handleRequest(UserRequest userRequest) {

//        System.out.println(JSON.toJSONString(userRequest));

        userRequest.setTimestamp(Long.valueOf(StringUtils.getMinutesSince1970()));

        // Check if the token is empty
        if (userRequest.getToken() == null || userRequest.getToken().isEmpty()) {
            String body = Resp.toJSON(Resp.failed("Token cannot be empty"));
            return ResponseEntity.status(HttpStatus.OK).body(body);
        }

        // 根据 token 获取用户对象，假设存在一个 UserService
        User user = userService.getUserByToken(userRequest.getToken());
        if (user == null) {
            String body = Resp.toJSON(Resp.failed("Invalid token"));
            return ResponseEntity.status(HttpStatus.OK).body(body);
        }

        //添加用户信息
        userRequest.setUser(user);
        // 记录请求到 Kafka
        putAccessingRecordToTopic(userRequest);

        // 判断当前 API 是否允许用户访问
        if (!userService.isRequestAllowed(userRequest)) {
            String msg = "QPS limit exceeded with " + user.getLimit() + " per minute!";
            String body = Resp.toJSON(Resp.failed(msg));
            return ResponseEntity.status(HttpStatus.OK).body(body);
        }

        String body = Resp.toJSON(Resp.ok());
        return ResponseEntity.ok(body);
    }

    // 发送消息到 Kafka
    private void putAccessingRecordToTopic(UserRequest userRequest) {
        Long timestamp = Long.valueOf(StringUtils.getMinutesSince1970());
        userRequest.setTimestamp(timestamp);
        // 创建请求消息，发送到 Kafka
        String message = JSON.toJSONString(userRequest);
        kafkaProducerConfig.sendMessage("access_topic", message);
    }

    // Exception handler for missing request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String body = Resp.toJSON(Resp.failed("Request body is missing or unreadable"));
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }
}