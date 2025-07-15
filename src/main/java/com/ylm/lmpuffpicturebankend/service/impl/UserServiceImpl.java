package com.ylm.lmpuffpicturebankend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylm.lmpuffpicturebankend.constant.UserConstant;
import com.ylm.lmpuffpicturebankend.exception.BusinessException;
import com.ylm.lmpuffpicturebankend.exception.ErrorCode;
import com.ylm.lmpuffpicturebankend.exception.ThrowUtils;
import com.ylm.lmpuffpicturebankend.model.dto.UserLoginRequest;
import com.ylm.lmpuffpicturebankend.model.dto.UserRegisterRequest;
import com.ylm.lmpuffpicturebankend.model.entity.User;
import com.ylm.lmpuffpicturebankend.model.enums.UserRoleEnum;
import com.ylm.lmpuffpicturebankend.model.vo.LoginUserVO;
import com.ylm.lmpuffpicturebankend.service.UserService;
import com.ylm.lmpuffpicturebankend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
* @author MI
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-15 16:56:24
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService, UserConstant {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 前端传过来的数据
     * @return
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 校验用户登录信息
        ThrowUtils.throwIf(userAccount == null || userPassword == null || checkPassword == null,
                ErrorCode.PARAMS_ERROR, "参数为空");

        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Long selectCount = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(selectCount > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        // 对密码进行加密并写入数据库中
        String encryptedPassword = passwordEncryptionProcessing(userPassword);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "注册失败，数据库出现异常");
        return 0;
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest,
                                 HttpServletRequest httpServletRequest) {
        // 校验用户
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "用户为空");
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "用户长度过短");
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码过短");
        // 对密码加密后查询
        String passwordEncryption = passwordEncryptionProcessing(userPassword);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", passwordEncryption);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("用户登录失败，用户不存在或密码错误");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 记录用户的登录态
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取脱敏类用户信息
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     * @param httpServletRequest
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest httpServletRequest) {
        // 判断是否已经登录
        Object userObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null,
                ErrorCode.NOT_LOGIN_ERROR);
        // 从数据库中再查一遍数据
        currentUser = this.baseMapper.selectById(currentUser.getId());
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return currentUser;
    }

    /**
     * 获取当前登录用户
     *
     * @param httpServletRequest
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest httpServletRequest) {
        // 判断是否已经登录
        Object userObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR);
        // 撤销登录态
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 密码加密处理
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    private String passwordEncryptionProcessing(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "ylm";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}




