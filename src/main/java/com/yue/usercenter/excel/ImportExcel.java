package com.yue.usercenter.excel;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * @author Yuuue
 * creat by 2023-08-05
 */
public class ImportExcel {
    public static void main(String[] args) {
//        indexOrNameRead();
        synchronousRead();
    }



    public static void indexOrNameRead() {
        String fileName = "E:\\yupi\\code\\yupao\\yupao-backed-master\\src\\main\\resources\\Excelplant.xlsx";
        // 这里默认读取第一个sheet
        EasyExcel.read(fileName, PlantStudent.class, new TableListener()).sheet().doRead();
    }

    /**
     * 没有使用监听器
     */
    public static void synchronousRead() {
        String fileName = "E:\\yupi\\code\\yupao\\yupao-backed-master\\src\\main\\resources\\Excelplant.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<PlantStudent> list = EasyExcel.read(fileName).head(PlantStudent.class).sheet().doReadSync();
        for (PlantStudent plantStudent : list) {
            System.out.println(plantStudent);
        }

    }
}
