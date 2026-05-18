package com.example.recetarium2.Infrastructure.database;

import com.example.recetarium2.domain.food.Dish;
import com.example.recetarium2.domain.food.FoodType;
import com.example.recetarium2.domain.food.Time;
import com.example.recetarium2.domain.menu.DayOfWeek;
import com.example.recetarium2.domain.preset.FoodTypeEntry;
import com.example.recetarium2.domain.preset.MenuPreset;
import com.example.recetarium2.domain.preset.SlotRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link MenuPreset} entities.
 * Provides CRUD operations and queries against the SQLite database.
 * Handles cascading persistence of related SlotRules and FoodTypeEntries.
 */
public class MenuPresetDao {

    /**
     * Inserts a new menu preset with all its slot rules and food pool entries.
     *
     * @param preset the MenuPreset to insert
     * @throws SQLException if database operation fails
     */
    public void create(MenuPreset preset) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert the preset itself
                String presetSql = "INSERT INTO menu_presets (id, name) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(presetSql)) {
                    stmt.setString(1, preset.getId());
                    stmt.setString(2, preset.getName());
                    stmt.executeUpdate();
                }

                // Insert all slot rules
                String ruleSql = "INSERT INTO preset_slot_rules (preset_id, day_of_week, time, dish, food_type, locked, cancelled) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(ruleSql)) {
                    for (SlotRule rule : preset.getSlotRules()) {
                        stmt.setString(1, preset.getId());
                        stmt.setString(2, rule.getDay().name());
                        stmt.setString(3, rule.getTime().name());
                        stmt.setString(4, rule.getDish().name());
                        stmt.setString(5, rule.getFoodType() != null ? rule.getFoodType().name() : null);
                        stmt.setBoolean(6, rule.isLocked());
                        stmt.setBoolean(7, rule.isCancelled());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                // Insert all food pool entries
                String poolSql = "INSERT INTO preset_food_pool (preset_id, food_type, quantity) " +
                                "VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(poolSql)) {
                    for (FoodTypeEntry entry : preset.getFoodTypePool()) {
                        stmt.setString(1, preset.getId());
                        stmt.setString(2, entry.getFoodType().name());
                        stmt.setInt(3, entry.getQuantity());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Finds a menu preset by its unique identifier.
     * Loads all associated slot rules and food pool entries.
     *
     * @param id the preset's unique id
     * @return the MenuPreset, or null if not found
     * @throws SQLException if database operation fails
     */
    public MenuPreset findById(String id) throws SQLException {
        String sql = "SELECT * FROM menu_presets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMenuPreset(conn, rs);
                }
            }
        }
        return null;
    }

    /**
     * Returns all menu presets in the database.
     *
     * @return list of all MenuPresets
     * @throws SQLException if database operation fails
     */
    public List<MenuPreset> findAll() throws SQLException {
        String sql = "SELECT * FROM menu_presets ORDER BY created_at DESC";
        List<MenuPreset> presets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                presets.add(mapRowToMenuPreset(conn, rs));
            }
        }
        return presets;
    }

    /**
     * Updates an existing menu preset and its related data.
     * Deletes and re-inserts all slot rules and food pool entries.
     *
     * @param preset the MenuPreset with updated values
     * @throws SQLException if database operation fails
     */
    public void update(MenuPreset preset) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update preset record
                String sql = "UPDATE menu_presets SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, preset.getName());
                    stmt.setString(2, preset.getId());
                    stmt.executeUpdate();
                }

                // Delete existing slot rules and food pool (cascading)
                deleteSlotRules(conn, preset.getId());
                deleteFoodPool(conn, preset.getId());

                // Re-insert updated slot rules
                String ruleSql = "INSERT INTO preset_slot_rules (preset_id, day_of_week, time, dish, food_type, locked, cancelled) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(ruleSql)) {
                    for (SlotRule rule : preset.getSlotRules()) {
                        stmt.setString(1, preset.getId());
                        stmt.setString(2, rule.getDay().name());
                        stmt.setString(3, rule.getTime().name());
                        stmt.setString(4, rule.getDish().name());
                        stmt.setString(5, rule.getFoodType() != null ? rule.getFoodType().name() : null);
                        stmt.setBoolean(6, rule.isLocked());
                        stmt.setBoolean(7, rule.isCancelled());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                // Re-insert updated food pool
                String poolSql = "INSERT INTO preset_food_pool (preset_id, food_type, quantity) " +
                                "VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(poolSql)) {
                    for (FoodTypeEntry entry : preset.getFoodTypePool()) {
                        stmt.setString(1, preset.getId());
                        stmt.setString(2, entry.getFoodType().name());
                        stmt.setInt(3, entry.getQuantity());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Deletes a menu preset by its id.
     * Cascading delete removes all associated slot rules and food pool entries.
     *
     * @param id the preset's unique id
     * @throws SQLException if database operation fails
     */
    public void deleteById(String id) throws SQLException {
        String sql = "DELETE FROM menu_presets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes all menu presets.
     *
     * @throws SQLException if database operation fails
     */
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM menu_presets";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a ResultSet row to a MenuPreset object, including loading
     * all associated slot rules and food pool entries.
     */
    private MenuPreset mapRowToMenuPreset(Connection conn, ResultSet rs) throws SQLException {
        String presetId = rs.getString("id");
        MenuPreset preset = new MenuPreset(presetId, rs.getString("name"));

        // Load all slot rules
        String ruleSql = "SELECT * FROM preset_slot_rules WHERE preset_id = ?";
        try (PreparedStatement ruleStmt = conn.prepareStatement(ruleSql)) {
            ruleStmt.setString(1, presetId);
            try (ResultSet ruleRs = ruleStmt.executeQuery()) {
                while (ruleRs.next()) {
                    SlotRule rule = mapRowToSlotRule(ruleRs);
                    preset.addSlotRule(rule);
                }
            }
        }

        // Load all food pool entries
        String poolSql = "SELECT * FROM preset_food_pool WHERE preset_id = ?";
        try (PreparedStatement poolStmt = conn.prepareStatement(poolSql)) {
            poolStmt.setString(1, presetId);
            try (ResultSet poolRs = poolStmt.executeQuery()) {
                while (poolRs.next()) {
                    FoodTypeEntry entry = mapRowToFoodTypeEntry(poolRs);
                    preset.addFoodTypeEntry(entry);
                }
            }
        }

        return preset;
    }

    /**
     * Maps a ResultSet row to a SlotRule object.
     */
    private SlotRule mapRowToSlotRule(ResultSet rs) throws SQLException {
        DayOfWeek day = DayOfWeek.valueOf(rs.getString("day_of_week"));
        Time time = Time.valueOf(rs.getString("time"));
        Dish dish = Dish.valueOf(rs.getString("dish"));
        String foodTypeStr = rs.getString("food_type");
        boolean locked = rs.getBoolean("locked");
        boolean cancelled = rs.getBoolean("cancelled");

        if (cancelled) {
            return SlotRule.cancelled(day, time, dish);
        } else if (locked && foodTypeStr != null) {
            FoodType foodType = FoodType.valueOf(foodTypeStr);
            return new SlotRule(day, time, dish, foodType);
        } else {
            return new SlotRule(day, time, dish);
        }
    }

    /**
     * Maps a ResultSet row to a FoodTypeEntry object.
     */
    private FoodTypeEntry mapRowToFoodTypeEntry(ResultSet rs) throws SQLException {
        FoodType foodType = FoodType.valueOf(rs.getString("food_type"));
        int quantity = rs.getInt("quantity");
        return new FoodTypeEntry(foodType, quantity);
    }

    /**
     * Deletes all slot rules for a preset.
     */
    private void deleteSlotRules(Connection conn, String presetId) throws SQLException {
        String sql = "DELETE FROM preset_slot_rules WHERE preset_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, presetId);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes all food pool entries for a preset.
     */
    private void deleteFoodPool(Connection conn, String presetId) throws SQLException {
        String sql = "DELETE FROM preset_food_pool WHERE preset_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, presetId);
            stmt.executeUpdate();
        }
    }
}

