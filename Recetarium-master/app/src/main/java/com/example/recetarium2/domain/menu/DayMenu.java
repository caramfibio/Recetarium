package com.example.recetarium2.domain.menu;

/**
 * Represents the meal plan for a single day of the week.
 *
 * Each day has two possible meals: lunch and dinner.
 * Either or both can be null, which means the user is eating out for that meal.
 * The day itself (DayOfWeek) is always present — nullability lives at the meal level.
 */
public class DayMenu {

    /** The day of the week this plan belongs to. Immutable. */
    private final DayOfWeek day;

    /**
     * The lunch plan for this day.
     * Null means the user is eating lunch outside.
     */
    private MealSet lunch;

    /**
     * The dinner plan for this day.
     * Null means the user is eating dinner outside.
     */
    private MealSet dinner;

    /**
     * Creates a DayMenu with no meals planned (both lunch and dinner set to eating out).
     *
     * @param day the day of the week
     * @throws IllegalArgumentException if day is null
     */
    public DayMenu(DayOfWeek day) {
        if (day == null)
            throw new IllegalArgumentException("Day cannot be null");
        this.day = day;
        this.lunch = null;
        this.dinner = null;
    }

    /**
     * Creates a DayMenu with specific lunch and dinner plans.
     * Either meal can be null to indicate eating out.
     *
     * @param day    the day of the week
     * @param lunch  the lunch MealSet, or null if eating out
     * @param dinner the dinner MealSet, or null if eating out
     * @throws IllegalArgumentException if day is null
     */
    public DayMenu(DayOfWeek day, MealSet lunch, MealSet dinner) {
        if (day == null)
            throw new IllegalArgumentException("Day cannot be null");
        this.day = day;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    /**
     * Returns true if lunch is not planned (eating out).
     */
    public boolean isLunchOut() { return lunch == null; }

    /**
     * Returns true if dinner is not planned (eating out).
     */
    public boolean isDinnerOut() { return dinner == null; }

    /**
     * Returns true if both lunch and dinner are not planned (full day eating out).
     */
    public boolean isFullDayOut() { return lunch == null && dinner == null; }

    /** Returns the day of the week. */
    public DayOfWeek getDay() { return day; }

    /**
     * Returns the lunch MealSet, or null if eating out.
     */
    public MealSet getLunch() { return lunch; }

    /**
     * Returns the dinner MealSet, or null if eating out.
     */
    public MealSet getDinner() { return dinner; }

    /**
     * Sets the lunch plan. Pass null to mark as eating out.
     *
     * @param lunch the MealSet for lunch, or null
     */
    public void setLunch(MealSet lunch) { this.lunch = lunch; }

    /**
     * Sets the dinner plan. Pass null to mark as eating out.
     *
     * @param dinner the MealSet for dinner, or null
     */
    public void setDinner(MealSet dinner) { this.dinner = dinner; }

    @Override
    public String toString() {
        return "DayMenu{" +
                "day=" + day +
                ", lunch=" + (lunch == null ? "eating out" : lunch) +
                ", dinner=" + (dinner == null ? "eating out" : dinner) +
                '}';
    }
}
