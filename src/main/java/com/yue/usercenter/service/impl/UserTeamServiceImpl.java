package com.yue.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.usercenter.mapper.UserTeamMapper;
import com.yue.usercenter.model.domain.UserTeam;
import com.yue.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author YUE
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2023-08-12 15:32:41
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




