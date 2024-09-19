import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.utfpr.mf.DocumentTest;

public class MongoTemplateTest {

    @Test
    void mongoTemplateInsert() {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/test");
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "test");
        DocumentTest document = new DocumentTest("1", "test");
        mongoTemplate.insert(document);
    }

}
