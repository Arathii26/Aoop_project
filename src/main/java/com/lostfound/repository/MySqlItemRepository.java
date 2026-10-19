package com.lostfound.repository;

import com.lostfound.enums.Category;
import com.lostfound.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySqlItemRepository implements ItemRepository {
    private final String url;
    private final String username;
    private final String password;

    public MySqlItemRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Item save(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        String sql = "INSERT INTO items (id, type, name, description, category, location, date_time, stored_at, reporter_id, resolved) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.getId());
            statement.setString(2, item instanceof LostItem ? "LOST" : "FOUND");
            statement.setString(3, item.getName());
            statement.setString(4, item.getDescription());
            statement.setString(5, item.getCategory().name());
            statement.setString(6, item.getLocation());
            statement.setTimestamp(7, Timestamp.valueOf(item.getDateTime()));
            statement.setTimestamp(8, item instanceof FoundItem foundItem && foundItem.getStoredAt() != null
                    ? Timestamp.valueOf(foundItem.getStoredAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            statement.setInt(9, item.getReporter().getId());
            statement.setBoolean(10, item.isResolved());
            statement.executeUpdate();
            return item;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save item to MySQL database.", e);
        }
    }

    @Override
    public Item findById(int id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to find item by id in MySQL database.", e);
        }
        return null;
    }

    @Override
    public List<Item> findAll() {
        String sql = "SELECT * FROM items";
        List<Item> items = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                items.add(mapRowToItem(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load items from MySQL database.", e);
        }
        return items;
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String type = rs.getString("type");
        String name = rs.getString("name");
        String description = rs.getString("description");
        Category category = Category.valueOf(rs.getString("category"));
        String location = rs.getString("location");
        LocalDateTime dateTime = rs.getTimestamp("date_time").toLocalDateTime();
        LocalDateTime storedAt = rs.getTimestamp("stored_at") != null ? rs.getTimestamp("stored_at").toLocalDateTime() : null;
        int reporterId = rs.getInt("reporter_id");
        boolean resolved = rs.getBoolean("resolved");

        User reporter = new Student();
        reporter.setId(reporterId);

        if ("LOST".equalsIgnoreCase(type)) {
            return new LostItem(id, name, description, category, location, dateTime, reporter, resolved);
        }
        FoundItem foundItem = new FoundItem(id, name, description, category, location, dateTime, reporter, resolved, storedAt);
        return foundItem;
    }
}
