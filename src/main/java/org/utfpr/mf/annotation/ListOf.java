package org.utfpr.mf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ListOf {

    Class<?> value();
    String table();
    String column();
    String targetTable();
    String targetColumn();
}
