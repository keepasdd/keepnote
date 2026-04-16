package com.keepasd.knowledgebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keepasd.knowledgebase.dto.request.UpdateUserDTO;
import com.keepasd.knowledgebase.dto.response.UserVO;
import com.keepasd.knowledgebase.entity.User;
import com.keepasd.knowledgebase.mapper.UserMapper;
import com.keepasd.knowledgebase.service.UserService;
import com.keepasd.knowledgebase.util.JwtUtil;
import com.keepasd.knowledgebase.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username).eq(User::getPassword, password);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public boolean register(String username, String password, String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectOne(wrapper) != null) {
            return false;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        return userMapper.insert(user) > 0;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        //根据用户ID来创建key
        String key = "user:info:"+userId;
        //先查询redis
        UserVO object = redisUtil.getObject(key, UserVO.class);
        //在redis中查询到直接返回
        if (object != null){
            return object;
        }
        //redis没查到，查mysql
        User user = userMapper.selectById(userId);
        //如果数据库中都没查到说明没有，直接返回null
        if (user == null) return null;
        UserVO userVO = new UserVO();
        //查到了之后写入redis
        BeanUtils.copyProperties(user,userVO);
        redisUtil.setObject(key,userVO,30,java.util.concurrent.TimeUnit.MINUTES);
        //最后查询数据库
        return userVO;
    }

    @Override
    public void updateUser(Long userId, UpdateUserDTO dto) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId);
        if (dto.getNickname() != null) wrapper.set(User::getNickname, dto.getNickname());
        if (dto.getEmail() != null) wrapper.set(User::getEmail, dto.getEmail());
        if (dto.getAvatar() != null) wrapper.set(User::getAvatar, dto.getAvatar());
        userMapper.update(null, wrapper);
    }

    @Override
    public void logout(HttpServletRequest request) {
        //从请求头中解析出token
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        //解析token剩余的TTL
        long ttl = jwtUtil.getExpiration(token);

        //存入redis黑名单
        if (ttl > 0){
            redisUtil.set("blacklist:" + token, "1", ttl, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }


    @Override
    public boolean saveBatch(Collection<User> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<User> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<User> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(User entity) {
        return false;
    }

    @Override
    public User getOne(Wrapper<User> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<User> getOneOpt(Wrapper<User> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<User> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<User> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<User> getBaseMapper() {
        return null;
    }

    @Override
    public Class<User> getEntityClass() {
        return null;
    }
}
