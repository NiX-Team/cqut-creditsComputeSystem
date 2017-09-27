package com.nix.web.annotation;

import java.lang.annotation.*;

/**
 * Created by 11723 on 2017/5/3.
 */
@Target ( {ElementType.TYPE, ElementType.METHOD})
@Retention (RetentionPolicy.RUNTIME)
@Documented
public @interface JSONFromat {
    String date_Fromat() default "yyyy:MM:dd:HH:ss:mm";//默认日期转换格式
//    boolean filterCollection() default true;//默认开启对象和集合过滤
//    String filterStr() default "";//默认的过滤字段字符串
//    boolean filterObject() default true;//默认开启对象过滤
//    String NotFilterStr() default "parentId,childers";//默认不过过滤的复合类型字段名称

}
