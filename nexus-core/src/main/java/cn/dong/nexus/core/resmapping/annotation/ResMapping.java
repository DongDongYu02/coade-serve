package cn.dong.nexus.core.resmapping.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>资源映射注解</p>
 *
 * @author Dong
 * @date 18:12 2023/11/6
 **/
@Retention(RetentionPolicy.RUNTIME)
public @interface ResMapping {

    /**
     * <p>翻译的目标字段</p>
     * <p>不填时，默认将字段尾部Id替换为Name example:entityId -> entityName</p>
     * <p>必须在对象中声明目标字段</p>
     * <p>也可以提供多字段 example:{"entityName","otherField"}</p>
     * <p>targets和values的索引值必须完全匹配，即资源字段的索引值会去匹配目标字段的值</p>
     **/
    String[] targets() default {};

    /**
     * <p>资源表名</p>
     * <p>通过表名找到对应的ORM实体</p>
     * <p>ORM实体上必须声明@TableName注解</p>
     **/
    String sourceTable();

    /**
     * <p>资源key字段</p>
     * <p>默认为 "id"</p>
     * <p>资源类型中必须包含该字段</p>
     **/
    String key() default "";

    /**
     * <p>资源value字段</p>
     * <p>默认为 "name"</p>
     * <p>可以提供多个值 example:{"name","otherField"}</p>
     * <p>也可以提供多字段 example:{"entityName","otherField"}</p>
     * <p>资源类型中必须包含这些字段</p>
     * <p>targets和values的索引值必须完全匹配，即资源字段的索引值会去匹配目标字段的值</p>
     **/
    String[] values() default {};

}
