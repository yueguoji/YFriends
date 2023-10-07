package com.yue.usercenter.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yue.usercenter.exception.BusinessException;
import com.yue.usercenter.mapper.UserMapper;
import com.yue.usercenter.model.domain.User;
import com.yue.usercenter.service.UserService;
import com.yue.usercenter.utils.MathUtils;
import com.yue.usercenter.common.ErrorCode;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yue.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.yue.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    // https://www.code-nav.cn/

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    // [加入星球](https://www.code-nav.cn/) 从 0 到 1 项目实战，经验拉满！10+ 原创项目手把手教程、7 日项目提升训练营、60+ 编程经验分享直播、1000+ 项目经验笔记

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTagName(originUser.getTagName());
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param httpServletRequest
     * @return
     */
    @Override
    public User getSafetyUserByHttp(HttpServletRequest httpServletRequest) {
        User originUser = (User)httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
//        ArrayList<User> users = new ArrayList<>();
        //使用sql查询
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        for (String tagName : tagNameList) {
//            queryWrapper.like(User::getTagName, tagName);
//        }
//        List<User> list = this.list(queryWrapper);
//        return list.stream().map(this::getSafetyUser).collect(Collectors.toList());

        //使用内存查询
        //查询出所有
        List<User> list = this.list();
        return list.stream().filter(user ->{
            Gson gson = new Gson();
            String tagName = user.getTagName();
            if (tagName==null){
                return false;
            }
            //json字符串转集合
            Set<String> tagNameSet = gson.fromJson(tagName, new TypeToken<Set<String>>() {
            }.getType());
            //判空
            tagNameSet =  Optional.ofNullable(tagNameSet).orElse(new HashSet<>());
            for (String s : tagNameSet) {
                if (!tagNameList.contains(s))
                {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());

    }

    /**
     * 更新用户
     * @param user
     * @param request
     * @return
     */
    @Override
    public int updateUser(User user, HttpServletRequest request) {
        //判断参数
        User oldUser = userMapper.selectById(user.getId());
        if (oldUser==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //TODO 补充校验 除了id没有传其他的的 则直接报错
        if (!isAdmin(request) && !user.getId().equals(oldUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return userMapper.updateById(user);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    public User getloginUser(HttpServletRequest request) {

        if (request ==null){
            throw new BusinessException(ErrorCode.NO_AUTH);

        }
        Object obj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (obj ==null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) obj;

    }

    /**
     * 用户匹配
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUser(Long num, User loginUser) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.isNotNull(User::getTagName);
        lambdaQueryWrapper.select(User::getId, User::getTagName);
        List<User> list = this.list(lambdaQueryWrapper);

        String tagName = loginUser.getTagName();
        List<String> tagList = new Gson().fromJson(tagName, new TypeToken<List<String>>() {
        }.getType());
//        SortedMap<Integer, Long> indexMap = new TreeMap<>();
        List<Pair<User,Long>> pairListlist = new ArrayList<>();

        for (User user : list) {
            if (user.getTagName()==null || user.getId().equals(loginUser.getId())){
                continue;
            }
            String userTagName = user.getTagName();
            List<String> userTagList = new Gson().fromJson(userTagName, new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long i1 = MathUtils.minDistance(tagList, userTagList);
            pairListlist.add(new Pair<>(user, i1));

        }
        //按编码顺序从小到大排序
        List<Pair<User, Long>> topNumList = pairListlist.stream().sorted((a, b) -> {
            return (int) (a.getValue() - b.getValue());
        }).limit(num).collect(Collectors.toList());

        //原本顺序的userId列表
        List<Long> distanceList = topNumList.stream().map(item -> item.getKey().getId()).collect(Collectors.toList());
        List<User> finalUserList = new ArrayList<>();
        if (distanceList.size()<=0){
            return finalUserList;
        }
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(User::getId,distanceList);

        Map<Long, List<User>> userIdListMap = this.list(lambdaQueryWrapper).stream().map(this::getSafetyUser).collect(Collectors.groupingBy(User::getId));


        for (Long userId : distanceList) {
            finalUserList.add(userIdListMap.get(userId).get(0));
        }
        return finalUserList;

    }

}

//Yuuue
