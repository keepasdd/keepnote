package com.keepasd.knowledgebase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keepasd.knowledgebase.dto.request.UpdateUserDTO;
import com.keepasd.knowledgebase.dto.response.UserVO;
import com.keepasd.knowledgebase.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {
    User login(String username, String password);
    boolean register(String username, String password, String email);
    User getByUsername(String username);
    UserVO getUserInfo(Long userId);
    void updateUser(Long userId, UpdateUserDTO dto);



    void logout(HttpServletRequest request);
}
