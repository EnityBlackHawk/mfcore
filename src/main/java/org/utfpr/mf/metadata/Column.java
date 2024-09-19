package org.mf.langchain.metadata;

import org.jetbrains.annotations.Nullable;
import org.mf.langchain.util.SqlDataType;

public record Column(String name, SqlDataType dataType, Boolean isPk, @Nullable FkInfo fkInfo) {


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
        return name + " " + dataType.name() + (isPk ? " PRIMARY KEY" : "" ) + (isFk() ? fkInfo.toString() : "") ;
    }
}
