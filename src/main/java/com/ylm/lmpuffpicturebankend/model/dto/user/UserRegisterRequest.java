package com.ylm.lmpuffpicturebankend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -388199071521060802L;

    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 校验密码
     */
    private String checkPassword;

}
