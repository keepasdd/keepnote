package com.keepasd.knowledgebase.Interceptor;

import com.keepasd.knowledgebase.entity.User;
import com.keepasd.knowledgebase.service.UserService;
import com.keepasd.knowledgebase.util.JwtUtil;
import com.keepasd.knowledgebase.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")){
            response.setStatus(401);
            return false;
        }
        String username = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        User user = userService.getByUsername(username);
        if (user == null){
            response.setStatus(401);
            return false;
        }
        //存入threadlocal
        UserContext.setUserId(user.getId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove(); //请求结束后处理
    }
}
