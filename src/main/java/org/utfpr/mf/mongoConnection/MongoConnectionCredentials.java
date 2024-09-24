package org.utfpr.mf.mongoConnection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MongoConnectionCredentials {

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
}
