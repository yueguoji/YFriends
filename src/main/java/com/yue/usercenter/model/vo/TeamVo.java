package com.yue.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Yuuue
 * creat by 2023-08-13
 */
@Data
public class TeamVo implements Serializable {

    private static final long serialVersionUID = -6874053622475406992L;

    /**
     * id
     * 前端传的id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 用户Id
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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 0-公开，1-私有，2-加密
     */
    private Integer teamStatus;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否加入队伍
     */
    private boolean hasJoin = false;

    private  UserVo user;
}
