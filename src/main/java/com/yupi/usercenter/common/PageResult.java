package com.yupi.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yuuue
 * creat by 2023-08-12
 */
@Data
public class PageResult implements Serializable {


    private static final long serialVersionUID = 4565678139186389489L;

    private Integer pageNum;

    private Integer PageSize;

    public void checkParam() {
        if (this.pageNum == null || this.pageNum <= 0) {
            setPageNum(1);
        }
        if (this.PageSize == null || this.PageSize <= 0 || this.PageSize > 100) {
            setPageSize(10);
        }
    }


}
