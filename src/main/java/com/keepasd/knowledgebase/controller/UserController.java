package com.keepasd.knowledgebase.controller;

import com.keepasd.knowledgebase.common.Result;
import com.keepasd.knowledgebase.entity.User;
import com.keepasd.knowledgebase.service.UserService;
import com.keepasd.knowledgebase.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        User user = userService.login(params.get("username"), params.get("password"));
        if (user != null) {
            String token = JwtUtil.generateToken(user.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success(data);
        }
        return Result.fail("用户名或密码错误");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, String> params) {
        boolean success = userService.register(
            params.get("username"),
            params.get("password"),
            params.get("email")
        );
        if (success) {
            return Result.success("注册成功");
        }
        return Result.fail("用户名已存在");
    }
}
