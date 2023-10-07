package com.yupi.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.usercenter.common.BaseResponse;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.common.ResultUtils;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.domain.Team;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.model.domain.UserTeam;
import com.yupi.usercenter.model.dto.TeamDto;
import com.yupi.usercenter.model.request.JoinTeamRequest;
import com.yupi.usercenter.model.vo.TeamVo;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yupi.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author Yuuue
 * 队伍接口
 * creat by 2023-08-12
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;


    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse saveAndAdd(@RequestBody TeamDto teamDto, HttpServletRequest servletRequest){
        if (teamDto==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.getloginUser(servletRequest);
        Team team = new Team();
        BeanUtils.copyProperties(teamDto,team);
        long teamId = teamService.saveAndAdd(user, team);
//        if (!save){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"新增失败");
//
//        }
        return ResultUtils.ok(teamId);

    }

    @PostMapping("/update")
    public BaseResponse updateTeam(@RequestBody TeamDto teamDto,HttpServletRequest servletRequest){
        //判断请求参数是否为空
        if (teamDto==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamDto,team);

        boolean result = teamService.updateTeam(team,servletRequest);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");

        }
        return ResultUtils.ok(result);

    }

    @PostMapping("/delete")
    public BaseResponse delete(@RequestBody Long id){
        if (id<1){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        boolean result = teamService.removeById(id);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");

        }
        return ResultUtils.ok(result);

    }

    @GetMapping("/list")
    public BaseResponse<List<TeamVo>> listTeam(TeamDto teamDto,HttpServletRequest servletRequest){
        if (teamDto==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        boolean admin = userService.isAdmin(servletRequest);
        User user = userService.getloginUser(servletRequest);
        List<TeamVo> teamVos = teamService.listTeam(teamDto, admin);
        //teamId集合
        List<Long> teamIdList = teamVos.stream().map(TeamVo::getId).collect(Collectors.toList());


        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getId, user.getId())
                .eq(UserTeam::getTeamId, teamIdList);
        List<UserTeam> list = userTeamService.list(queryWrapper);
        Set<Long> hasJoinTeamSet = list.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        teamVos.forEach(item ->{
            boolean contains = hasJoinTeamSet.contains(item.getId());
            item.setHasJoin(contains);
        });

        return ResultUtils.success1(teamVos);
    }

    /**
     * 获取我创建的队伍
     * @param teamDto
     * @param servletRequest
     * @return
     */
    @GetMapping("/list/my/creat")
    public BaseResponse<List<TeamVo>> listCreatTeamMy(TeamDto teamDto,HttpServletRequest servletRequest){
        if (teamDto==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.getloginUser(servletRequest);
        teamDto.setUserId(user.getId());
//        boolean admin = userService.isAdmin(servletRequest);

        List<TeamVo> teamVos = teamService.listTeam(teamDto, true);
        return ResultUtils.success1(teamVos);
    }

    /**
     * 获取我加入的队伍
     * @param teamDto
     * @param servletRequest
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamVo>> listJoinTeamMy(TeamDto teamDto,HttpServletRequest servletRequest){
        if (teamDto==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.getloginUser(servletRequest);

        teamDto.setUserId(user.getId());
//        boolean admin = userService.isAdmin(servletRequest);

        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getUserId,user.getId());
        List<UserTeam> joinTeamlist = userTeamService.list(lambdaQueryWrapper);
        //转化为以teamId为键的Map
        Map<Long, List<UserTeam>> listMap = joinTeamlist.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        //用户加入的team集合
        List<Long> teamIdList = new ArrayList<>(listMap.keySet());
        teamDto.setIdList(teamIdList);



        List<TeamVo> teamVos = teamService.listTeam(teamDto, true);
        return ResultUtils.success1(teamVos);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<Team>> listByPage(@RequestBody TeamDto teamDto){
        if (teamDto==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamDto,team);
        LambdaQueryWrapper<Team> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(teamDto.getTeamName()), Team::getTeamName, team.getTeamName())
                .like(StringUtils.isNotEmpty(teamDto.getTeamDescription()), Team::getTeamName, team.getTeamDescription())
                .eq(StringUtils.isNotEmpty(teamDto.getMaxNum().toString()), Team::getMaxNum, teamDto.getMaxNum());
        teamDto.checkParam();
        Page<Team> page = new Page<>(teamDto.getPageNum(),teamDto.getPageSize());
        page= teamService.page(page, lambdaQueryWrapper);

        return ResultUtils.success1(page);

    }


    @PostMapping("/join")
    public BaseResponse joinTeam(@RequestBody JoinTeamRequest joinTeamRequest,HttpServletRequest httpServletRequest){
        //判断参数
        if (joinTeamRequest==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        boolean result = teamService.joinTeam(joinTeamRequest,httpServletRequest);
        return ResultUtils.ok(result);

    }


    @PostMapping("/quit")
    public BaseResponse quitTeam(@RequestBody Long teamId,HttpServletRequest httpServletRequest){
        //判断参数
        if (teamId==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.getloginUser(httpServletRequest);
        boolean result = teamService.quitTeam(teamId, user);

        return ResultUtils.ok(result);
    }

}
