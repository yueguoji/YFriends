package com.yupi.usercenter.service;
import java.util.Date;

// [编程学习交流圈](https://www.code-nav.cn/) 连接万名编程爱好者，一起优秀！20000+ 小伙伴交流分享、40+ 大厂嘉宾一对一答疑、100+ 各方向编程交流群、4000+ 编程问答参考

import com.google.common.base.Stopwatch;
import com.yupi.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 用户服务测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    /**
     * 测试添加用户
     */
    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("岳");
        user.setUserAccount("Yuuue");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setPhone("12323455");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    // https://www.code-nav.cn/

    /**
     * 测试更新用户
     */
    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("dogYupi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.updateById(user);
        Assertions.assertTrue(result);
    }

    /**
     * 测试删除用户
     */
    @Test
    public void testDeleteUser() {
        boolean result = userService.removeById(1L);
        Assertions.assertTrue(result);
    }

    // https://space.bilibili.com/12890453/

    /**
     * 测试获取用户
     */
    @Test
    public void testGetUser() {
        User user = userService.getById(1L);
        Assertions.assertNotNull(user);
    }

    /**
     * 测试用户注册
     */
    @Test
    void userRegister() {
        String userAccount = "Yuuue1";

        String checkPassword = "1234561111";

        long result = userService.userRegister(userAccount, checkPassword, checkPassword, "1");
    }



    @Test
    public void test1(){
        List<String> list1 = Arrays.asList("java", "c++");
        List<String> list = new ArrayList<>();
        list.add("java");
        list.add("c++");

        List<User> userList = userService.searchUserByTags(list);
        System.out.println(userList);

    }

    /**
     * 插入数据
     */
    @Test
    public void importData(){
        final int IMPORT_NUM = 1000;
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            User user = new User();
            user.setUsername("Yuuue"+i);
            user.setUserAccount("Yuuue"+i);
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("1234567812");
            user.setPhone("1234");
            user.setEmail("12345");
            user.setUserStatus(0);
            user.setIsDelete(0);
            user.setUserRole(0);
            user.setPlanetCode("12345");
            users.add(user);

        }

        //批量插入数据
        userService.saveBatch(users,20);

    }

    /**
     * 并发插入数据
     */
    @Test
    public void concurrencyInsertData(){
//        Stopwatch stopwatch = new Stopwatch();
        final int IMPORT_NUM = 1000;
        int j = 0;
//        stopwatch.start()
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < IMPORT_NUM; i++) {
            ArrayList<User> users = new ArrayList<>();
            j++;
            while (true){
                User user = new User();
                user.setUsername("Yuuue"+i);
                user.setUserAccount("Yuuue"+i);
                user.setAvatarUrl("");
                user.setGender(0);
                user.setUserPassword("1234567812");
                user.setPhone("1234");
                user.setEmail("12345");
                user.setUserStatus(0);
                user.setIsDelete(0);
                user.setUserRole(0);
                user.setPlanetCode("12345");
                users.add(user);
                if (j%100==0){
                    break;
                }
                //异步执行
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                    userService.saveBatch(users, 20);
                });
                futureList.add(voidCompletableFuture);

                CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
            }


        }




    }
}
