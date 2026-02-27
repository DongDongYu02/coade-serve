package cn.dong.nexus.core.annotations;

import java.lang.annotation.*;

/**
 * 自定义需要查询的字段
 *
 * @author Dong
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface SelectColumns {

    String[] columns();

}
