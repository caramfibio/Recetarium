package com.example.recetarium2.Infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite database connection and schema initialization.
 * This class is responsible for:
 * - Opening and closing database connections
 * - Creating the database schema on first run
 * - Switching between production and test databases
 */
public class DatabaseConnection {

    private static String DB_URL = "jdbc:sqlite:comida_database.db";
    private static Connection connection;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    /**
     * Gets or creates a connection to the SQLite database.
     * Creates the database schema if it doesn't exist.
     *
     * @return the database connection
     * @throws SQLException if connection fails
     */
    public static synchronized Connection getConnection() throws SQLException {
        // Check if we're in test mode and update URL accordingly
        String currentUrl = getEffectiveDatabaseUrl();
        
        // If URL has changed (e.g., from prod to test or vice versa), close old connection
        if (connection != null && !connection.isClosed() && !DB_URL.equals(currentUrl)) {
            connection.close();
            connection = null;
        }
        
        DB_URL = currentUrl;

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            initializeSchema();
        }
        return connection;
    }

    /**
     * Gets the effective database URL considering test mode.
     */
    private static String getEffectiveDatabaseUrl() {
        try {
            Class<?> testSetupClass = Class.forName("test_utils.TestDatabaseSetup");
            java.lang.reflect.Method getDatabaseUrlMethod = testSetupClass.getMethod("getDatabaseUrl");
            return (String) getDatabaseUrlMethod.invoke(null);
        } catch (Exception e) {
            // If TestDatabaseSetup is not available, use default URL
            return "jdbc:sqlite:comida_database.db";
        }
    }

    /**
     * Closes the database connection.
     *
     * @throws SQLException if closing fails
     */
    public static synchronized void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }

    /**
     * Initializes the database schema with all required tables.
     * Tables created:
     * - meals: stores individual meal recipes
     * - menu_presets: stores preset configurations
     * - preset_food_pool: stores the food type pool for each preset
     * - menus: stores generated weekly menus
     * - menu_day_meals: stores the meal assignments for each day
     *
     * @throws SQLException if schema creation fails
     */
    private static void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create meals table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS meals (" +
                "  id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  food_type TEXT NOT NULL," +
                "  time TEXT NOT NULL," +
                "  dish TEXT NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Create menu_presets table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS menu_presets (" +
                "  id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Create preset_slot_rules table (stores the slot rules for each preset)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS preset_slot_rules (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  preset_id TEXT NOT NULL," +
                "  day_of_week TEXT NOT NULL," +
                "  time TEXT NOT NULL," +
                "  dish TEXT NOT NULL," +
                "  food_type TEXT," +
                "  locked BOOLEAN DEFAULT 0," +
                "  cancelled BOOLEAN DEFAULT 0," +
                "  FOREIGN KEY (preset_id) REFERENCES menu_presets(id) ON DELETE CASCADE" +
                ")"
            );

            // Create preset_food_pool table (stores FoodTypeEntry for each preset)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS preset_food_pool (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  preset_id TEXT NOT NULL," +
                "  food_type TEXT NOT NULL," +
                "  quantity INTEGER NOT NULL," +
                "  FOREIGN KEY (preset_id) REFERENCES menu_presets(id) ON DELETE CASCADE" +
                ")"
            );

            // Create menus table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS menus (" +
                "  id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Create menu_meals table (stores meal assignments per day and time)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS menu_meals (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  menu_id TEXT NOT NULL," +
                "  day_of_week TEXT NOT NULL," +
                "  time TEXT NOT NULL," +
                "  meal_type TEXT NOT NULL," +
                "  meal_id TEXT NOT NULL," +
                "  FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE," +
                "  FOREIGN KEY (meal_id) REFERENCES meals(id)" +
                ")"
            );

            connection.commit();
        }
    }
}

