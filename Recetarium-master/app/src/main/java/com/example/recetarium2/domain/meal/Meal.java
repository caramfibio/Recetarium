package com.example.recetarium2.domain.meal;

import com.example.recetarium2.domain.food.Dish;
import com.example.recetarium2.domain.food.FoodType;
import com.example.recetarium2.domain.food.Time;
import java.util.Objects;

/**
 * Represents a concrete dish in the domain.
 *
 * Identity is based solely on {@code id} — two Meal objects are equal
 * if they share the same id, regardless of other fields (DDD entity rule).
 */
public class Meal {

    /** Unique identifier. Immutable once assigned. */
    private final String id;

    /** Display name of the meal (e.g. "Lentil soup"). */
    private String name;

    /** The ingredient/nutritional category of this meal. */
    private FoodType foodType;

    /** The time of day this meal is suited for. */
    private Time time;

    /** The dish position this meal occupies within a MealSet. */
    private Dish dish;

    /**
     * Creates a new Meal with all required fields.
     *
     * @param id       unique identifier, must not be blank
     * @param name     display name, must not be blank
     * @param foodType nutritional category
     * @param time     intended time of day
     * @param dish     dish position within a MealSet
     */
    public Meal(String id, String name, FoodType foodType, Time time, Dish dish) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        if (foodType == null)
            throw new IllegalArgumentException("FoodType cannot be null");
        if (time == null)
            throw new IllegalArgumentException("Time cannot be null");
        if (dish == null)
            throw new IllegalArgumentException("Dish cannot be null");

        this.id       = id;
        this.name     = name;
        this.foodType = foodType;
        this.time     = time;
        this.dish     = dish;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public FoodType getFoodType() { return foodType; }
    public Time getTime()       { return time; }
    public Dish getDish()       { return dish; }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        this.name = name;
    }

    public void setFoodType(FoodType foodType) {
        if (foodType == null) throw new IllegalArgumentException("FoodType cannot be null");
        this.foodType = foodType;
    }

    public void setTime(Time time) {
        if (time == null) throw new IllegalArgumentException("Time cannot be null");
        this.time = time;
    }

    public void setDish(Dish dish) {
        if (dish == null) throw new IllegalArgumentException("Dish cannot be null");
        this.dish = dish;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meal)) return false;
        return Objects.equals(id, ((Meal) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Meal{id='" + id + "', name='" + name + "', foodType=" + foodType +
               ", time=" + time + ", dish=" + dish + "}";
    }
}
