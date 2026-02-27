package cn.dong.nexus.core.resmapping.aop;

import cn.dong.nexus.core.api.Result;
import cn.dong.nexus.core.resmapping.ResMappingHandler;
import cn.dong.nexus.core.resmapping.ResMappingUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>结果集资源翻译切面</p>
 * <p>配合@ResultTranslate注解实现自动翻译字段</p>
 *
 * @date 10:21 2023/11/8
 **/
@Aspect
@Component
public class ResultTranslateAspect {

    /**
     * 翻译切点：@ResultTranslate注解
     */
    @Pointcut("@annotation(cn.dong.nexus.core.resmapping.annotation.ResultTranslate)")
    public void translatePointCut() {
    }

    /**
     * 清除缓存切点：@PutMapping、@DeleteMapping、@PostMapping 接口
     **/
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void clearCachePointCut() {
    }


    /**
     * 清除翻译缓存切面
     **/
    @After("clearCachePointCut()")
    public void around() {
        ResMappingHandler.RESOURCE_CACHE.invalidateAll();
    }

    /**
     * 字段翻译切面
     *
     * @param point 切点
     **/
    @Around("translatePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();
        Object target = result instanceof Result<?> ? ((Result<?>) result).getData() : result;

        if (target instanceof List) {
            ResMappingUtil.translateField((List<?>) target);
            return result;
        }
        if (target instanceof IPage) {
            ResMappingUtil.translateField(((IPage<?>) target).getRecords());
            return result;
        }
        ResMappingUtil.translateObjField(target);
        return result;
    }

}
