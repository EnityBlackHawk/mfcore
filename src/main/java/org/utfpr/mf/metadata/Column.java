package org.utfpr.mf.metadata;

import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.tools.SqlDataType;

public record Column(String name, SqlDataType dataType, Boolean isPk, @Nullable FkInfo fkInfo, Boolean isUnique) {


    public boolean isFk() {
        return fkInfo != null;
    }

    public boolean isPrimaryKey() {
        return isPk;
    }

    public record FkInfo(String columnName, String pk_tableName, String pk_name){

        public String toString_() {
            return columnName + " REFERENCES " + pk_tableName;
        }

        @Override
        public String toString() {
            return " REFERENCES " + pk_tableName + "(" + pk_name + ")";
        }
    }

    @Override
    public String toString() {

        if(dataType == null) {
            System.out.println("Data type is null here");
            return "";
        }

        return name + " " + dataType.name() + (isPk ? " PRIMARY KEY" : "" ) + (isFk() ? fkInfo.toString() : "") ;
    }
}
