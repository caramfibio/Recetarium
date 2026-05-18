package com.example.recetarium2.Infrastructure.persistence;

import com.example.recetarium2.Infrastructure.database.MealDao;
import com.example.recetarium2.application.usecase.MenuGeneratorService;
import com.example.recetarium2.domain.food.Dish;
import com.example.recetarium2.domain.food.FoodType;
import com.example.recetarium2.domain.food.Time;
import com.example.recetarium2.domain.meal.Meal;

import java.sql.SQLException;
import java.util.List;

/**
 * SQLite implementation of the {@link MenuGeneratorService.MealRepository} interface.
 * Provides access to meals stored in the SQLite database.
 */
public class SqliteMealRepository implements MenuGeneratorService.MealRepository {

    private final MealDao mealDao;

    public SqliteMealRepository() {
        this.mealDao = new MealDao();
    }

    @Override
    public List<Meal> findBy(FoodType foodType, Time time, Dish dish) {
        try {
            return mealDao.findBy(foodType, time, dish);
        } catch (SQLException e) {
            throw new RuntimeException("Error querying meals from database", e);
        }
    }
}

