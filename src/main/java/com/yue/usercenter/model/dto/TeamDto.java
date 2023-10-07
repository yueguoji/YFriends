package com.yue.usercenter.model.dto;

import com.yue.usercenter.common.PageResult;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Yuuue
 * creat by 2023-08-12
 */
@Data
public class TeamDto extends PageResult {

    private static final long serialVersionUID = -6787917009516037028L;

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private List<Long> idList;
    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 创建Id
     */
    private Long userId;


    /**
     * 描述
     */
    private String teamDescription;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 0-公开，1-私有，2-加密
     */
    private Integer teamStatus;

    /**
     * 队伍密码
     */
    private String teamPassword;

    /**
     * 过期时间
     */
    private Date expireTime;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否加入队伍
     */
    private boolean hasJoin = false;




}
