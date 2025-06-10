package org.utfpr.mf.migration;

import com.mongodb.client.model.ValidationOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.utfpr.mf.annotation.Export;
import org.utfpr.mf.annotation.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.mongoConnection.MongoConnection;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;
import org.utfpr.mf.tools.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrateDatabaseStep2 extends MigrateDatabaseStep{

    @Injected(DefaultInjectParams.JSON_SCHEMA_LIST)
    private JsonSchemaList recepies;

    public MigrateDatabaseStep2(MongoConnectionCredentials connectionCredentials) {
        super(connectionCredentials);
    }

    public MigrateDatabaseStep2(PrintStream printStream) {
        super(printStream);
    }

    public MigrateDatabaseStep2(MongoConnectionCredentials connectionCredentials, PrintStream printStream) {
        super(connectionCredentials, printStream);
    }

    @Override
    protected Map<String, Integer> makeMigration(DbMetadata dbMetadata, MongoConnection mongoConnection, Map<String, Class<?>> classes) {

        HashMap<String, Integer> classCount = new HashMap<>();

        //ArrayList<TemplatedThread> threads = new ArrayList<>();

        for(JsonSchema recipe : recepies)
        {
                String className = TemplatedString.capitalize(recipe.getTitle());
                notifyUpdate(new UpdateType("conversion", "Converting " + className), UpdateType.class);

                BEGIN_SUB("Querying data from " + className);
                QueryResult2 qr = DataImporter.Companion.runQuery(
                        String.format("SELECT * FROM %s", dbMetadata.checkTableNameAndTryFix(className)),
                        dbMetadata,
                        QueryResult2.class
                );
                BEGIN_SUB("Converting data: " + className);
                List<?> objects = qr.asObject(classes.get(className));

                BEGIN_SUB("Persisting data: " + className);
                MongoTemplate mTemplate = mongoConnection.getTemplate();
                int count = 0;
                for(var obj : objects)
                {
                    mTemplate.insert(obj);
                    count++;
                }
                classCount.put(className, count);
        }
        notifyUpdate(new UpdateType("concluded", "Migration completed"), String.class);
        return classCount;
    }
}
