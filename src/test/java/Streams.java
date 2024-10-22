import org.junit.jupiter.api.Test;
import org.utfpr.mf.stream.FilePrintStream;
import org.utfpr.mf.stream.StringPrintStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Streams {

    @Test
    void stringPrintStream() {
        var printStream = new StringPrintStream();
        printStream.println("Hello, World!");

        assertEquals("Hello, World!\n", printStream.get());
    }

    @Test
    void filePrintStream() throws FileNotFoundException {
        var printStream = new FilePrintStream("test.txt");
        printStream.println("Hello, World!");

        String result = "";
        File myObj = new File("test.txt");
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            result = myReader.nextLine();
        }

        assertEquals("Hello, World!", result);
    }


}
