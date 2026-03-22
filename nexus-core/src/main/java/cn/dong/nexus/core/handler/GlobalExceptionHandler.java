package cn.dong.nexus.core.handler;

import cn.dong.nexus.core.api.ApiMessage;
import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.exception.BizException;
import cn.hutool.core.text.StrJoiner;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
@Hidden // 不加这个注解 swagger文档报错，knife4j的问题
public class GlobalExceptionHandler {

    private static final String VALID_MUST_NOT_NULL = "不能为null";

    private static final String VALID_MUST_NOT_BLANK = "不能为空";

    private static final String VALID_CONVERT_ERROR = "格式错误";
    @PostConstruct
    public void init() {
        log.info("SaTokenExceptionHandler loaded");
    }
    /**
     * 业务异常处理
     **/
    @ExceptionHandler(BizException.class)
    public Result<Void> handleException(BizException e) {
        if (Objects.isNull(e.getCode())) {
            return Result.error(e.getMessage());
        }
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数异常处理
     **/
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleException(IllegalArgumentException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public String handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request
    ) {
        log.error("不支持的Content-Type异常, uri={}, method={}, contentType={}, remoteAddr={}, userAgent={}, msg={}",
                request.getRequestURI(),
                request.getMethod(),
                request.getContentType(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                e.getMessage(),
                e);

        return "Content-Type not supported";
    }




    /**
     * Validation异常处理
     **/
    @ExceptionHandler(BindException.class)
    public Result<String> bindExceptionHandler(BindException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        if (allErrors.size() == 1) {
            ObjectError objectError = allErrors.get(0);
            String defaultMessage = objectError.getDefaultMessage();
            DefaultMessageSourceResolvable argument = (DefaultMessageSourceResolvable)
                    Objects.requireNonNull(objectError.getArguments())[0];
            if (StrUtil.isBlank(defaultMessage)) {
                return Result.error(e.getMessage());
            }
            if (defaultMessage.equals(VALID_MUST_NOT_NULL) ||
                defaultMessage.equals(VALID_MUST_NOT_BLANK)) {
                return Result.error(argument.getCode() + VALID_MUST_NOT_BLANK);
            }
            return Result.error(defaultMessage);
        }
        StrJoiner sj = new StrJoiner(StrUtil.COMMA);
        for (ObjectError error : e.getBindingResult().getAllErrors()) {
            String defaultMessage = error.getDefaultMessage();
            DefaultMessageSourceResolvable argument = (DefaultMessageSourceResolvable)
                    Objects.requireNonNull(error.getArguments())[0];
            if (StrUtil.isNotBlank(defaultMessage)) {
                if (defaultMessage.equals(VALID_MUST_NOT_NULL) ||
                    defaultMessage.equals(VALID_MUST_NOT_BLANK)) {
                    sj.append(argument.getCode() + VALID_MUST_NOT_BLANK);
                    continue;
                }
                if (defaultMessage.contains("Failed to convert")) {
                    sj.append(argument.getCode() + VALID_CONVERT_ERROR);
                    continue;
                }
                sj.append(defaultMessage);
            }
        }
        if (StrUtil.isNotBlank(sj.toString())) {
            return Result.error(sj.toString());
        }
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleAllException(Exception e) {
        log.error(e.getMessage());
        return Result.error(ApiMessage.NOT_FOUND);
    }


    /**
     * 全局异常处理
     **/
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.error(ApiMessage.INTERNAL_ERROR);
    }


}
