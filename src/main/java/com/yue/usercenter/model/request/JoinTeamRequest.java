package com.yue.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yuuue
 * creat by 2023-08-14
 */
@Data
public class JoinTeamRequest implements Serializable {

    private static final long serialVersionUID = -7579671853307289349L;

    /**
     * 用户Id
     */
    private Long teamId;


    /**
     * 用户密码
     */
    private String teamPassword;


}
