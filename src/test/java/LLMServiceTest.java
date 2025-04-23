import org.junit.jupiter.api.Test;
import org.utfpr.mf.descriptor.LLMServiceDesc;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.llm.LLMResponseJsonSchema;
import org.utfpr.mf.llm.LLMService;
import org.utfpr.mf.markdown.MarkdownContent;
import org.utfpr.mf.markdown.MarkdownDocument;
import org.utfpr.mf.metadata.Column;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.metadata.Table;
import org.utfpr.mf.tools.CodeSession;
import org.utfpr.mf.tools.SqlDataType;

import java.util.List;

public class LLMServiceTest {

    @Test
    void getJsonSchema() {

        CodeSession.LastSet = System.out;

        LLMServiceDesc desc = new LLMServiceDesc();
        desc.llm_key = System.getenv("LLM_KEY");
        desc.model = "gpt-4o-mini";

        LLMService service = new LLMService(desc);
        Table tableCustomer = new Table("Customer",
                List.of(
                        new Column("id", SqlDataType.BIGINT, true, null, false),
                        new Column("name", SqlDataType.VARCHAR, false, null, false),
                        new Column("email", SqlDataType.VARCHAR, false, null, false)
                )
        );

        Table tableProduct = new Table("Product",
                List.of(
                        new Column("id", SqlDataType.BIGINT, true, null, false),
                        new Column("name", SqlDataType.VARCHAR, true, null, false),
                        new Column("price", SqlDataType.FLOAT, false, null, false)
                )
        );

        Table tableSale = new Table("Sale",
                List.of(
                        new Column("id", SqlDataType.BIGINT, true, null, false),
                        new Column("customerId",
                                SqlDataType.BIGINT,
                                false,
                                new Column.FkInfo("customerId", "id", "Customer"),
                                false),
                        new Column("productId",
                                SqlDataType.BIGINT,
                                false,
                                new Column.FkInfo("productId", "id", "Product"),
                                false),
                        new Column("quantity", SqlDataType.BIGINT, false, null, false)
                )
        );

        MarkdownContent sqlContent = new MarkdownContent();
        sqlContent.addTitle2("Table Customer");
        sqlContent.addCodeBlock(tableCustomer.toString(), "sql");
        sqlContent.addTitle2("Table Product");
        sqlContent.addCodeBlock(tableProduct.toString(), "sql");
        sqlContent.addTitle2("Table Sale");
        sqlContent.addCodeBlock(tableSale.toString(), "sql");

        MarkdownContent promptContent = new MarkdownContent();
        promptContent.addPlainText("Based on the relational database3 tables, create a MongoDB schema for the tables.", '\n');
        promptContent.addTitle2("Tables");
        promptContent.addPlainText(sqlContent.get(), '\n');

        LLMResponseJsonSchema resp = service.getJsonSchemaList(promptContent.get());

        System.out.println("Response: " + resp);

    }

}
