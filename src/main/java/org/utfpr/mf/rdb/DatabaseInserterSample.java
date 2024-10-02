package org.utfpr.mf.rdb;

import org.utfpr.mf.model.Credentials;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class DatabaseInserterSample {
    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;

    public DatabaseInserterSample(Credentials credentials)
    {
        this.DB_URL = credentials.getConnectionString();
        this.DB_USER = credentials.getUsername();
        this.DB_PASSWORD = credentials.getPassword();
    }

    public void insertData() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            insertManufacturers(connection);
            insertAirlines(connection);
            insertAircrafts(connection);
            insertAirports(connection);
            insertFlights(connection);
            insertPassengers(connection);
            insertBookings(connection);
            System.out.println("Data inserted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertManufacturers(Connection connection) throws SQLException {
        String sql = "INSERT INTO manufacturer (id, name) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 1000; i++) {
                statement.setInt(1, i);
                statement.setString(2, "Manufacturer " + i);
                statement.executeUpdate();
            }
        }
    }

    private void insertAirlines(Connection connection) throws SQLException {
        String sql = "INSERT INTO airline (id, name) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 1000; i++) {
                statement.setInt(1, i);
                statement.setString(2, "Airline " + i);
                statement.executeUpdate();
            }
        }
    }

    private void insertAircrafts(Connection connection) throws SQLException {
        String sql = "INSERT INTO aircraft (id, type, airline, manufacturer, registration, max_passengers) VALUES (?, ?, ?, ?, ?, ?)";
        Random random = new Random();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 1000; i++) {
                statement.setInt(1, i);
                statement.setString(2, "Type " + i);
                statement.setInt(3, random.nextInt(1000) + 1);
                statement.setInt(4, random.nextInt(1000) + 1);
                statement.setString(5, "REG" + i);
                statement.setInt(6, random.nextInt(500) + 50); // Passenger capacity between 50 and 550
                statement.executeUpdate();
            }
        }
    }

    private void insertAirports(Connection connection) throws SQLException {
        String sql = "INSERT INTO airport (id, name, city, country) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < 1000; i++) {
                statement.setString(1, String.format("%03d", i));
                statement.setString(2, "Airport " + i);
                statement.setString(3, "City " + i);
                statement.setString(4, "Country " + i);
                statement.executeUpdate();
            }
        }
    }

    private void insertFlights(Connection connection) throws SQLException {
        String sql = "INSERT INTO flight (number, airport_from, airport_to, departure_time_scheduled, departure_time_actual, arrival_time_scheduled, arrival_time_actual, gate, aircraft, connects_to) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Random random = new Random();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 1000; i++) {
                statement.setString(1, String.format("FL%04d",  i));
                statement.setString(2, String.format("%03d", random.nextInt(999) + 1));
                statement.setString(3, String.format("%03d", random.nextInt(999) + 1));
                statement.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
                statement.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                statement.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis() + 3600000));
                statement.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis() + 3600000 + 600000));
                statement.setInt(8, random.nextInt(50) + 1);
                statement.setInt(9, random.nextInt(1000) + 1);
                statement.setString(10, null); // Assuming no connections
                statement.executeUpdate();
            }
        }
    }

    private void insertPassengers(Connection connection) throws SQLException {
        String sql = "INSERT INTO passenger (id, first_name, last_name, passport_number) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 1000; i++) {
                statement.setInt(1, i);
                statement.setString(2, "FirstName " + i);
                statement.setString(3, "LastName " + i);
                statement.setString(4, "P" + String.format("%07d", i));
                statement.executeUpdate();
            }
        }
    }

    private void insertBookings(Connection connection) throws SQLException {
        String sql = "INSERT INTO booking (id, flight, passenger, seat) VALUES (?, ?, ?, ?)";
        Random random = new Random();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 1000; i++) {
                statement.setInt(1, i);
                statement.setString(2, String.format("FL%04d", random.nextInt(1000) + 1));
                statement.setInt(3, random.nextInt(1000) + 1);
                statement.setString(4, String.format("%02d%c", random.nextInt(30) + 1, (char) (random.nextInt(6) + 'A')));
                statement.executeUpdate();
            }
        }
    }
}
