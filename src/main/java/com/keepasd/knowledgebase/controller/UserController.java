package com.keepasd.knowledgebase.controller;

import com.keepasd.knowledgebase.common.Result;
import com.keepasd.knowledgebase.dto.request.UpdateUserDTO;
import com.keepasd.knowledgebase.entity.User;
import com.keepasd.knowledgebase.service.UserService;
import com.keepasd.knowledgebase.util.JwtUtil;
import com.keepasd.knowledgebase.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        log.info("用户登录请求，username={}", params.get("username"));
        User user = userService.login(params.get("username"), params.get("password"));
        if (user != null) {
            String token = JwtUtil.generateToken(user.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            log.info("用户登录成功，username={}, token={}", user.getUsername(), token);
            return Result.success(data);
        }
        log.info("用户登录失败，username={}", params.get("username"));
        return Result.fail("用户名或密码错误");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, String> params) {
        log.info("用户注册请求，username={}", params.get("username"));
        boolean success = userService.register(
            params.get("username"),
            params.get("password"),
            params.get("email")
        );
        if (success) {
            log.info("用户注册成功，username={}", params.get("username"));
            return Result.success("注册成功");
        }
        log.info("用户注册失败，username={} 已存在", params.get("username"));
        return Result.fail("用户名已存在");
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        User user = userService.getUserInfo(UserContext.getUserId());
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody UpdateUserDTO dto) {
        userService.updateUser(UserContext.getUserId(), dto);
        return Result.success();
    }
}
