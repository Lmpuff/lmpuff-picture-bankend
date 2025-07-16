package com.ylm.lmpuffpicturebankend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 以登录用户(脱敏)
 */
@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = -5576425081657938242L;
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
     * 创建时间
     */
    private Date createTime;


}