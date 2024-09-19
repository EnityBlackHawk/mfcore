package org.mf.langchain.util;

public enum SqlDataType {

    // Numeric Types
    TINYINT(-6),
    SMALLINT(5),
    INTEGER(4),
    BIGINT(-5),
    DECIMAL(3),
    REAL(7),
    DOUBLE(8),
    FLOAT(6),
    NUMERIC(2),

    // String Types
    CHAR(1),
    VARCHAR(12),
    NCHAR(-15),
    NVARCHAR(-9),
    CLOB(2005),
    NCLOB(2011),
    BLOB(2004),
    VARBINARY(-3),

    // Boolean Type
    BOOLEAN(16),

    // Date/Time Types
    DATE(91),
    TIME(92),
    TIMESTAMP(93),
    TIMESTAMPTZ(2014),

    // Other Types
    BINARY(-2),
    BIT(-7),
    DATALINK(70), // Use String representation for non-standard types
    SQLXML(2009),
    ARRAY(2003),
    STRUCT(2002),
    DISTINCT(2001),
    JAVAOBJECT(2000),
    REF(2006),
    REFCURSOR(2012),
    ROWID(-8),
    NULL(0),

    // Unknown Type (add more as needed)
    UNKNOWN(-1);

    private final int sqlType;

    public static SqlDataType getByValue(int n) {
        for(var x : SqlDataType.values()){
            if(x.getSqlType() == n)
                return x;
        }
        return null;
    }

    SqlDataType(int sqlType) {
        this.sqlType = sqlType;
    }

    public int getSqlType() {
        return sqlType;
    }
}