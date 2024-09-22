package org.utfpr.mf.migration.params;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RdbCredentials {
    public enum CreationMethod {
        CREATE_DATABASE,
        USE_EXISTING,
        USER_PROVIDED
    }


    private String connectionString;
    private String username;
    private String password;
    private CreationMethod creationMethod;


    public RdbCredentials(){}
    public RdbCredentials(String connectionString, String username, String password) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
        this.creationMethod = CreationMethod.USER_PROVIDED;
    }

    public RdbCredentials(String connectionString, String username, String password, CreationMethod creationMethod) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
        this.creationMethod = creationMethod;
    }
}
