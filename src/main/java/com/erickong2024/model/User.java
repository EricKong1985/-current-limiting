package com.erickong2024.model;

public class User {

    private String id;           // 用户ID

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    private String token;        // 用户令牌
    private int limit;           // 每分钟的请求限制（QPS 限制）

    public User(String id, String token, int limit) {
        this.id = id;
        this.token = token;
        this.limit = limit;
    }



}