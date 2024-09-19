package org.mf.langchain.DTO;

public class Credentials {

        public enum CreationMethod {
            CREATE_DATABASE,
            USE_EXISTING,
            USER_PROVIDED
        }


        private String connectionString;
        private String username;
        private String password;
        private CreationMethod creationMethod;


        public Credentials(){}
        public Credentials(String connectionString, String username, String password) {
            this.connectionString = connectionString;
            this.username = username;
            this.password = password;
            this.creationMethod = CreationMethod.USER_PROVIDED;
        }

        public Credentials(String connectionString, String username, String password, CreationMethod creationMethod) {
            this.connectionString = connectionString;
            this.username = username;
            this.password = password;
            this.creationMethod = creationMethod;
        }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CreationMethod getCreationMethod() {
        return creationMethod;
    }
}
