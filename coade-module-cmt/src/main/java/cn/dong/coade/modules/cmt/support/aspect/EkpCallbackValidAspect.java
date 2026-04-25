package cn.dong.coade.modules.cmt.support.aspect;

import cn.dong.nexus.common.constants.GlobalConstants;
import cn.dong.nexus.core.exception.BizException;
import cn.dong.nexus.infra.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Aspect
@Component
public class EkpCallbackValidAspect {

    /**
     * 翻译切点：@EkpCallbackValid注解
     */
    @Pointcut("@annotation(cn.dong.coade.modules.cmt.support.aspect.annotation.EkpCallbackValid)")
    public void pointCut() {
    }


    @Before("pointCut()")
    public void before() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String accessToken = request.getParameter("access_token");
        Object token = RedisUtil.get(GlobalConstants.CacheKey.EKP_PROVIDE_TOKEN);
        if (Objects.isNull(token)) {
            throw new BizException("ekp callback accessToken has expired!");
        }
        if (!String.valueOf(token).equals(accessToken)) {
            throw new BizException("ekp callback accessToken has expired!");
        }
    }
}
