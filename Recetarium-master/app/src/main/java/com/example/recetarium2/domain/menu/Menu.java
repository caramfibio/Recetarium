package com.example.recetarium2.domain.menu;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate root of the menu domain.
 *
 * A Menu represents a full weekly meal plan, organized by day of the week.
 * It always contains exactly 7 days (Monday through Sunday). Each day holds
 * an independent lunch and dinner slot, either of which can be null (eating out).
 *
 * This class is the only entry point for modifying the week plan — the internal
 * map is never exposed for direct modification.
 */
public class Menu {

    /** Unique identifier of this menu. Immutable once set. */
    private final String id;

    /** Display name of the menu (e.g. "Week 1 - Summer"). */
    private String name;

    /**
     * The weekly plan, keyed by day of the week.
     * Always contains all 7 days — initialized in the constructor.
     * Using EnumMap for efficient key lookup with enum keys.
     */
    private final Map<DayOfWeek, DayMenu> weekPlan;

    /**
     * Creates a new Menu with all 7 days initialized with no meals planned.
     *
     * @param id   unique identifier for this menu
     * @param name display name
     * @throws IllegalArgumentException if id or name are blank
     */
    public Menu(String id, String name) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");

        this.id = id;
        this.name = name;
        this.weekPlan = new EnumMap<>(DayOfWeek.class);

        // Initialize all 7 days with empty DayMenu (no meals = eating out by default)
        for (DayOfWeek day : DayOfWeek.values()) {
            weekPlan.put(day, new DayMenu(day));
        }
    }

    /**
     * Returns the DayMenu for the given day.
     * Never returns null — all 7 days are always present.
     *
     * @param day the day of the week
     * @return the DayMenu for that day
     */
    public DayMenu getDay(DayOfWeek day) {
        return weekPlan.get(day);
    }

    /**
     * Assigns a MealSet to lunch on the given day.
     *
     * @param day   the day to update
     * @param lunch the MealSet for lunch, or null to mark as eating out
     */
    public void setLunch(DayOfWeek day, MealSet lunch) {
        weekPlan.get(day).setLunch(lunch);
    }

    /**
     * Assigns a MealSet to dinner on the given day.
     *
     * @param day    the day to update
     * @param dinner the MealSet for dinner, or null to mark as eating out
     */
    public void setDinner(DayOfWeek day, MealSet dinner) {
        weekPlan.get(day).setDinner(dinner);
    }

    /**
     * Marks lunch on the given day as eating out (sets it to null).
     *
     * @param day the day to clear
     */
    public void clearLunch(DayOfWeek day) {
        weekPlan.get(day).setLunch(null);
    }

    /**
     * Marks dinner on the given day as eating out (sets it to null).
     *
     * @param day the day to clear
     */
    public void clearDinner(DayOfWeek day) {
        weekPlan.get(day).setDinner(null);
    }

    /**
     * Returns an unmodifiable view of the full week plan.
     * To modify individual days, use {@link #setLunch}, {@link #setDinner},
     * {@link #clearLunch} or {@link #clearDinner}.
     *
     * @return read-only map of the week plan
     */
    public Map<DayOfWeek, DayMenu> getWeekPlan() {
        return Collections.unmodifiableMap(weekPlan);
    }

    /** Returns the unique identifier of this menu. */
    public String getId() { return id; }

    /** Returns the display name of this menu. */
    public String getName() { return name; }

    /**
     * Updates the display name of this menu.
     *
     * @param name new name, must not be blank
     * @throws IllegalArgumentException if name is blank
     */
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        this.name = name;
    }

    /**
     * Two menus are equal if they share the same id,
     * regardless of their content (DDD entity identity rule).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Menu)) return false;
        return Objects.equals(id, ((Menu) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Menu{id='" + id + "', name='" + name + "', weekPlan=" + weekPlan + "}";
    }
}
