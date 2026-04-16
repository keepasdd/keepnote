package com.keepasd.knowledgebase.controller;

import com.keepasd.knowledgebase.common.Result;
import com.keepasd.knowledgebase.dto.request.UpdateUserDTO;
import com.keepasd.knowledgebase.dto.response.UserVO;
import com.keepasd.knowledgebase.entity.User;
import com.keepasd.knowledgebase.service.UserService;
import com.keepasd.knowledgebase.util.JwtUtil;
import com.keepasd.knowledgebase.util.RedisUtil;
import com.keepasd.knowledgebase.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
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
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        log.info("用户登录请求，username={}", params.get("username"));
        User user = userService.login(params.get("username"), params.get("password"));
        if (user != null) {
            String token = JwtUtil.generateToken(user.getUsername());
            //把用户信息写入redis,过期时间和token一致 key是token,value是用户信息
            redisUtil.setObject("user:token:"+token, user, JwtUtil.EXPIRATION, java.util.concurrent.TimeUnit.MILLISECONDS);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            log.info("用户登录成功，username={}, token={}", user.getUsername(), token);
            return Result.success(data);
        }
        log.info("用户登录失败，username={}", params.get("username"));
        return Result.fail("用户名或密码错误");
    }

    /**
     * 密码校验规则：
     * 1. 长度 6-30 位
     * 2. 只允许字母和数字
     * 3. 必须包含大写字母、小写字母、数字中的至少两种
     */
    private static final java.util.regex.Pattern PASSWORD_BASE  = java.util.regex.Pattern.compile("^[A-Za-z\\d]{6,30}$");
    private static final java.util.regex.Pattern HAS_LOWER      = java.util.regex.Pattern.compile("[a-z]");
    private static final java.util.regex.Pattern HAS_UPPER      = java.util.regex.Pattern.compile("[A-Z]");
    private static final java.util.regex.Pattern HAS_DIGIT      = java.util.regex.Pattern.compile("\\d");

    private boolean isValidPassword(String pwd) {
        if (pwd == null || !PASSWORD_BASE.matcher(pwd).matches()) return false;
        int kinds = (HAS_LOWER.matcher(pwd).find() ? 1 : 0)
                  + (HAS_UPPER.matcher(pwd).find() ? 1 : 0)
                  + (HAS_DIGIT.matcher(pwd).find() ? 1 : 0);
        return kinds >= 2;
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, String> params) {
        log.info("用户注册请求，username={}", params.get("username"));
        String password = params.get("password");
        if (!isValidPassword(password)) {
            return Result.fail("密码必须为6-30位字母数字组合，且包含大写字母、小写字母、数字中的至少两种");
        }
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
    public Result getUserInfo() {
        UserVO user = userService.getUserInfo(UserContext.getUserId());
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody UpdateUserDTO dto) {
        userService.updateUser(UserContext.getUserId(), dto);
        return Result.success();
    }

    //用户退出登录
    @PostMapping("/logout")
    public Result logout(HttpServletRequest  request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String key = "user:token:"+token;
        redisUtil.delete(token);
        userService.logout(request);
        return Result.success();
    }
}
