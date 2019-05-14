package com.free.plaform.mybatis.page.dialect;

public abstract class Dialect {

    public static enum Type {
        MYSQL,
        ORACLE,
        SQLSERVER,
        DB2
    }


    public abstract String spellPageSql(String sql, int offset, int limit);

    public String getCountSql(String sql) {
        return "SELECT COUNT(0) FROM (" + sql + ") TEMP";
    }


}
