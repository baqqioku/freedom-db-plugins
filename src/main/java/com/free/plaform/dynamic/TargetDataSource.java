package com.free.plaform.dynamic;


import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDataSource {


    /** 数据源名称 */
    String value() default "dataSource";
}
