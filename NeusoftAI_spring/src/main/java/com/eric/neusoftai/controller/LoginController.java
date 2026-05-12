package com.eric.neusoftai.controller;

import com.eric.neusoftai.common.Result;
import com.eric.neusoftai.entity.User;
import com.eric.neusoftai.service.UserService;
import com.eric.neusoftai.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 前后端分离模式
 * 所有接口返回 JSON，不再进行页面跳转
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    /**
     * 登录 - 返回 JWT Token
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String username,
                                             @RequestParam String password) {
        User user = userService.login(username, password);
        if (user == null) {
            return Result.fail("账号或密码错误");
        }

        String token = JwtUtil.generateToken(user);
        user.setPassword(null); // 不返回密码

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.ok("登录成功", data);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestParam String username,
                              @RequestParam String password) {
        String msg = userService.register(username, password);
        if ("ok".equals(msg)) {
            return Result.ok("注册成功，请登录", null);
        }
        return Result.fail(msg);
    }

    /**
     * 退出登录（前端清除 Token 即可）
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.ok("退出成功", null);
    }
}
