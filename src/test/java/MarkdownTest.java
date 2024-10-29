import org.junit.jupiter.api.Test;
import org.utfpr.mf.markdown.MarkdownContent;
import org.utfpr.mf.markdown.MarkdownDocument;
import org.utfpr.mf.tools.QueryResult;

public class MarkdownTest {
    @Test
    void markdownTable() {

        MarkdownContent content = new MarkdownContent();

        content.addTitle3("Notas:");
        content.addPlainText("Notas dos alunos", '\n');
        content.addCodeBlock("""
                SELECT * FROM notas;
                """, "sql");

        QueryResult qr = new QueryResult("Nome", "Idade", "Nota");
        qr.addRow("Luan", "21", "10.0");
        qr.addRow("Maria", "18", "10.0");
        qr.addRow("Pedro", "19", "10.0");

        content.addTable(qr);

        MarkdownDocument doc = new MarkdownDocument("/home/luan/doc.md");
        doc.write(content);
    }

}
