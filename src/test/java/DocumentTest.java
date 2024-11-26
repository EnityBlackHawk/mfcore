import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.utfpr.mf.annotation.FromRDB;

@Data
@AllArgsConstructor
public class DocumentTest {
    @Id
    private String id;
    @FromRDB(type = "String", typeClass = String.class, table = "document_test", column = "name")
    private String name;

}
