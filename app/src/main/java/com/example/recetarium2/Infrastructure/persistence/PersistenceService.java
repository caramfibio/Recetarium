package com.example.recetarium2.Infrastructure.persistence;

import com.example.recetarium2.Infrastructure.database.MealDao;
import com.example.recetarium2.Infrastructure.database.MenuDao;
import com.example.recetarium2.Infrastructure.database.MenuPresetDao;
import com.example.recetarium2.domain.meal.Meal;
import com.example.recetarium2.domain.menu.Menu;
import com.example.recetarium2.domain.preset.MenuPreset;

import java.sql.SQLException;
import java.util.List;

/**
 * Application service that provides CRUD operations for all entities.
 * This service acts as the main interface between the application and the data layer.
 * Handles all database operations for Meals, Menus, and MenuPresets.
 */
public class PersistenceService {

    private final MealDao mealDao;
    private final MenuDao menuDao;
    private final MenuPresetDao menuPresetDao;

    public PersistenceService() {
        this.mealDao = new MealDao();
        this.menuDao = new MenuDao();
        this.menuPresetDao = new MenuPresetDao();
    }

    // ========== MEAL OPERATIONS ==========

    /**
     * Creates a new meal in the database.
     *
     * @param meal the meal to create
     * @throws RuntimeException if database operation fails
     */
    public void createMeal(Meal meal) {
        try {
            mealDao.create(meal);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating meal: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a meal by its id.
     *
     * @param id the meal's unique identifier
     * @return the Meal, or null if not found
     * @throws RuntimeException if database operation fails
     */
    public Meal getMealById(String id) {
        try {
            return mealDao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving meal: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all meals.
     *
     * @return list of all meals
     * @throws RuntimeException if database operation fails
     */
    public List<Meal> getAllMeals() {
        try {
            return mealDao.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all meals: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing meal.
     *
     * @param meal the meal with updated values
     * @throws RuntimeException if database operation fails
     */
    public void updateMeal(Meal meal) {
        try {
            mealDao.update(meal);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating meal: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a meal by its id.
     *
     * @param id the meal's unique identifier
     * @throws RuntimeException if database operation fails
     */
    public void deleteMeal(String id) {
        try {
            mealDao.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting meal: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes all meals.
     *
     * @throws RuntimeException if database operation fails
     */
    public void deleteAllMeals() {
        try {
            mealDao.deleteAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all meals: " + e.getMessage(), e);
        }
    }

    // ========== MENU PRESET OPERATIONS ==========

    /**
     * Creates a new menu preset with all its rules and food pool.
     *
     * @param preset the MenuPreset to create
     * @throws RuntimeException if database operation fails
     */
    public void createMenuPreset(MenuPreset preset) {
        try {
            menuPresetDao.create(preset);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating menu preset: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a menu preset by its id.
     * Includes all associated slot rules and food pool entries.
     *
     * @param id the preset's unique identifier
     * @return the MenuPreset, or null if not found
     * @throws RuntimeException if database operation fails
     */
    public MenuPreset getMenuPresetById(String id) {
        try {
            return menuPresetDao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving menu preset: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all menu presets.
     *
     * @return list of all MenuPresets
     * @throws RuntimeException if database operation fails
     */
    public List<MenuPreset> getAllMenuPresets() {
        try {
            return menuPresetDao.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all menu presets: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing menu preset.
     * This will update rules and food pool completely (delete and re-insert).
     *
     * @param preset the MenuPreset with updated values
     * @throws RuntimeException if database operation fails
     */
    public void updateMenuPreset(MenuPreset preset) {
        try {
            menuPresetDao.update(preset);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating menu preset: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a menu preset by its id.
     * Cascading delete will remove all associated data.
     *
     * @param id the preset's unique identifier
     * @throws RuntimeException if database operation fails
     */
    public void deleteMenuPreset(String id) {
        try {
            menuPresetDao.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting menu preset: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes all menu presets.
     *
     * @throws RuntimeException if database operation fails
     */
    public void deleteAllMenuPresets() {
        try {
            menuPresetDao.deleteAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all menu presets: " + e.getMessage(), e);
        }
    }

    // ========== MENU OPERATIONS ==========

    /**
     * Creates a new menu with all its daily meal assignments.
     *
     * @param menu the Menu to create
     * @throws RuntimeException if database operation fails
     */
    public void createMenu(Menu menu) {
        try {
            menuDao.create(menu);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating menu: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a menu by its id.
     * Includes all associated daily meal assignments.
     *
     * @param id the menu's unique identifier
     * @return the Menu, or null if not found
     * @throws RuntimeException if database operation fails
     */
    public Menu getMenuById(String id) {
        try {
            return menuDao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving menu: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all menus.
     *
     * @return list of all Menus
     * @throws RuntimeException if database operation fails
     */
    public List<Menu> getAllMenus() {
        try {
            return menuDao.findAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all menus: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing menu.
     * This will update all meal assignments completely.
     *
     * @param menu the Menu with updated values
     * @throws RuntimeException if database operation fails
     */
    public void updateMenu(Menu menu) {
        try {
            menuDao.update(menu);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating menu: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a menu by its id.
     * Cascading delete will remove all associated data.
     *
     * @param id the menu's unique identifier
     * @throws RuntimeException if database operation fails
     */
    public void deleteMenu(String id) {
        try {
            menuDao.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting menu: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes all menus.
     *
     * @throws RuntimeException if database operation fails
     */
    public void deleteAllMenus() {
        try {
            menuDao.deleteAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all menus: " + e.getMessage(), e);
        }
    }
}

