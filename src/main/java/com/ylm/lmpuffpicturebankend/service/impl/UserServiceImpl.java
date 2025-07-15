package com.ylm.lmpuffpicturebankend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.User;
import com.ylm.lmpuffpicturebankend.service.UserService;
import com.ylm.lmpuffpicturebankend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author MI
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-15 16:56:24
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




