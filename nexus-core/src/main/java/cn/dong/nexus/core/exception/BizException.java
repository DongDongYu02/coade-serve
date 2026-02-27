package cn.dong.nexus.core.exception;

import cn.dong.nexus.core.api.ApiMessage;
import lombok.Getter;

import java.io.Serial;

@Getter
public class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    public BizException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public BizException(ApiMessage apiMessage) {
        super(apiMessage.getMessage());
        this.code = apiMessage.getCode();
    }

    public BizException(String message) {
        super(message);
    }

    public BizException() {

    }

}
