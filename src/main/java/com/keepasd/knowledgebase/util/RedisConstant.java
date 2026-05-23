package com.keepasd.knowledgebase.util;

public class RedisConstant {
    public static final String USER_INFO_KEY = "user:info:";  // + userId
    public static final long USER_INFO_TTL = 30;  // 30分钟

    public static final String NOTE_LIST_KEY = "note:list:";  // + userId:page:pageSize:keyword:categoryId:tagId:isFavorite:dateRange
    public static final long NOTE_LIST_TTL = 600;  // 600秒

    public static final String NOTE_DETAIL_KEY = "note:detail:";  // + noteId
    public static final String NOTE_INDEX_TOKEN_KEY = "note:index:token:";  // + token -> noteId set
    public static final String NOTE_INDEX_DOC_KEY = "note:index:doc:";  // + noteId -> token set
    public static final String NOTE_INDEX_USER_KEY = "note:index:user:";  // + userId -> noteId set
}
