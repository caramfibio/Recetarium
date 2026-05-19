package com.example.recetarium2.Infrastructure.database;

import com.example.recetarium2.domain.food.Time;
import com.example.recetarium2.domain.meal.Meal;
import com.example.recetarium2.domain.menu.DayMenu;
import com.example.recetarium2.domain.menu.DayOfWeek;
import com.example.recetarium2.domain.menu.Menu;
import com.example.recetarium2.domain.menu.MealSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link Menu} entities.
 * Provides CRUD operations and queries against the SQLite database.
 * Handles cascading persistence of daily meal assignments.
 */
public class MenuDao {

    /**
     * Inserts a new menu with all its daily meal assignments.
     *
     * @param menu the Menu to insert
     * @throws SQLException if database operation fails
     */
    public void create(Menu menu) throws SQLException {
        MealDao mealDao = new MealDao();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert the menu itself
                String menuSql = "INSERT INTO menus (id, name) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(menuSql)) {
                    stmt.setString(1, menu.getId());
                    stmt.setString(2, menu.getName());
                    stmt.executeUpdate();
                }

                // Insert all daily meals
                String mealSql = "INSERT INTO menu_meals (menu_id, day_of_week, time, meal_type, meal_id) " +
                                "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(mealSql)) {
                    for (DayOfWeek day : DayOfWeek.values()) {
                        DayMenu dayMenu = menu.getDay(day);

                        // Store lunch meals if present
                        if (dayMenu.getLunch() != null) {
                            insertMealSet(stmt, menu.getId(), day, Time.LUNCH, dayMenu.getLunch());
                        }

                        // Store dinner meals if present
                        if (dayMenu.getDinner() != null) {
                            insertMealSet(stmt, menu.getId(), day, Time.DINNER, dayMenu.getDinner());
                        }
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
     * Finds a menu by its unique identifier.
     * Loads all associated daily meal assignments.
     *
     * @param id the menu's unique id
     * @return the Menu, or null if not found
     * @throws SQLException if database operation fails
     */
    public Menu findById(String id) throws SQLException {
        String sql = "SELECT * FROM menus WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMenu(conn, rs);
                }
            }
        }
        return null;
    }

    /**
     * Returns all menus in the database.
     *
     * @return list of all Menus
     * @throws SQLException if database operation fails
     */
    public List<Menu> findAll() throws SQLException {
        String sql = "SELECT * FROM menus ORDER BY created_at DESC";
        List<Menu> menus = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                menus.add(mapRowToMenu(conn, rs));
            }
        }
        return menus;
    }

    /**
     * Updates an existing menu.
     * Name can be updated; meal assignments are managed separately via removal and re-creation.
     *
     * @param menu the Menu with updated values
     * @throws SQLException if database operation fails
     */
    public void update(Menu menu) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update menu record
                String sql = "UPDATE menus SET name = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, menu.getName());
                    stmt.setString(2, menu.getId());
                    stmt.executeUpdate();
                }

                // Delete and re-insert all meals
                deleteAllMenuMeals(conn, menu.getId());

                String mealSql = "INSERT INTO menu_meals (menu_id, day_of_week, time, meal_type, meal_id) " +
                                "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(mealSql)) {
                    for (DayOfWeek day : DayOfWeek.values()) {
                        DayMenu dayMenu = menu.getDay(day);

                        if (dayMenu.getLunch() != null) {
                            insertMealSet(stmt, menu.getId(), day, Time.LUNCH, dayMenu.getLunch());
                        }

                        if (dayMenu.getDinner() != null) {
                            insertMealSet(stmt, menu.getId(), day, Time.DINNER, dayMenu.getDinner());
                        }
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
     * Deletes a menu by its id.
     * Cascading delete removes all associated meal assignments.
     *
     * @param id the menu's unique id
     * @throws SQLException if database operation fails
     */
    public void deleteById(String id) throws SQLException {
        String sql = "DELETE FROM menus WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes all menus.
     *
     * @throws SQLException if database operation fails
     */
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM menus";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a ResultSet row to a Menu object, including loading
     * all associated daily meal assignments.
     */
    private Menu mapRowToMenu(Connection conn, ResultSet rs) throws SQLException {
        String menuId = rs.getString("id");
        Menu menu = new Menu(menuId, rs.getString("name"));

        // Load all meals for this menu
        String sql = "SELECT * FROM menu_meals WHERE menu_id = ? ORDER BY day_of_week, time";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, menuId);
            try (ResultSet mealRs = stmt.executeQuery()) {
                while (mealRs.next()) {
                    DayOfWeek day = DayOfWeek.valueOf(mealRs.getString("day_of_week"));
                    Time time = Time.valueOf(mealRs.getString("time"));
                    String mealType = mealRs.getString("meal_type");
                    String mealId = mealRs.getString("meal_id");

                    // Load the actual meal from database
                    MealDao mealDao = new MealDao();
                    Meal meal = mealDao.findById(mealId);
                    if (meal == null) continue; // Skip if meal not found

                    // Determine MealSet type based on meal_type
                    if ("COURSE_FIRST".equals(mealType) || "COURSE_SECOND".equals(mealType)) {
                        // We'll need to load both first and second meals together
                        loadAndAssignCourseMeals(conn, menu, day, time, mealId, "COURSE_FIRST".equals(mealType));
                        // Skip the second record when we encounter it
                        if ("COURSE_SECOND".equals(mealType)) {
                            continue;
                        }
                    } else if ("MAIN".equals(mealType)) {
                        menu.setLunch(day, MealSet.ofMain(meal));
                    }
                }
            }
        }

        return menu;
    }

    /**
     * Loads and assigns course meals (first + second) to a menu day.
     */
    private void loadAndAssignCourseMeals(Connection conn, Menu menu, DayOfWeek day, Time time, 
                                         String firstMealId, boolean isFirst) throws SQLException {
        MealDao mealDao = new MealDao();
        
        // Find the paired first and second meals
        String sql = "SELECT * FROM menu_meals WHERE menu_id = ? AND day_of_week = ? AND time = ? " +
                    "AND meal_type IN ('COURSE_FIRST', 'COURSE_SECOND') ORDER BY meal_type";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, menu.getId());
            stmt.setString(2, day.name());
            stmt.setString(3, time.name());
            try (ResultSet rs = stmt.executeQuery()) {
                Meal firstMeal = null;
                Meal secondMeal = null;

                while (rs.next()) {
                    String mealType = rs.getString("meal_type");
                    String mealId = rs.getString("meal_id");
                    Meal meal = mealDao.findById(mealId);
                    
                    if ("COURSE_FIRST".equals(mealType)) {
                        firstMeal = meal;
                    } else if ("COURSE_SECOND".equals(mealType)) {
                        secondMeal = meal;
                    }
                }

                if (firstMeal != null && secondMeal != null) {
                    MealSet mealSet = MealSet.ofCourse(firstMeal, secondMeal);
                    if (time == Time.LUNCH) {
                        menu.setLunch(day, mealSet);
                    } else {
                        menu.setDinner(day, mealSet);
                    }
                }
            }
        }
    }

    /**
     * Inserts meals from a MealSet into the prepared statement batch.
     */
    private void insertMealSet(PreparedStatement stmt, String menuId, DayOfWeek day, 
                              Time time, MealSet mealSet) throws SQLException {
        if (mealSet.isMain()) {
            stmt.setString(1, menuId);
            stmt.setString(2, day.name());
            stmt.setString(3, time.name());
            stmt.setString(4, "MAIN");
            stmt.setString(5, mealSet.getMain().getId());
            stmt.addBatch();
        } else if (mealSet.isCourse()) {
            // Insert first course
            stmt.setString(1, menuId);
            stmt.setString(2, day.name());
            stmt.setString(3, time.name());
            stmt.setString(4, "COURSE_FIRST");
            stmt.setString(5, mealSet.getFirst().getId());
            stmt.addBatch();

            // Insert second course
            stmt.setString(1, menuId);
            stmt.setString(2, day.name());
            stmt.setString(3, time.name());
            stmt.setString(4, "COURSE_SECOND");
            stmt.setString(5, mealSet.getSecond().getId());
            stmt.addBatch();
        }
    }

    /**
     * Deletes all meal assignments for a menu.
     */
    private void deleteAllMenuMeals(Connection conn, String menuId) throws SQLException {
        String sql = "DELETE FROM menu_meals WHERE menu_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, menuId);
            stmt.executeUpdate();
        }
    }
}


