package com.yue.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yue.usercenter.model.request.UserRegisterRequest;
import com.yue.usercenter.common.BaseResponse;
import com.yue.usercenter.common.ErrorCode;
import com.yue.usercenter.common.ResultUtils;
import com.yue.usercenter.exception.BusinessException;
import com.yue.usercenter.model.domain.User;
import com.yue.usercenter.model.request.UserLoginRequest;
import com.yue.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yue.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000") //解决跨域问题
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    RedisTemplate<String,Object> redisTemplate;
    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }


    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recomandUser(long pageSize,long pageNum,HttpServletRequest request) {
        Long userId = userService.getloginUser(request).getId();
        Gson gson = new Gson();
        String userKey = String.format("yupao:user:recommend%s",userId.toString());
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        String pageStr = (String)operations.get(userKey);
        Page<User> page = gson.fromJson(pageStr, Page.class);
        if (page !=null){
            return ResultUtils.success1(page);
        }
        page = new Page(pageNum,pageSize);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        page = userService.page(page, queryWrapper);

        try {
            operations.set(userKey, gson.toJson(page),10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        List<User> userList = userService.list(queryWrapper);
//        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success1(page);
    }

    /**
     * 根据标签查询
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success1(userList);
    }


    /**
     * 删除用户
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        //判断参数
        if (user == null) {
            return ResultUtils.error(ErrorCode.NULL_ERROR);
        }

        return ResultUtils.success(userService.updateUser(user, request));
    }




    @GetMapping("/match")
    public BaseResponse<List<User>> matchUser(Long num, HttpServletRequest request){
        //判断参数
        if (num == null || num <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getloginUser(request);
        List<User> userList = userService.matchUser(num, loginUser);

        return ResultUtils.success(userList);
    }

}
