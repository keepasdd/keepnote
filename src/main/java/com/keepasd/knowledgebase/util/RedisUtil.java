package com.keepasd.knowledgebase.util;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    //往redis当中存入一个带有过期时间的key
    public void set(String key, String value, long timeout, TimeUnit unit){
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    //查询某个key是否存在
    public boolean haskey(String key){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 存对象（序列化成 JSON）
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), timeout, unit);
    }

    // 取对象
    public <T> T getObject(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        return JSONUtil.toBean(json, clazz);
    }

    // 删除
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 按 pattern 批量删除（如 "note:list:123:*"）
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
