package org.utfpr.mf.mongoConnection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Data;
import org.springframework.data.mongodb.core.MongoTemplate;

@Data
public class MongoConnection {

    private MongoClient client;
    private MongoTemplate template;
    private MongoConnectionCredentials credentials;

    public MongoConnection(MongoConnectionCredentials credentials) {

        String uri = credentials.getUsername() != null ? String.format("mongodb://%s:%s@%s:%d/%s",
                credentials.getUsername(),
                credentials.getPassword(),
                credentials.getHost(),
                credentials.getPort(),
                credentials.getDatabase())
                :
                String.format("mongodb://%s:%d/%s",
                        credentials.getHost(),
                        credentials.getPort(),
                        credentials.getDatabase());
        MongoClient mongoClient = MongoClients.create(uri);

        this.client = mongoClient;
        this.template = new MongoTemplate(mongoClient, credentials.getDatabase());
        this.credentials = credentials;
    }

    public MongoDatabase getDatabase() {
        return client.getDatabase(credentials.getDatabase());
    }

    public void clearAll() {
        template.getCollectionNames().forEach(template::dropCollection);
    }


}
