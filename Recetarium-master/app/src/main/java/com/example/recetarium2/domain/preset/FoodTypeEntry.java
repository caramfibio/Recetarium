package com.example.recetarium2.domain.preset;

import com.example.recetarium2.domain.food.FoodType;

/**
 * Represents a single entry in the FoodType pool of a {@link MenuPreset}.
 *
 * Defines how many times a particular FoodType should appear across
 * the free slots of a weekly menu (e.g. VEGETABLE x2, CEREAL x1).
 */
public class FoodTypeEntry {

    /** The type of food this entry represents. */
    private final FoodType foodType;

    /**
     * How many times this FoodType should appear in the weekly menu.
     * Must be at least 1.
     */
    private int quantity;

    /**
     * @param foodType the type of food
     * @param quantity how many slots this FoodType covers (must be >= 1)
     */
    public FoodTypeEntry(FoodType foodType, int quantity) {
        if (foodType == null) throw new IllegalArgumentException("FoodType cannot be null");
        if (quantity < 1)     throw new IllegalArgumentException("Quantity must be at least 1");
        this.foodType = foodType;
        this.quantity = quantity;
    }

    public FoodType getFoodType() { return foodType; }
    public int getQuantity()      { return quantity; }

    public void setQuantity(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "FoodTypeEntry{foodType=" + foodType + ", quantity=" + quantity + "}";
    }
}
