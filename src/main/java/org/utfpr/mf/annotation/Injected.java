package org.utfpr.mf.annotation;
import org.utfpr.mf.enums.DefaultInjectParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Injected {
    DefaultInjectParams value() default DefaultInjectParams.UNSET;
    boolean required() default true;
}
