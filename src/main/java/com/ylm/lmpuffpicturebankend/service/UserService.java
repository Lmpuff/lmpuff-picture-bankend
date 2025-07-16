package com.ylm.lmpuffpicturebankend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ylm.lmpuffpicturebankend.model.dto.user.UserLoginRequest;
import com.ylm.lmpuffpicturebankend.model.dto.user.UserQueryRequest;
import com.ylm.lmpuffpicturebankend.model.dto.user.UserRegisterRequest;
import com.ylm.lmpuffpicturebankend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ylm.lmpuffpicturebankend.model.vo.LoginUserVO;
import com.ylm.lmpuffpicturebankend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author MI
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-07-15 16:56:24
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     * @param userLoginRequest
     * @param httpServletRequest
     * @return
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest);

    /**
     * 获取脱敏后的登录用户信息
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取当前登录用户
     * @param httpServletRequest
     * @return
     */
    User getLoginUser(HttpServletRequest httpServletRequest);

    /**
     * 用户注销
     * @param httpServletRequest
     */
    boolean userLogout(HttpServletRequest httpServletRequest);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 密码加密处理
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String passwordEncryptionProcessing(String userPassword);

}
