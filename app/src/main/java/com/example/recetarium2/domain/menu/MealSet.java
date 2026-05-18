package com.example.recetarium2.domain.menu;

import com.example.recetarium2.domain.meal.Meal;

/**
 * Represents a valid combination of meals for a single eating occasion (lunch or dinner).
 *
 * According to business rules, only two combinations are allowed:
 *   - First + Second: a two-course meal (both are required together)
 *   - Main: a single standalone dish
 *
 * This class uses static factory methods to enforce those rules at construction time,
 * making it impossible to create an invalid MealSet.
 */
public class MealSet {

    private final Meal first;
    private final Meal second;
    private final Meal main;

    /**
     * Private constructor — use {@link #ofCourse(Meal, Meal)} or {@link #ofMain(Meal)} instead.
     */
    private MealSet(Meal first, Meal second, Meal main) {
        this.first = first;
        this.second = second;
        this.main = main;
    }

    /**
     * Creates a two-course MealSet with a First and a Second dish.
     * Both meals are mandatory — neither can be null.
     *
     * @param first  the first course
     * @param second the second course
     * @return a valid two-course MealSet
     * @throws IllegalArgumentException if either meal is null
     */
    public static MealSet ofCourse(Meal first, Meal second) {
        if (first == null || second == null)
            throw new IllegalArgumentException("First and Second are both required when combining courses");
        return new MealSet(first, second, null);
    }

    /**
     * Creates a standalone MealSet with a single Main dish.
     *
     * @param main the main dish
     * @return a valid single-course MealSet
     * @throws IllegalArgumentException if main is null
     */
    public static MealSet ofMain(Meal main) {
        if (main == null)
            throw new IllegalArgumentException("Main meal cannot be null");
        return new MealSet(null, null, main);
    }

    /**
     * Returns true if this set contains a standalone Main dish.
     */
    public boolean isMain() {
        return main != null;
    }

    /**
     * Returns true if this set contains a First + Second combination.
     */
    public boolean isCourse() {
        return first != null && second != null;
    }

    /**
     * Returns the first course, or null if this is a Main set.
     */
    public Meal getFirst() { return first; }

    /**
     * Returns the second course, or null if this is a Main set.
     */
    public Meal getSecond() { return second; }

    /**
     * Returns the main dish, or null if this is a two-course set.
     */
    public Meal getMain() { return main; }

    @Override
    public String toString() {
        if (isMain()) return "MealSet{main=" + main + "}";
        return "MealSet{first=" + first + ", second=" + second + "}";
    }
}
