package com.keepasd.knowledgebase.util;

public class UserContext {
    private static final ThreadLocal<Long> threadlocal = new ThreadLocal<>();

    public static void setUserId(Long id){
        threadlocal.set(id);
    }
    //获取userid
    public static Long getUserId(){
        return threadlocal.get();
    }
    public static void remove(){
        threadlocal.remove(); //防止内存泄漏
    }
}
