import org.springframework.data.annotation.Id;
import org.utfpr.mf.annotation.FromRDB;

public class Manufacturer {

    @Id
    @FromRDB(type = "java.lang.String", typeClass = String.class, table = "manufacturer", column = "id")
    private String id;
    @FromRDB(type = "java.lang.String", typeClass = String.class, table = "manufacturer", column = "name")
    private String name;

}
