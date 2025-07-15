package com.ylm.lmpuffpicturebankend.service;

import com.ylm.lmpuffpicturebankend.model.dto.UserLoginRequest;
import com.ylm.lmpuffpicturebankend.model.dto.UserRegisterRequest;
import com.ylm.lmpuffpicturebankend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ylm.lmpuffpicturebankend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

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
     * 获取脱敏类用户信息
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

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

}
