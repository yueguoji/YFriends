package com.yue.usercenter.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

// 有个很重要的点 DemoDataListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
@Slf4j
public class TableListener implements ReadListener<PlantStudent> {






    /**
     * 这个每一条数据解析都会来调用
     *
     * @param plantStudent    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param analysisContext
     */
    @Override
    public void invoke(PlantStudent plantStudent, AnalysisContext analysisContext) {
//        log.info("开始读取数据");
        System.out.println(plantStudent);
    }


    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("所有数据解析完成！");
    }
}
