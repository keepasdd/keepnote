package com.keepasd.knowledgebase.service;

import com.keepasd.knowledgebase.entity.User;

public interface UserService {
    User login(String username, String password);
    boolean register(String username, String password, String email);
    User getByUsername(String username);
}
