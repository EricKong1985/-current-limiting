// src/test/java/com/erickong2024/controller/TrafficControllerTest.java
package com.erickong2024.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TrafficControllerTest {
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Test
//    void handleApi1Request() {
//        String url = "/current-limiting/api1"; // 使用相对URL
//        String token = "user1_token"; // 替换为有效的token
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("token", token); // 设置请求头
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        for (int i = 0; i < 10001; i++) {
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//            // 这里可以根据需要验证响应
//            assertEquals(200, response.getStatusCodeValue(), "Request failed on iteration " + (i + 1));
//        }
//    }
}