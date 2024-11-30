package com.erickong2024.model;


public class UserRequest {

    private String token;           // 用户的 token，用于识别用户身份

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private String path;             // 请求的 API 名称（例如：api1, api2, api3）
    private Long timestamp; // 请求时间戳
    private String requestData;     // 请求携带的数据，视实际需求而定
    private User user;



}