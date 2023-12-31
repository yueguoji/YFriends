package com.yue.usercenter.service;

import com.yue.usercenter.model.request.JoinTeamRequest;
import com.yue.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yue.usercenter.model.domain.User;
import com.yue.usercenter.model.dto.TeamDto;
import com.yue.usercenter.model.vo.TeamVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author YUE
* @description 针对表【team(用户)】的数据库操作Service
* @createDate 2023-08-12 15:31:58
*/
public interface TeamService extends IService<Team> {


    long saveAndAdd(User user,Team team);


    List<TeamVo> listTeam(TeamDto team, boolean isAdmin);

    boolean updateTeam(Team team,HttpServletRequest httpServletRequest);


    boolean joinTeam(JoinTeamRequest joinTeamRequest, HttpServletRequest httpServletRequest);

    boolean quitTeam(Long teamId, User user);

}
