package org.utfpr.mf.migration;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.util.Pair;
import org.utfpr.mf.MockLayer;
import org.utfpr.mf.annotarion.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.exceptions.DBConnectionException;
import org.utfpr.mf.llm.ChatAssistant;
import org.utfpr.mf.metadata.RelationCardinality;
import org.utfpr.mf.migration.params.MetadataInfo;
import org.utfpr.mf.migration.params.RdbCredentials;
import org.utfpr.mf.tools.CodeSession;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.tools.DataImporter;
import org.utfpr.mf.tools.QueryResult;
import org.utfpr.mf.tools.TemplatedString;
import org.utfpr.mf.metadata.Relations;
import org.utfpr.mf.metadata.Table;
import org.utfpr.mf.metadata.Column;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AcquireMetadataStep extends MfMigrationStepEx {

    @Injected(DefaultInjectParams.LLM_KEY)
    public String llm_key;

    public AcquireMetadataStep() {
        this(System.out);
    }

    public AcquireMetadataStep(PrintStream printStream) {
        super("AcquireMetadataStep", printStream, RdbCredentials.class, MetadataInfo.class);
    }

    @Override
    public Object execute(Object input) {
        assert llm_key != null : "llm_key is not set";
        RdbCredentials cred = (RdbCredentials) input;
        String data = "";
        List<RelationCardinality> card = null;

        if(cred.getConnectionString() == null)
            throw new RuntimeException("RdbCredentials.connectionString is required when not proving data getSource");

        var user = cred.getUsername() != null ? cred.getUsername() : "admin";
        var passw = cred.getPassword() != null ? cred.getPassword() : "admin";
        BEGIN("Connecting to database");
        try {
            var mdb = new DbMetadata(
                    cred.getConnectionString(),
                    user,
                    passw,
                    null);

            if(!mdb.isConnected())
                throw new DBConnectionException(mdb.getLastError());
            data = mdb.toString();
            card = getRelationsCardinality(mdb);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new MetadataInfo(
                data,
                card
        );
    }

    public List<RelationCardinality> getRelationsCardinality(DbMetadata metadata) {

        var queries = new ArrayList<Pair<Relations, String>>();

        var rels = getRelations(metadata.toString(), null);
        //var rels = List.of(new Relations("aircraft", "airline", "many-to-one"));

        List<RelationCardinality> rcd = new ArrayList<>();

        var templateString = new TemplatedString(
                "SELECT {{target}}.{{target_pk}} AS id , " +
                        "COUNT({{getSource}}.{{source_pk}}) AS number_of_{{getSource}} " +
                        "FROM {{getSource}} " +
                        "JOIN {{target}} ON {{getSource}}.{{prop}} = {{target}}.{{target_pk}} " +
                        "GROUP BY {{target}}.{{target_pk}}"
        );
        BEGIN("Creating queries");
        for(var rel : rels) {

            if(rel.cardinality.equals("one-to-many")) continue;
            if(rel.table_source.equals(rel.table_target)) continue;

            Table tSource = metadata.getTables().stream().filter((e) -> Objects.equals(e.name(), rel.table_source))
                    .findFirst().orElseThrow(RuntimeException::new);
            List<Column.FkInfo> props = tSource.columns().stream().filter((e) -> e.isFk() && Objects.equals(e.fkInfo().pk_tableName(), rel.table_target))
                    .map(Column::fkInfo).toList();

            if(props.isEmpty())
                throw new RuntimeException("No foreign key found");

            queries.addAll(
                    props.stream().map((e) -> Pair.of(rel, templateString.render(
                            Pair.of("getSource", rel.table_source),
                            Pair.of("target", rel.table_target),
                            Pair.of("target_pk", e.pk_name()),
                            Pair.of("source_pk", tSource.getPrimaryKey().name()),
                            Pair.of("prop", e.columnName()))
                    )).toList()
            );
        }
        BEGIN("Executing queries");
        for(var q : queries) {
            BEGIN_SUB("Executing query: " + q.getFirst());
            var qResult =
                    DataImporter.Companion.runQuery(q.getSecond(), metadata.getConnection(), QueryResult.class);
            var values = qResult.getAllFromColumn("number_of_" + q.getFirst().table_source, Integer.class);
            int min = values.stream().min(Integer::compareTo).orElseThrow(RuntimeException::new);
            int max = values.stream().max(Integer::compareTo).orElseThrow(RuntimeException::new);
            double avg = values.stream().mapToInt(Integer::intValue).average().orElseThrow();
            rcd.add(new RelationCardinality(q.getFirst(), min, max, avg));
        }

        return rcd;
    }

    public List<Relations> getRelations(String text, @Nullable ChatAssistant gptAssistant) {
        BEGIN("Configuring OpenAi API");
        if(gptAssistant == null)
        {
            var gpt = new OpenAiChatModel.OpenAiChatModelBuilder()
                    .apiKey(llm_key)
                    .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                    .maxRetries(1)
                    .logRequests(true)
                    .logResponses(true)
                    //.responseFormat("json_object")
                    .temperature(0.5)
                    .build();
            gptAssistant = AiServices.builder(ChatAssistant.class).chatLanguageModel(gpt).build();
        }

        var q = "Considering this database: \n" + text + " What are the relations between the tables? (Remove duplicates)";
        Type listType = new TypeToken<List<Relations>>() {}.getType();

        BEGIN("Getting relations");

        String response;
        if(MockLayer.isActivated) {
            response = MOCK_RESPONSE;
        }
        else {
            var result = gptAssistant.getRelations(q);
            response = result.content().text();
        }
        String responseCode = extractTextMarkdownCode(response, "json");
        Gson gson = new Gson();
        return gson.fromJson(responseCode, listType);
    }

    static String extractTextMarkdownCode(String text, String language) {
        return text.substring(text.indexOf("```") + 3 + language.length(), text.lastIndexOf("```"));
    }

    public static final String MOCK_RESPONSE = """
            Based on the provided database schema, the relations between the tables can be summarized in the following JSON format:
            
            ```json
            [
                {"table_source": "aircraft", "table_target": "airline", "cardinality": "many-to-one"},
                {"table_source": "aircraft", "table_target": "manufacturer", "cardinality": "many-to-one"},
                {"table_source": "booking", "table_target": "flight", "cardinality": "many-to-one"},
                {"table_source": "booking", "table_target": "passenger", "cardinality": "many-to-one"},
                {"table_source": "flight", "table_target": "airport", "cardinality": "many-to-one"},
                {"table_source": "flight", "table_target": "aircraft", "cardinality": "many-to-one"},
                {"table_source": "flight", "table_target": "flight", "cardinality": "one-to-many (self-reference)"}
            ]
            ```
            
            ### Explanation of Relations:
            1. **aircraft to airline**: Each aircraft is associated with one airline, but an airline can have multiple aircraft (many-to-one).
            2. **aircraft to manufacturer**: Each aircraft is manufactured by one manufacturer, but a manufacturer can produce multiple aircraft (many-to-one).
            3. **booking to flight**: Each booking is for one flight, but a flight can have multiple bookings (many-to-one).
            4. **booking to passenger**: Each booking is made by one passenger, but a passenger can have multiple bookings (many-to-one).
            5. **flight to airport**: Each flight departs from and arrives at one airport, but an airport can serve multiple flights (many-to-one).
            6. **flight to aircraft**: Each flight is operated by one aircraft, but an aircraft can operate multiple flights (many-to-one).
            7. **flight to flight**: A flight can connect to another flight, creating a self-reference where one flight can have multiple connections to other flights (one-to-many).
            """;

}
