import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
public class DocumentTest {
    @Id
    private String id;
    private String name;

}
