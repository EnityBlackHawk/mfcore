package org.utfpr.mf.model;

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
        private String databaseName;


        public Credentials(){}
        public Credentials(String connectionString, String username, String password) {
            this.connectionString = connectionString;
            this.username = username;
            this.password = password;
            this.creationMethod = CreationMethod.USER_PROVIDED;
        }

        public Credentials(String connectionString, String username, String password, String databaseName) {
            this.connectionString = connectionString;
            this.username = username;
            this.password = password;
            this.creationMethod = CreationMethod.USER_PROVIDED;
            this.databaseName = databaseName;
        }

        public Credentials(String connectionString, String username, String password, CreationMethod creationMethod) {
            this.connectionString = connectionString;
            this.username = username;
            this.password = password;
            this.creationMethod = creationMethod;
        }

        public String getBaseConnectionString() {
            String f = "";
            String dbName;
            for(int i = connectionString.length() - 1; i >= 0; i--) {
                if(connectionString.charAt(i) == '/') {
                    f = connectionString.substring(0, i);
                    break;
                }
            }
            return f;
        }

        public String getDatabaseName() {
            if(databaseName != null) {
                return databaseName;
            }
            String f = "";
            for(int i = connectionString.length() - 1; i >= 0; i--) {
                if(connectionString.charAt(i) == '/') {
                    f = connectionString.substring(i + 1);
                    break;
                }
            }
            return f;
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
