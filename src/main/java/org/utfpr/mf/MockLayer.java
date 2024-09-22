package org.utfpr.mf;

import java.util.Map;
import java.util.Optional;

public class MockLayer {


    public static boolean isActivated = false;

    public static String MOCK_LLM_RESPOSE = """
            To create a MongoDB structure based on your relational database schema, we'll need to embed documents instead of using references. This is a key aspect of NoSQL databases like MongoDB, and it often leads to better performance for read-heavy workloads.
            
            Below is the suggested structure and corresponding Java code for each class. Each class representing a MongoDB document and its repository interface is provided separately.\s
            
            ### Aircraft.java
            ```java
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "aircraft")
            public class Aircraft {
                @Id
                private String id;
                private String type;
                private Airline airline;
                private Manufacturer manufacturer;
                private String registration;
                private int maxPassengers;
            
                // Getters and Setters
            }
            ```
            
            ### Airline.java
            ```java
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "airline")
            public class Airline {
                @Id
                private String id;
                private String name;
            
                // Getters and Setters
            }
            ```
            
            ### Airport.java
            ```java
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "airport")
            public class Airport {
                @Id
                private String id;
                private String name;
                private String city;
                private String country;
            
                // Getters and Setters
            }
            ```
            
            ### Booking.java
            ```java
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "booking")
            public class Booking {
                @Id
                private String id;
                private String flight;
                private Passenger passenger;
                private String seat;
            
                // Getters and Setters
            }
            ```
            
            ### Flight.java
            ```java
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "flight")
            public class Flight {
                @Id
                private String number;
                private Airport airportFrom;
                private Airport airportTo;
                private String departureTimeScheduled;
                private String departureTimeActual;
                private String arrivalTimeScheduled;
                private String arrivalTimeActual;
                private int gate;
                private Aircraft aircraft;
                private String connectsTo;
            
                // Getters and Setters
            }
            ```
            
            ### Manufacturer.java
            ```java
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "manufacturer")
            public class Manufacturer {
                @Id
                private String id;
                private String name;
            
                // Getters and Setters
            }
            ```
            
            ### Passenger.java
            ```java
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "passenger")
            public class Passenger {
                @Id
                private String id;
                private String firstName;
                private String lastName;
                private String passportNumber;
            
                // Getters and Setters
            }
            ```
            
            ### AircraftRepository.java
            ```java
            import org.springframework.data.mongodb.repository.MongoRepository;
            
            public interface AircraftRepository extends MongoRepository<Aircraft, String> {
            }
            ```
            
            ### AirlineRepository.java
            ```java
            import org.springframework.data.mongodb.repository.MongoRepository;
            
            public interface AirlineRepository extends MongoRepository<Airline, String> {
            }
            ```
            
            ### AirportRepository.java
            ```java
            import org.springframework.data.mongodb.repository.MongoRepository;
            
            public interface AirportRepository extends MongoRepository<Airport, String> {
            }
            ```
            
            ### BookingRepository.java
            ```java
            import org.springframework.data.mongodb.repository.MongoRepository;
            
            public interface BookingRepository extends MongoRepository<Booking, String> {
            }
            ```
            
            ### FlightRepository.java
            ```java
            import org.springframework.data.mongodb.repository.MongoRepository;
            
            public interface FlightRepository extends MongoRepository<Flight, String> {
            }
            ```
            
            ### ManufacturerRepository.java
            ```java
            import org.springframework.data.mongodb.repository.MongoRepository;
            
            public interface ManufacturerRepository extends MongoRepository<Manufacturer, String> {
            }
            ```
            
            ### PassengerRepository.java
            ```java
            import org.springframework.data.mongodb.repository.MongoRepository;
            
            public interface PassengerRepository extends MongoRepository<Passenger, String> {
            }
            ```
            
            ### Key Considerations
            1. **Embedded Documents**: The structure above embeds related documents (e.g., `Airline` and `Manufacturer` in `Aircraft`, `Airport` in `Flight`, `Passenger` in `Booking`) directly within each main document.
            2. **Performance**: Embedding related data reduces the number of queries needed to retrieve information, significantly improving read performance in many scenarios.
            3. **Flexibility**: The model is flexible, allowing for complex queries and aggregations when necessary.
            4. **Spring Data MongoDB**: The repositories are designed to work seamlessly with Spring Data, allowing you to perform CRUD operations easily.
            
            Make sure to implement the getters and setters in each class as required for your specific use cases.
            """;

    public static final String MOCK_GENERATE_MODEL = """
            // Aircraft collection
            {
            \t"id": NumberInt,
            \t"type": String,
            \t"airline": {
            \t\t"id": NumberInt,
            \t\t"name": String
            \t},
            \t"manufacturer": {
            \t\t"id": NumberInt,
            \t\t"name": String
            \t},
            \t"registration": String,
            \t"max_passengers": NumberInt
            }

            // Airport collection
            {
            \t"id": String,
            \t"name": String,
            \t"city": String,
            \t"country": String
            }

            // Passenger collection
            {
            \t"id": NumberInt,
            \t"first_name": String,
            \t"last_name": String,
            \t"passport_number": String,
            \t"bookings": [
            \t\t{
            \t\t\t"flight_number": String,  // flight.number
            \t\t\t"seat": String
            \t\t}
            \t]
            }

            // Flight collection
            {
            \t"number": String,
            \t"airport_from": {
            \t\t"id": String,
            \t\t"name": String,
            \t\t"city": String,
            \t\t"country": String
            \t},
            \t"airport_to": {
            \t\t"id": String,
            \t\t"name": String,
            \t\t"city": String,
            \t\t"country": String
            \t},
            \t"departure_time_scheduled": Date,
            \t"departure_time_actual": Date,
            \t"arrival_time_scheduled": Date,
            \t"arrival_time_actual": Date,
            \t"gate": NumberInt,
            \t"aircraft": {
            \t\t"id": NumberInt,
            \t\t"type": String,
            \t\t"airline": {
            \t\t\t"id": NumberInt,
            \t\t\t"name": String
            \t\t}
            \t},
            \t"connects_to": String  // flight.number of connecting flight
            }""";

    public static final Map<String, String> MOCK_GENERATE_JAVA_CODE = Map.of( "Airline", """
            import org.springframework.data.annotation.Id;
            public class Airline {
                @Id
                private String id;
                private String name;
            }
            """,
            "Manufacturer",
            """
             import org.springframework.data.annotation.Id;
             public class Manufacturer {
                @Id
                private String id;
                private String name;
            }
            """,
            "Aircraft",
            """
            import org.springframework.data.mongodb.core.mapping.DBRef;
            import lombok.Data;
            @Data
            public class Aircraft {
                private String id;
                private String type;
                @DBRef
                private Airline airline;
                private Manufacturer manufacturer;
                private String registration;
                private int maxPassengers;
             }
            """);


//    public void init(ProcessStepName[] ... steps) {
//        isActivated = true;
//        var l = Arrays.stream(steps).toList();
//        if(l.contains(ProcessStepName.GENERATE_MODEL)) {
//            values.put(ProcessStepName.GENERATE_MODEL, MOCK_GENERATE_MODEL);
//        }
//        if(l.contains(ProcessStepName.GENERATE_JAVA_CODE)) {
//            values.put(ProcessStepName.GENERATE_JAVA_CODE, MOCK_GENERATE_JAVA_CODE);
//        }
//
//    }


}
