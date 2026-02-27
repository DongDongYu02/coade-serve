package cn.dong.nexus.core.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果
 *
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;

    @Schema(description = "状态码")
    private int code;
    @Schema(description = "成功标识")
    private boolean success;
    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "数据")
    private T data;

    public Result() {
    }

    public Result(int code, boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<T>(SUCCESS_CODE, true, "ok", null);
    }

    public static <T> Result<T> success(String message) {
        return new Result<T>(SUCCESS_CODE, true, message, null);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<T>(SUCCESS_CODE, true, message, data);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(SUCCESS_CODE, true, "ok", data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<T>(ERROR_CODE, false, message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<T>(code, false, message, null);
    }

    public static <T> Result<T> error(ApiMessage apiMessage) {
        return new Result<T>(apiMessage.getCode(), apiMessage.isSuccess(), apiMessage.getMessage(), null);
    }
}
