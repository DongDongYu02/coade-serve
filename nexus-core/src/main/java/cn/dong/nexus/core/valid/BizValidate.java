package cn.dong.nexus.core.valid;

import java.lang.annotation.*;

public interface BizValidate {

    @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Unique {
        String message() default "重复值";
    }


}
