package org.utfpr.mf.metadata;

import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.model.Credentials;
import org.utfpr.mf.tools.SqlDataType;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DbMetadata {

    private final Connection _connection;
    private ArrayList<Table> tables = new ArrayList<>();
    private DatabaseMetaData _metadata;
    private String lastError = "";

    public DbMetadata(Connection connection, @Nullable String tableNamePatter) throws SQLException {
        _connection = connection;

        if(_connection == null) return;

        _metadata = _connection.getMetaData();

        ResultSet tbs = _metadata.getTables(null, null, tableNamePatter, new String[] {"TABLE"});

        while(tbs.next()) {
            ArrayList<Column> columnArrayList = new ArrayList<>();
            ArrayList<String> pk_names = new ArrayList<>();
            HashMap<String, Column.FkInfo> fks_info = new HashMap<>();
            ArrayList<String> unique_cols = new ArrayList<>();
            String tb_name = tbs.getString("TABLE_NAME");
            ResultSet cls = _metadata.getColumns(null, null, tb_name, null);
            ResultSet pks = _metadata.getPrimaryKeys(null, null, tb_name);
            ResultSet fks = _metadata.getImportedKeys(null, null, tb_name);
            ResultSet uniques = _metadata.getIndexInfo(null, null, tb_name, true, false);

            while(pks.next())
            {
                String columnName = pks.getString("COLUMN_NAME");
                pk_names.add(columnName);
            }

            while(uniques.next())
            {
                String columnName = uniques.getString("COLUMN_NAME");
                unique_cols.add(columnName);
            }

            while (fks.next())
            {
                String pkTableName = fks.getString("PKTABLE_NAME");
                String pkColumnName = fks.getString("PKCOLUMN_NAME");
                String fkTableName = fks.getString("FKTABLE_NAME");
                String fkColumnName = fks.getString("FKCOLUMN_NAME");
                Column.FkInfo fi = new Column.FkInfo(
                        fkColumnName,
                        pkTableName,
                        pkColumnName
                );
                fks_info.put(fkColumnName, fi);
            }

            while (cls.next())
            {
                String columnName = cls.getString("COLUMN_NAME");
                String datatype = cls.getString("DATA_TYPE");

                columnArrayList.add(
                        new Column(columnName,
                                SqlDataType.getByValue(Integer.parseInt(datatype)),
                                pk_names.contains(columnName),
                                fks_info.getOrDefault(columnName, null),
                                unique_cols.contains(columnName)
                        )
                );

            }
            tables.add(
                    new Table(
                            tb_name,
                            columnArrayList
                    )
            );
        }
    }

    public DbMetadata(Credentials credentials, @Nullable String tableNamePatter) throws SQLException {
        this(credentials.getConnectionString(), credentials.getUsername(), credentials.getPassword(), tableNamePatter);
    }

    public DbMetadata(String connectionString, String username, String password, @Nullable String tableNamePatter) throws SQLException {
        Connection tmp_connection;
        try {
            tmp_connection = DriverManager.getConnection(connectionString, username, password);
        } catch (SQLException e) {
            lastError = e.getMessage();
            tmp_connection = null;
        }
        _connection = tmp_connection;

        if(_connection == null) return;

        _metadata = _connection.getMetaData();

        ResultSet tbs = _metadata.getTables(null, null, tableNamePatter, new String[] {"TABLE"});

        while(tbs.next()) {
            ArrayList<Column> columnArrayList = new ArrayList<>();
            ArrayList<String> pk_names = new ArrayList<>();
            HashMap<String, Column.FkInfo> fks_info = new HashMap<>();
            ArrayList<String> unique_cols = new ArrayList<>();
            String tb_name = tbs.getString("TABLE_NAME");
            ResultSet cls = _metadata.getColumns(null, null, tb_name, null);
            ResultSet pks = _metadata.getPrimaryKeys(null, null, tb_name);
            ResultSet fks = _metadata.getImportedKeys(null, null, tb_name);
            ResultSet uniques = _metadata.getIndexInfo(null, null, tb_name, true, false);

            while(pks.next())
            {
                String columnName = pks.getString("COLUMN_NAME");
                pk_names.add(columnName);
            }

            while(uniques.next())
            {
                String columnName = uniques.getString("COLUMN_NAME");
                unique_cols.add(columnName);
            }

            while (fks.next())
            {
                String pkTableName = fks.getString("PKTABLE_NAME");
                String pkColumnName = fks.getString("PKCOLUMN_NAME");
                String fkTableName = fks.getString("FKTABLE_NAME");
                String fkColumnName = fks.getString("FKCOLUMN_NAME");
                Column.FkInfo fi = new Column.FkInfo(
                        fkColumnName,
                        pkTableName,
                        pkColumnName
                );
                fks_info.put(fkColumnName, fi);
            }

            while (cls.next())
            {
                String columnName = cls.getString("COLUMN_NAME");
                String datatype = cls.getString("DATA_TYPE");

                columnArrayList.add(
                        new Column(columnName,
                                SqlDataType.getByValue(Integer.parseInt(datatype)),
                                pk_names.contains(columnName),
                                fks_info.getOrDefault(columnName, null),
                                unique_cols.contains(columnName)
                        )
                );

            }
            tables.add(
                    new Table(
                            tb_name,
                            columnArrayList
                    )
            );
        }


    }

    public static Connection createConnection(String connectionString, String username, String password) throws SQLException {
        try {
            return DriverManager.getConnection(connectionString, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean isTableExists(String tableName) {
        return tables.stream().anyMatch(table -> table.name().equalsIgnoreCase(tableName));
    }

    public String checkTableNameAndTryFix(String tableName) {
        if(isTableExists(tableName)) {
            return tableName;
        }

        if(tableName.endsWith("ies")) {
            String fixedName = tableName.substring(0, tableName.length() - 3) + "y";
            if(isTableExists(fixedName)) {
                return fixedName;
            }
        }

        if(tableName.charAt(tableName.length() - 1) == 's') {
            String fixedName = tableName.substring(0, tableName.length() - 1);
            if(isTableExists(fixedName)) {
                return fixedName;
            }
        }

        return tableName;
    }

    public boolean isConnected() {
        try {
            return _connection != null && !_connection.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return _connection;
    }

    public List<Table> getTables() {
        return tables;
    }

    public String getLastError() {
        return lastError;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (var x : getTables()) {
            s.append(x.toString()).append("\n");
        }
        return s.toString();
    }
}
