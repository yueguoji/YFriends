package com.yupi.usercenter.config;


import lombok.Data;
import lombok.Value;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.swing.*;

/**
 * @author Yuuue
 * creat by 2023-08-10
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

   private String host;

    private String port;

    private String password;

    @Bean
    public RedissonClient RedissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress(String.format("redis://%s:%s",host,port)).setPassword(password).setDatabase(3);
        RedissonClient redisson = Redisson.create(config);
        return redisson;



    }


}
