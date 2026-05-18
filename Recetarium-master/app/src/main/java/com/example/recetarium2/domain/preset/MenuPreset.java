package com.example.recetarium2.domain.preset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Defines the generation rules for a weekly Menu.
 *
 * A MenuPreset is composed of two parts:
 *
 *   1. {@code slotRules} — one rule per slot (day + mealTime + dishPosition),
 *      each either FREE, LOCKED (fixed FoodType), or CANCELLED (eating out).
 *
 *   2. {@code foodTypePool} — the list of FoodTypeEntries that defines which
 *      FoodTypes are available and how many times each must appear across
 *      the free slots of the generated menu.
 *
 * The MenuGenerator uses this preset to build a randomized Menu each week,
 * respecting locked slots and cancelled days.
 */
public class MenuPreset {

    /** Unique identifier of this preset. Immutable. */
    private final String id;

    /** Display name of this preset (e.g. "Healthy week", "Bulk season"). */
    private String name;

    /**
     * List of rules, one per active slot.
     * Cancelled slots are also included so the generator knows to skip them.
     */
    private final List<SlotRule> slotRules;

    /**
     * Pool of FoodTypes with their frequencies.
     * The generator expands this into a shuffleable list during generation.
     */
    private final List<FoodTypeEntry> foodTypePool;

    /**
     * Creates an empty MenuPreset with no rules or pool entries.
     * Use {@link #addSlotRule} and {@link #addFoodTypeEntry} to build it up.
     *
     * @param id   unique identifier
     * @param name display name
     */
    public MenuPreset(String id, String name) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        this.id = id;
        this.name = name;
        this.slotRules = new ArrayList<>();
        this.foodTypePool = new ArrayList<>();
    }

    /**
     * Adds a SlotRule to this preset.
     * Each (day + mealTime + dishPosition) combination should appear at most once.
     *
     * @param rule the slot rule to add
     */
    public void addSlotRule(SlotRule rule) {
        if (rule == null)
            throw new IllegalArgumentException("SlotRule cannot be null");
        slotRules.add(rule);
    }

    /**
     * Adds a FoodTypeEntry to the pool.
     *
     * @param entry the entry to add
     */
    public void addFoodTypeEntry(FoodTypeEntry entry) {
        if (entry == null)
            throw new IllegalArgumentException("FoodTypeEntry cannot be null");
        foodTypePool.add(entry);
    }

    /**
     * Returns an unmodifiable view of the slot rules.
     */
    public List<SlotRule> getSlotRules() {
        return Collections.unmodifiableList(slotRules);
    }

    /**
     * Returns an unmodifiable view of the FoodType pool.
     */
    public List<FoodTypeEntry> getFoodTypePool() {
        return Collections.unmodifiableList(foodTypePool);
    }

    public String getId()   { return id; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank");
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuPreset)) return false;
        return Objects.equals(id, ((MenuPreset) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "MenuPreset{id='" + id + "', name='" + name +
                "', slotRules=" + slotRules +
                ", foodTypePool=" + foodTypePool + "}";
    }
}
