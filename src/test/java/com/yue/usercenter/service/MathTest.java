package com.yue.usercenter.service;

import com.yue.usercenter.utils.MathUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Yuuue
 * creat by 2023-08-20
 */
public class MathTest {

    @Test
    public void teat(){

        List<String> list = Arrays.asList("java", "大一", "男");
        List<String> list2 = Arrays.asList("jav", "大一", "nv");
        List<String> list3 = Arrays.asList("java", "大二", "男");

        int i = MathUtils.minDistance(list, list2);
        System.out.println(i);
    }
}
