package com.example.recetarium2.domain.food;

/**
 * Represents the position of a dish within a meal.
 *
 * Business rules:
 *   - FIRST and SECOND always appear together in a MealSet
 *   - MAIN always appears alone
 */
public enum Dish {
    FIRST,
    SECOND,
    MAIN
}
