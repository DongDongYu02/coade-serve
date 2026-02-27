package cn.dong.nexus.core.api;

import lombok.Getter;

@Getter
public enum ApiMessage {

    OK(true, "ok", 200),
    ERROR(false, "操作失败！", 500),
    SUCCESS(true, "操作成功！", 200),
    INTERNAL_ERROR(false, "系统异常，请稍后再试！", 500),
    NOT_FOUND(false, "资源未找到！", 404),

    UNAUTHORIZED(false, "用户未授权！", 401),
    FORBIDDEN(false, "无权访问！", 403);

    private final boolean success;
    private final String message;
    private final Integer code;

    ApiMessage(boolean success, String message, Integer code) {
        this.success = success;
        this.message = message;
        this.code = code;
    }
}
