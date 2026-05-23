package com.pathfinder.database;

import com.pathfinder.model.City;
import com.pathfinder.model.Edge;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:graphdata.db";

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS cities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL, " +
                    "x_coord REAL NOT NULL, " +
                    "y_coord REAL NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS edges (" +
                    "source_id INTEGER, " +
                    "target_id INTEGER, " +
                    "distance REAL NOT NULL, " +
                    "PRIMARY KEY (source_id, target_id), " +
                    "FOREIGN KEY(source_id) REFERENCES cities(id), " +
                    "FOREIGN KEY(target_id) REFERENCES cities(id))");

            // Seed initial data if empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM cities");
            if (rs.next() && rs.getInt(1) == 0) {
                insertCity("JSSATE Campus", 200, 300);
                insertCity("Nandi Hills", 400, 100);
                insertCity("MG Road", 450, 350);
                insertCity("Mysuru", 100, 500);

                insertEdge(1, 2, 65.5);
                insertEdge(1, 3, 15.2);
                insertEdge(1, 4, 140.0);
                insertEdge(3, 2, 60.0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCity(String name, double x, double y) {
        String sql = "INSERT INTO cities(name, x_coord, y_coord) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, x);
            pstmt.setDouble(3, y);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("City might already exist or DB error.");
        }
    }

    public void insertEdge(int sourceId, int targetId, double distance) {
        String sql = "INSERT INTO edges(source_id, target_id, distance) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sourceId);
            pstmt.setInt(2, targetId);
            pstmt.setDouble(3, distance);
            pstmt.executeUpdate();

            // Assuming two-way roads
            pstmt.setInt(1, targetId);
            pstmt.setInt(2, sourceId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Edge might already exist.");
        }
    }

    public List<City> getAllCities() {
        List<City> cities = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM cities")) {
            while (rs.next()) {
                cities.add(new City(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("x_coord"), rs.getDouble("y_coord")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cities;
    }

    public List<Edge> getAllEdges() {
        List<Edge> edges = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM edges")) {
            while (rs.next()) {
                edges.add(new Edge(rs.getInt("source_id"), rs.getInt("target_id"), rs.getDouble("distance")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edges;
    }
}