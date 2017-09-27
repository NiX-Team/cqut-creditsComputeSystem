package com.nix.web.annotation;

import java.lang.annotation.*;

/**
 * Created by 11723 on 2017/5/4.
 */
@Target( {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Clear {
    boolean value() default true;
}
