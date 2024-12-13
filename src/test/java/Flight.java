import org.utfpr.mf.annotation.FromRDB;

public class Flight {

    @FromRDB(type = "java.lang.String", typeClass = String.class, column = "id", table = "flight")
    private String number;

}
