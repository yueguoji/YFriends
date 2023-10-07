package com.yupi.usercenter.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author Yuuue
 * creat by 2023-08-09
 */
@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("Yuuue1","ydcsj");
        String yuuue = (String) valueOperations.get("Yuuue1");
//        Assertions.assertTrue(yuuue.equals("1234"));
        System.out.println(yuuue);


    }
}
