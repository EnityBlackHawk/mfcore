package org.utfpr.mf.enums;

public enum BasicClasses {

    INTEGER("Integer"),
    STRING("String"),
    BOOLEAN("Boolean"),
    DOUBLE("Double"),
    FLOAT("Float"),
    LOCAL_DATA_TIME("LocalDateTime");

    private final String type;

    BasicClasses(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public static boolean isBasicClass(String type){
        for(BasicClasses basicClass : BasicClasses.values()){
            if(basicClass.getType().equals(type)){
                return true;
            }
        }
        return false;
    }

}
