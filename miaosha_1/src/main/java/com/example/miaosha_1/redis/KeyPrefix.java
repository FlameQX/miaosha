package com.example.miaosha_1.redis;

public interface KeyPrefix {

    public int expireSeconds();

    public String getPrefix();
}
