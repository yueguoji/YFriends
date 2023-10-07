package com.yue.usercenter.enums;

public enum TeamStutusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");


    private int status;

    private String msg;

    TeamStutusEnum(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public static TeamStutusEnum getTeamStutusEnum(Integer status){
        if (status==null){
            return null;
        }
        //获取所有的value值
        TeamStutusEnum[] enumValues = TeamStutusEnum.values();
        for (TeamStutusEnum enumValue : enumValues) {
            if (enumValue.getStatus()==status){
                return enumValue;
            }
        }
        return null;

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
