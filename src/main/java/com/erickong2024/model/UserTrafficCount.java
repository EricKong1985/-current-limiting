package com.erickong2024.model;


public class UserTrafficCount {
    private String userId;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String min;
    private Long count;
    private String path;


    public UserTrafficCount(String userId, String min, Integer count, String path) {
        this.userId = userId;
        this.min = min;
        this.count = Long.valueOf(count);
        this.path = path;
    }
}
