package org.utfpr.mf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FromRDB {

    String type();
    Class<?> typeClass();
    String table();
    String column();
    boolean isReference() default false;
    String targetTable() default "";
    String targetColumn() default "";
    String projection() default "*";
    boolean isAbstract() default false;


}
