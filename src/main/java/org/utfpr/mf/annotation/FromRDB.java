package org.utfpr.mf.annotation;

public @interface FromRDB {

    String type();
    String table();
    String column();

}
