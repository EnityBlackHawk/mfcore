package org.mf.langchain;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ConvertToJavaFile {

    public static String MOCK = "```java\n" +
            "import lombok.Data;\n" +
            "import org.springframework.data.annotation.Id;\n" +
            "import org.springframework.data.mongodb.core.mapping.Document;\n" +
            "\n" +
            "@Data\n" +
            "@Document(collection = \"flightData\")\n" +
            "public class FlightData {\n" +
            "    @Id\n" +
            "    private String id;\n" +
            "    private List<Flight> flights;\n" +
            "}\n" +
            "\n" +
            "@Data\n" +
            "public class Flight {\n" +
            "    private String number;\n" +
            "    private int gate;\n" +
            "    private Date arrivalTimeScheduled;\n" +
            "    private Date arrivalTimeActual;\n" +
            "    private Date departureTimeScheduled;\n" +
            "    private Date departureTimeActual;\n" +
            "    private Airport airportTo;\n" +
            "    private Airport airportFrom;\n" +
            "    private Flight connectsTo;\n" +
            "    private Aircraft aircraft;\n" +
            "    private Airline airline;\n" +
            "}\n" +
            "\n" +
            "@Data\n" +
            "public class Airport {\n" +
            "    private String id;\n" +
            "    private String name;\n" +
            "    private String city;\n" +
            "    private String country;\n" +
            "}\n" +
            "\n" +
            "@Data\n" +
            "public class Aircraft {\n" +
            "    private int id;\n" +
            "    private int airlineId;\n" +
            "    private int manufacturerId;\n" +
            "    private int maxPassengers;\n" +
            "    private String registration;\n" +
            "    private String type;\n" +
            "}\n" +
            "\n" +
            "@Data\n" +
            "public class Airline {\n" +
            "    private int id;\n" +
            "    private String iata;\n" +
            "    private String icao;\n" +
            "    private String name;\n" +
            "}\n" +
            "\n" +
            "@Data\n" +
            "public class Manufacturer {\n" +
            "    private int id;\n" +
            "    private String name;\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "These Java classes are structured with Lombok annotations for getter, setter, and constructor generation. The classes are suitable for use with the Spring Data MongoDB framework, with embedded documents for Airport, Aircraft, Airline, and Manufacturer within the Flight object.";

    public static String MOCK_2 = "```java\n" +
            "import lombok.Data;\n" +
            "import org.springframework.data.annotation.Id;\n" +
            "import org.springframework.data.mongodb.core.mapping.Document;\n" +
            "\n" +
            "@Data\n" +
            "@Document\n" +
            "public class Aircraft {\n" +
            "    @Id\n" +
            "    private Integer id;\n" +
            "    private Integer airline;\n" +
            "    private Integer manufacturerId;\n" +
            "    private Integer maxPassengers;\n" +
            "    private String registration;\n" +
            "    private String type;\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "```java\n" +
            "import lombok.Data;\n" +
            "import org.springframework.data.annotation.Id;\n" +
            "import org.springframework.data.mongodb.core.mapping.Document;\n" +
            "\n" +
            "@Data\n" +
            "@Document\n" +
            "public class Airline {\n" +
            "    @Id\n" +
            "    private Integer id;\n" +
            "    private String iata;\n" +
            "    private String icao;\n" +
            "    private String name;\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "```java\n" +
            "import lombok.Data;\n" +
            "import org.springframework.data.annotation.Id;\n" +
            "import org.springframework.data.mongodb.core.mapping.Document;\n" +
            "\n" +
            "@Data\n" +
            "@Document\n" +
            "public class Airport {\n" +
            "    @Id\n" +
            "    private String id;\n" +
            "    private String city;\n" +
            "    private String country;\n" +
            "    private String name;\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "```java\n" +
            "import lombok.Data;\n" +
            "import org.springframework.data.annotation.Id;\n" +
            "import org.springframework.data.mongodb.core.mapping.DBRef;\n" +
            "import org.springframework.data.mongodb.core.mapping.Document;\n" +
            "\n" +
            "import java.util.Date;\n" +
            "\n" +
            "@Data\n" +
            "@Document\n" +
            "public class Flight {\n" +
            "    @Id\n" +
            "    private String number;\n" +
            "    private Integer aircraftId;\n" +
            "    private Integer gate;\n" +
            "    private Date arrivalTimeActual;\n" +
            "    private Date arrivalTimeScheduled;\n" +
            "    private Date departureTimeActual;\n" +
            "    private Date departureTimeScheduled;\n" +
            "    @DBRef\n" +
            "    private Airport airportFrom;\n" +
            "    @DBRef\n" +
            "    private Airport airportTo;\n" +
            "    @DBRef\n" +
            "    private Flight connectsTo;\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "```java\n" +
            "import lombok.Data;\n" +
            "import org.springframework.data.annotation.Id;\n" +
            "import org.springframework.data.mongodb.core.mapping.Document;\n" +
            "\n" +
            "@Data\n" +
            "@Document\n" +
            "public class Manufacturer {\n" +
            "    @Id\n" +
            "    private Integer id;\n" +
            "    private String name;\n" +
            "}\n" +
            "```\n";
//            "\n" +
//            "Classes Generated: Aircraft, Airline, Airport, Flight, Manufacturer.";


    public static String getFromFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path + "cache.txt"));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveToFile(String path, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "cache.txt"));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void toFile(String path, String _package, String content) {


        var contents = new ArrayList<String>();
        while(true){
            var start = content.indexOf("```java");
            if(start == -1)
                break;
            var start_2 = content.substring(start + 8);
            var end = start_2.indexOf("```");
            contents.add(start_2.substring(0, end));
            content = content.substring(start + 8 + end);
        }

        for(String c : contents){
            var classNameIndex = c.indexOf("class");
            var isInterface = classNameIndex == -1;
            if(isInterface) {
                classNameIndex = c.indexOf("interface");
            }
            var className = c.substring(classNameIndex + (isInterface ? 9 : 6), c.indexOf( isInterface ? "extends" : "{")).trim();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(path + className + ".java"));
                writer.write("package " + _package + ";\n\n");
                writer.write(c);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
