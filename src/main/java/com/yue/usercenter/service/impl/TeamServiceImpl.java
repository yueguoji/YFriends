package com.yue.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yue.usercenter.enums.TeamStutusEnum;
import com.yue.usercenter.exception.BusinessException;
import com.yue.usercenter.mapper.TeamMapper;
import com.yue.usercenter.model.domain.Team;
import com.yue.usercenter.model.domain.User;
import com.yue.usercenter.model.dto.TeamDto;
import com.yue.usercenter.model.request.JoinTeamRequest;
import com.yue.usercenter.model.vo.TeamVo;
import com.yue.usercenter.model.vo.UserVo;
import com.yue.usercenter.service.TeamService;
import com.yue.usercenter.service.UserService;
import com.yue.usercenter.service.UserTeamService;
import com.yue.usercenter.common.ErrorCode;
import com.yue.usercenter.model.domain.UserTeam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
* @author YUE
* @description 针对表【team(用户)】的数据库操作Service实现
* @createDate 2023-08-12 15:31:58
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;
//
//    @Resource
//    private TeamService teamService;

    @Override
    @Transactional(rollbackFor= Exception.class)
    public long saveAndAdd(User user, Team team) {
        if (team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (user==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);

        }
//        if (team.getMaxNum()==null){
//            team.setMaxNum(5);
//        }
        if (team.getMaxNum() <= 1 && team.getMaxNum() > 20) {

            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数错误");
        }

        if (StringUtils.isEmpty(team.getTeamName()) || team.getTeamName().length()>20) {

            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称错误");
        }
        if (StringUtils.isNotEmpty(team.getTeamDescription()) && team.getTeamDescription().length()>512) {

            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述错误");
        }
        if (team.getTeamStatus()==null){
            team.setTeamStatus(TeamStutusEnum.PUBLIC.getStatus());
        }
        if (team.getTeamStatus().equals(TeamStutusEnum.SECRET.getStatus())){
            if (StringUtils.isEmpty(team.getTeamPassword()) || team.getTeamPassword().length()>32 )
            {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不正确");

            }
        }
        //new Date().after(team.getExpireTime()) 比较时间大小
//        boolean after = new Date().after(team.getExpireTime());
        if (team.getExpireTime()!=null && team.getExpireTime().getTime()<System.currentTimeMillis()){

            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不正确");
        }
        LambdaQueryWrapper<Team> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.eq(Team::getUserId,user.getId());
        long count = this.count(lambdaQueryWrapper);
        if (count>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "可创建队伍数量已满");
        }
        team.setUserId(user.getId());
        this.save(team);
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(user.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        userTeam.setCreateTime(new Date());
        userTeam.setUpdateTime(new Date());
        userTeamService.save(userTeam);
        return team.getId();
    }

    @Override
    public List<TeamVo> listTeam(TeamDto teamdto, boolean isAdmin) {
        if (teamdto==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        LambdaQueryWrapper<Team> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(teamdto.getTeamName()), Team::getTeamName, teamdto.getTeamName())
                .like(StringUtils.isNotEmpty(teamdto.getTeamDescription()), Team::getTeamDescription, teamdto.getTeamDescription())
                .eq(teamdto.getMaxNum()!=null, Team::getMaxNum, teamdto.getMaxNum())
                .eq(teamdto.getUserId()!=null, Team::getUserId, teamdto.getUserId())
                .and(lqw ->lqw.lt(Team::getExpireTime,teamdto.getExpireTime()).or().isNull(Team::getExpireTime))
                .and(StringUtils.isNotEmpty(teamdto.getSearchText()),lqw ->lqw.like(Team::getTeamName,teamdto.getSearchText()).or().like(Team::getTeamDescription,teamdto.getSearchText()));

        if (!isAdmin){
            TeamStutusEnum teamStutusEnum = TeamStutusEnum.getTeamStutusEnum(teamdto.getTeamStatus());
            if (teamStutusEnum==null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            if (teamdto.getTeamStatus().equals(TeamStutusEnum.PRIVATE.getStatus())){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
        }
        List<TeamVo> teamVos =new ArrayList<>();
        //判断idList
        if (!CollectionUtils.isEmpty(teamdto.getIdList())){
            lambdaQueryWrapper.in(Team::getId,teamdto.getIdList());
        }
        List<Team> teamList = this.list(lambdaQueryWrapper);

        if (teamList.size()<1){
            return teamVos;

        }
        for (Team team : teamList) {
            TeamVo teamVo = new TeamVo();
            BeanUtils.copyProperties(team,teamVo);
            Long userId = team.getUserId();
            User user = userService.getById(userId);

            if (user!=null){
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user,userVo);
                teamVo.setUser(userVo);
            }
            teamVos.add(teamVo);

        }

        return teamVos;
    }

    @Override
    @Transactional(rollbackFor= Exception.class)
    public boolean updateTeam(Team team, HttpServletRequest servletRequest) {

        Long id = team.getId();
        //查询队伍是否为空
        Team oldTeam = this.getById(id);
        if (oldTeam==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        //脱敏
       User safetyUser = userService.getSafetyUserByHttp(servletRequest);
       //判断是否为管理员
        boolean admin = userService.isAdmin(servletRequest);
        if (!admin && !safetyUser.getId().equals(oldTeam.getUserId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //判断新老数据

        //判断加密状态
        if (team.getTeamStatus().equals(TeamStutusEnum.SECRET.getStatus())){
            if (StringUtils.isEmpty(team.getTeamPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"修改为加密状态需设置密码");
            }
        }


        return this.updateById(team);
    }

    @Override
    @Transactional(rollbackFor= Exception.class)
    public boolean joinTeam(JoinTeamRequest joinTeamRequest, HttpServletRequest request) {
        //登录用户
        User user = userService.getloginUser(request);
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper =  new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getUserId,user.getId());
        long userTeamNum = userTeamService.count(lambdaQueryWrapper);
        //用户最多加入五个队伍
        if (userTeamNum>=5){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户最多加入五个队伍");
        }
        //队伍必须存在，只能加入未满、未过期的队伍
        Long teamId = joinTeamRequest.getTeamId();
        LambdaQueryWrapper<Team> teamQueryWrapper =  new LambdaQueryWrapper<>();
        teamQueryWrapper.eq(Team::getId,teamId);
        Team team = this.getOne(teamQueryWrapper);
        if (team ==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍不存在");
        }
        teamQueryWrapper.gt(Team::getExpireTime,new Date()).or().isNull(Team::getExpireTime);
        team = this.getOne(teamQueryWrapper);
        if (team ==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍不存在");
        }
        //队伍数量要小于队伍最大数量
        lambdaQueryWrapper =  new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId,teamId);
        long teamNum = userTeamService.count(lambdaQueryWrapper);
        if (teamNum>=team.getMaxNum()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍已满");
        }
        //不能重复加入已加入的队伍
        lambdaQueryWrapper =  new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId,teamId)
                      .eq(UserTeam::getUserId,user.getId());
        long userJoinTeamNum = userTeamService.count(lambdaQueryWrapper);
        if (userJoinTeamNum>=1){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍已满");
        }



        if (user.getId().equals(team.getUserId())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不允许加入自己的队伍");
        }
        if (TeamStutusEnum.PRIVATE.getStatus() == team.getTeamStatus()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不允许加入私有得队伍");
        }

        if (TeamStutusEnum.SECRET.getStatus() == team.getTeamStatus()) {
            if (StringUtils.isEmpty(joinTeamRequest.getTeamPassword()) || !joinTeamRequest.getTeamPassword().equals(team.getTeamPassword())){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"密码不匹配");
            }
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(user.getId());
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);


    }

    /**
     * 加入队伍
     * @param teamId
     * @param user
     * @return
     */
    @Override
    @Transactional(rollbackFor= Exception.class)
    public boolean quitTeam(Long teamId, User user) {
        Team team = this.getById(teamId);
        //检查队伍是否存在
        if (team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //效验是否已经加入队伍
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId,teamId).eq(UserTeam::getUserId,user.getId());
        UserTeam joinTeam = userTeamService.getOne(lambdaQueryWrapper);
        if (joinTeam==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"未加入队伍");

        }
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId,teamId);
        long joinTeamNum = userTeamService.count(lambdaQueryWrapper);
        if (joinTeamNum==1){
            //队伍只剩一人
            this.removeById(teamId);
            return userTeamService.remove(lambdaQueryWrapper);
        }else {
            //队伍还有其他人
            if (user.getId().equals(team.getUserId())){
                //如果是队长
                lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(UserTeam::getTeamId,teamId).orderByAsc(UserTeam::getId);
                lambdaQueryWrapper.last("limit 2");
                List<UserTeam> userTeamList = userTeamService.list(lambdaQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) ||userTeamList.size()<=1){
                    throw new BusinessException(ErrorCode.NULL_ERROR,"队伍数量异常");
                }

                lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(UserTeam::getUserId, user.getId());
                boolean remove = userTeamService.remove(lambdaQueryWrapper);
                if (!remove) {

                    throw new BusinessException(ErrorCode.NULL_ERROR, "删除失败");
                }
                UserTeam userTeam = userTeamList.get(1);

                LambdaQueryWrapper<Team> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                lambdaQueryWrapper1.eq(Team::getId,teamId);
                Team team1 = this.getOne(lambdaQueryWrapper1);
                team1.setUserId(userTeam.getUserId());
                return this.updateById(team1);


            }else {
                //如果不是队长直接退出队伍
                lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(UserTeam::getTeamId,teamId).eq(UserTeam::getUserId, user.getId());
                return userTeamService.remove(lambdaQueryWrapper);
            }
        }
    }
}




