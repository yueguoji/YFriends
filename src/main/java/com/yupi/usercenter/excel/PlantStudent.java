package com.yupi.usercenter.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode
public class PlantStudent {


    @ExcelProperty("成员编号")
    private String planetCode;

    @ExcelProperty("成员昵称")
    private String username;

    @ExcelProperty("本月积分")
    private String integral;

}
