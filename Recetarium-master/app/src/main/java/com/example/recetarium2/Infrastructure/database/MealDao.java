package com.example.recetarium2.Infrastructure.database;

import com.example.recetarium2.domain.food.Dish;
import com.example.recetarium2.domain.food.FoodType;
import com.example.recetarium2.domain.food.Time;
import com.example.recetarium2.domain.meal.Meal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link Meal} entities.
 * Provides CRUD operations and queries against the SQLite database.
 */
public class MealDao {

    /**
     * Inserts a new meal into the database.
     *
     * @param meal the meal to insert
     * @throws SQLException if database operation fails
     */
    public void create(Meal meal) throws SQLException {
        String sql = "INSERT INTO meals (id, name, food_type, time, dish) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, meal.getId());
            stmt.setString(2, meal.getName());
            stmt.setString(3, meal.getFoodType().name());
            stmt.setString(4, meal.getTime().name());
            stmt.setString(5, meal.getDish().name());
            stmt.executeUpdate();
        }
    }

    /**
     * Finds a meal by its unique identifier.
     *
     * @param id the meal's unique id
     * @return the Meal, or null if not found
     * @throws SQLException if database operation fails
     */
    public Meal findById(String id) throws SQLException {
        String sql = "SELECT * FROM meals WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMeal(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds all meals matching the given criteria.
     * At least one criterion must be non-null.
     *
     * @param foodType filter by food type (null to ignore)
     * @param time     filter by meal time (null to ignore)
     * @param dish     filter by dish position (null to ignore)
     * @return list of matching meals
     * @throws SQLException if database operation fails
     */
    public List<Meal> findBy(FoodType foodType, Time time, Dish dish) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM meals WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (foodType != null) {
            sql.append(" AND food_type = ?");
            params.add(foodType.name());
        }
        if (time != null) {
            sql.append(" AND time = ?");
            params.add(time.name());
        }
        if (dish != null) {
            sql.append(" AND dish = ?");
            params.add(dish.name());
        }

        List<Meal> meals = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    meals.add(mapRowToMeal(rs));
                }
            }
        }
        return meals;
    }

    /**
     * Returns all meals in the database.
     *
     * @return list of all meals
     * @throws SQLException if database operation fails
     */
    public List<Meal> findAll() throws SQLException {
        String sql = "SELECT * FROM meals";
        List<Meal> meals = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                meals.add(mapRowToMeal(rs));
            }
        }
        return meals;
    }

    /**
     * Updates an existing meal.
     *
     * @param meal the meal with updated values
     * @throws SQLException if database operation fails
     */
    public void update(Meal meal) throws SQLException {
        String sql = "UPDATE meals SET name = ?, food_type = ?, time = ?, dish = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, meal.getName());
            stmt.setString(2, meal.getFoodType().name());
            stmt.setString(3, meal.getTime().name());
            stmt.setString(4, meal.getDish().name());
            stmt.setString(5, meal.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a meal by its id.
     *
     * @param id the meal's unique id
     * @throws SQLException if database operation fails
     */
    public void deleteById(String id) throws SQLException {
        String sql = "DELETE FROM meals WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes all meals.
     *
     * @throws SQLException if database operation fails
     */
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM meals";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a ResultSet row to a Meal object.
     */
    private Meal mapRowToMeal(ResultSet rs) throws SQLException {
        return new Meal(
            rs.getString("id"),
            rs.getString("name"),
            FoodType.valueOf(rs.getString("food_type")),
            Time.valueOf(rs.getString("time")),
            Dish.valueOf(rs.getString("dish"))
        );
    }
}

