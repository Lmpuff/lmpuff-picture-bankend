package com.ylm.lmpuffpicturebankend.model.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 以登录用户(脱敏)
 */
@Data
public class LoginUserVO implements Serializable {
    private static final long serialVersionUID = -5414776501150228500L;
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}