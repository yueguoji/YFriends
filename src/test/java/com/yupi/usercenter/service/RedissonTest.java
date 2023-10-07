package com.yupi.usercenter.service;

import com.google.gson.Gson;
import netscape.javascript.JSObject;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Yuuue
 * creat by 2023-08-10
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test(){

        RList<String> rList = redissonClient.getList("test1");
        Gson gson = new Gson();
        String str = gson.toJson("你是好人吗");
        String json = gson.fromJson("你是好人吗", String.class);
        rList.add(str);
//        rList.remove(0);
        System.out.println(rList.get(0));

    }
}
