package cn.dong.nexus.core.resmapping.annotation;

import java.lang.annotation.*;

/**
 * <p>结果集资源字段翻译注解</p>
 * <p>支持List或IPage类型的返回结果集</p>
 *
 * @author Dong
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ResultTranslate {

}
