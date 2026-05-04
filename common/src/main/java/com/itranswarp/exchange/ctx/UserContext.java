package com.itranswarp.exchange.ctx;

import com.itranswarp.exchange.ApiError;
import com.itranswarp.exchange.ApiException;

/**
 * Holds user context in thread-local.
 * 保存用户上下文的线程本地变量。
 */
public class UserContext implements AutoCloseable {

    static final ThreadLocal<Long> THREAD_LOCAL_CTX = new ThreadLocal<>();

    /**
     * Get current user id, or throw exception if no user.
     * 获取当前用户id，如果没有用户，则抛出异常。
     */
    public static Long getRequiredUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new ApiException(ApiError.AUTH_SIGNIN_REQUIRED, null, "Need signin first.");
        }
        return userId;
    }

    /**
     * Get current user id, or null if no user.
     * 获取当前用户id，如果没有用户，则返回null。
     */
    public static Long getUserId() {
        return THREAD_LOCAL_CTX.get();
    }

    public UserContext(Long userId) {
        THREAD_LOCAL_CTX.set(userId);
    }

    @Override
    public void close() {
        THREAD_LOCAL_CTX.remove();
    }
}
