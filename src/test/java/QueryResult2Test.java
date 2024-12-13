import org.junit.jupiter.api.Test;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.model.Credentials;
import org.utfpr.mf.tools.DataImporter;
import org.utfpr.mf.tools.QueryResult2;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QueryResult2Test {


    @Test
    void asObject() throws SQLException {

        Credentials credentials = new Credentials(
                "jdbc:postgresql://localhost:5432/airport3",
                "admin",
                "admin");
        DbMetadata db = new DbMetadata(credentials, null);

        QueryResult2 result = DataImporter.Companion.runQuery("SELECT * FROM aircraft", db, QueryResult2.class);
        List<AircraftMongo> list = result.asObject(AircraftMongo.class);

        assertNotNull(list);

    }
}
