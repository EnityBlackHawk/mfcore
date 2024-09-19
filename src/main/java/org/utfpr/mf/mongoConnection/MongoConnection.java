package org.utfpr.mf.mongoConnection;

import lombok.Data;

@Data
public class MongoConnection {

    private String id;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
}
