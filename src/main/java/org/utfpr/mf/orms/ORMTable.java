package org.utfpr.mf.orms;


import org.utfpr.mf.prompt.Framework;

public class ORMTable {

    private static ORM springData = new ORM(
            Framework.SPRING_DATA,
            "org.springframework.data.annotation.Id",
            "org.springframework.data.mongodb.core.mapping.DBRef",
            "org.springframework.data.mongodb.core.mapping.Document"
            ) {};


    public static ORM getOrmAnnotations(Framework framework) {
        switch (framework) {
            case SPRING_DATA:
                return springData;
            default:
                return null;
        }
    }

}
