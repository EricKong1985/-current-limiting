package com.erickong2024.dto;


import com.alibaba.fastjson.JSON;


public class Resp {
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    private Integer code;
    private Object data;

    public static Resp ok() {
        Resp resp = new Resp();
        resp.setCode(0);
        resp.setData(null);
        resp.setMsg("ok");
        return resp;
    }

    public static Resp failed(Object obj) {
        Resp resp = new Resp();
        resp.setCode(0);
        resp.setData(obj);
        resp.setMsg("ok");
        return resp;
    }

    public static String toJSON(Resp resp) {
        return JSON.toJSONString(resp);
    }
}
