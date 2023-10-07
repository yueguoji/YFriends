# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>
# 用户表
drop table if exists user;

create table user
(
    username     varchar(256)                       null comment '用户昵称',
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '用户头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(512)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 not null comment '状态 0 - 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    planetCode   varchar(512)                       null comment '星球编号'
)
    comment '用户';

# [加入编程导航](https://t.zsxq.com/0emozsIJh) 入门捷径+交流答疑+项目实战+求职指导，帮你自学编程不走弯路
drop table if exists team;
create table team
(

    id           bigint auto_increment comment 'id' primary key,
    teamName         varchar(256)                not     null comment '队伍名称',
    userId  bigint                     not   null comment '用户Id',
    teamDescription varchar(512) null comment '描述',
    maxNum int default 1 not null comment '最大人数',
    expireTime datetime null comment '过期时间',
    teamStatus tinyint default 0 not null comment '0-公开，1-私有，2-加密',
    teamPassword varchar(512)                       not null comment '队伍密码',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除'
)
    comment '用户';


drop table if exists user_team;
create table user_team
(

    id           bigint auto_increment comment 'id' primary key,
    userId  bigint                     not   null comment '用户Id',
    teamId  bigint                     not   null comment '用户Id',
    joinTime   datetime default CURRENT_TIMESTAMP null comment '加入时间',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除'
)
    comment '用户队伍关系表';

