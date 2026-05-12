package com.eric.neusoftai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eric.neusoftai.entity.User;
import com.eric.neusoftai.mapper.UserMapper;
import com.eric.neusoftai.util.Md5Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 用户注册
     */
    public String register(String username, String password) {
        if (username == null || username.length() < 6 || username.length() > 12) {
            return "账号长度必须在6-12位之间";
        }
        if (password == null || password.length() < 6) {
            return "密码长度不能少于6位";
        }

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (count > 0) {
            return "该账号已被注册";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.encrypt(password));
        user.setRole("NORMAL"); // 新注册用户默认为普通用户
        userMapper.insert(user);
        return "ok";
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getPassword, Md5Util.encrypt(password))
        );
    }

    /**
     * 根据ID查询用户
     */
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
