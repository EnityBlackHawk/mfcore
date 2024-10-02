package org.utfpr.mf.rdb;

import lombok.Data;
import org.utfpr.mf.model.Credentials;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Data
public class RdbDatabase {

    private Credentials credentials;
    private Connection connection;

    public RdbDatabase(Credentials credentials) {
        this.credentials = credentials;
        try {
            this.connection = DriverManager.getConnection(credentials.getConnectionString(), credentials.getUsername(), credentials.getPassword());
        } catch (SQLException e) {
            this.connection = null;
        }
    }

}
