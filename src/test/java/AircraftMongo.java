import org.utfpr.mf.annotation.FromRDB;

public class AircraftMongo {
    @FromRDB(type = "java.lang.String", typeClass = String.class, column = "id", table = "aircraft")
    private String id;
    @FromRDB(type = "java.lang.String", typeClass = String.class, column = "type", table = "aircraft")
    private String type;
    @FromRDB(type = "java.lang.String", typeClass = String.class, column = "registration", table = "aircraft")
    private String registry;
    @FromRDB(type = "java.lang.String", typeClass = String.class, column = "max_passengers", table = "aircraft")
    private String max_passengers;
    @FromRDB(type = "Manufacturer",
            typeClass = Manufacturer.class,
            column = "id",
            table = "manufacturer",
            isReference = true
    )
    private Manufacturer manufacturer;

}
