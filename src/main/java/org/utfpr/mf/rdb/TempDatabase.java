package org.utfpr.mf.rdb;

import lombok.Data;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.model.Credentials;
import org.utfpr.mf.tools.DataImporter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Data
public class TempDatabase {

    private Credentials credentials;
    private Connection connection;

    protected TempDatabase(Credentials credentials) throws SQLException {
        this.credentials = credentials;
        connection = DriverManager.getConnection(credentials.getConnectionString(), credentials.getUsername(), credentials.getPassword());
    }

    public static TempDatabase create(String databaseName, Credentials credentials) throws SQLException {
        boolean isNew = true;
        var mainConnection = DriverManager.getConnection(credentials.getConnectionString(), credentials.getUsername(), credentials.getPassword());
        var result = DataImporter.Companion.createDatabase(mainConnection, databaseName);
        if(!result.equals("OK"))
        {
            System.out.println("Error creating database: " + result + "\n Connecting to an existing database");
            isNew = false;
        }
        var conn = DriverManager.getConnection(credentials.getBaseConnectionString() + "/" + databaseName, credentials.getUsername(), credentials.getPassword());

        var n_cred = new Credentials(credentials.getBaseConnectionString() + "/" + databaseName, credentials.getUsername(), credentials.getPassword(), isNew ? Credentials.CreationMethod.CREATE_DATABASE : Credentials.CreationMethod.USE_EXISTING);

        return new TempDatabase(n_cred);
    }

    public static ResponseCreateDatabase createDatabaseAndExecuteSQL(String databaseName, String sql, RdbDatabase rdb) throws SQLException {
        DataImporter.Companion.createDatabase(rdb.getConnection(), databaseName);
        try {
            var creds = rdb.getCredentials();
            var conn = DriverManager.getConnection(creds.getBaseConnectionString() + "/" + databaseName, creds.getUsername(), creds.getPassword());
            var exRes = sql == null ? "Not executed" : DataImporter.Companion.runSQL(sql, conn);
            var n_cred = new Credentials(creds.getBaseConnectionString() + "/" + databaseName, creds.getUsername(), creds.getPassword(), Credentials.CreationMethod.CREATE_DATABASE);
            return new ResponseCreateDatabase(exRes, new TempDatabase(n_cred));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String executeSQL(String databaseName, String sql) {
        var conn = connection;
        if(conn == null) throw new RuntimeException("Database not found");
        return DataImporter.Companion.runSQL(sql, conn);
    }

    public String getCreateSQL(String databaseName) {
        var conn = connection;
        if(conn == null) throw new RuntimeException("Database not found");
        DbMetadata metadata = null;
        try {
            metadata = new DbMetadata(conn, null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return metadata.toString();
    }

    public void drop() throws SQLException {
        DataImporter.Companion.runSQL("DROP DATABASE " + credentials.getDatabaseName() + " WITH (FORCE);", connection);
        connection.close();
    }

}
