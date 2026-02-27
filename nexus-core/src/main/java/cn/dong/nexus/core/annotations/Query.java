package cn.dong.nexus.core.annotations;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;

import java.lang.annotation.*;

/**
 * 条件查询注解
 *
 * @author Dong
 * @date 17:14 2023/11/8
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Query {

    /**
     * 条件关键字 EQ,LIKE,GT,LT....
     **/
    SqlKeyword value();

    /**
     * 字段名，默认为对象属性名
     **/
    String column() default "";

}
