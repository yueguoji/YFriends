package com.yupi.usercenter.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yupi.usercenter.common.ResultUtils;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author Yuuue
 * 定时 任务
 * creat by 2023-08-09
 */
@Component
@Slf4j
public class ScheduledJon {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;


    @Scheduled(cron = "0 48 17 * * *")   //每天的18时14分定时查询
    public void doRedisJob(){

        RLock lock = redissonClient.getLock("yupao:precache:recommend");
        try {
            log.info("lock{}",Thread.currentThread().getId());
        if (lock.tryLock(0,3000L,TimeUnit.MILLISECONDS)){
            Gson gson = new Gson();
            Long id = 1L;
            String userKey = String.format("yupao:user:recommend%s",id);
            ValueOperations<String, Object> operations = redisTemplate.opsForValue();
//        String pageStr = (String)operations.get(userKey);
//        Page<User> page = gson.fromJson(pageStr, Page.class);
            Page<User> page = new Page(1,20);


            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getId,id);
            page = userService.page(page, queryWrapper);
            operations.set(userKey, gson.toJson(page),1000000, TimeUnit.MILLISECONDS);
        }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            //根据锁的Id释放锁
            if (lock.isHeldByCurrentThread())
            //放到finally里 防止try里面的代码抛异常导致没有进行解锁
                lock.unlock();
        }



    }
}
