package cn.dong.nexus.security.exception;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.api.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@Hidden // 不加这个注解 swagger文档报错，knife4j的问题
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SaTokenExceptionHandler {
    /**
     * SaToken异常处理
     **/
    @ExceptionHandler(SaTokenException.class)
    public Result<Void> handleException(SaTokenException e) {
        log.error(e.getMessage(), e);
        return Result.error(ApiMessage.UNAUTHORIZED);
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handException(NotPermissionException e) {
        log.error(e.getMessage());
        return Result.error(ApiMessage.FORBIDDEN);

    }

}
