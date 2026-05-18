package com.example.recetarium2.domain.preset;

import com.example.recetarium2.domain.food.Dish;
import com.example.recetarium2.domain.food.FoodType;
import com.example.recetarium2.domain.food.Time;
import com.example.recetarium2.domain.menu.DayOfWeek;

/**
 * Defines the rule for a single slot in the weekly menu.
 *
 * A slot is identified by three coordinates:
 *   - {@link DayOfWeek}: which day of the week
 *   - {@link Time}: lunch or dinner
 *   - {@link Dish}: first, second, or main
 *
 * Each slot can be in one of three states:
 *   - FREE: FoodType assigned by the generator during shuffle
 *   - LOCKED: FoodType is fixed and never shuffled
 *   - CANCELLED: eating out — no meal generated for this slot
 */
public class SlotRule {

    private final DayOfWeek day;
    private final Time time;
    private final Dish dish;

    /**
     * The FoodType assigned to this slot.
     * Null if FREE (not yet assigned) or CANCELLED.
     */
    private FoodType foodType;

    /** If true, foodType is fixed and will never be changed by the generator. */
    private final boolean locked;

    /** If true, this slot is eating out — no meal will be generated. */
    private final boolean cancelled;

    /**
     * Creates a FREE slot rule with no FoodType assigned yet.
     *
     * @param day  the day of the week
     * @param time lunch or dinner
     * @param dish first, second, or main
     */
    public SlotRule(DayOfWeek day, Time time, Dish dish) {
        if (day == null)  throw new IllegalArgumentException("Day cannot be null");
        if (time == null) throw new IllegalArgumentException("Time cannot be null");
        if (dish == null) throw new IllegalArgumentException("Dish cannot be null");
        this.day       = day;
        this.time      = time;
        this.dish      = dish;
        this.foodType  = null;
        this.locked    = false;
        this.cancelled = false;
    }

    /**
     * Creates a LOCKED slot rule with a fixed FoodType.
     *
     * @param day      the day of the week
     * @param time     lunch or dinner
     * @param dish     first, second, or main
     * @param foodType the fixed FoodType for this slot
     */
    public SlotRule(DayOfWeek day, Time time, Dish dish, FoodType foodType) {
        if (day == null)      throw new IllegalArgumentException("Day cannot be null");
        if (time == null)     throw new IllegalArgumentException("Time cannot be null");
        if (dish == null)     throw new IllegalArgumentException("Dish cannot be null");
        if (foodType == null) throw new IllegalArgumentException("A locked SlotRule must have a FoodType");
        this.day       = day;
        this.time      = time;
        this.dish      = dish;
        this.foodType  = foodType;
        this.locked    = true;
        this.cancelled = false;
    }

    /**
     * Creates a CANCELLED slot rule (eating out).
     * No meal will be generated for this slot.
     *
     * @param day  the day of the week
     * @param time lunch or dinner
     * @param dish first, second, or main
     * @return a cancelled SlotRule
     */
    public static SlotRule cancelled(DayOfWeek day, Time time, Dish dish) {
        return new SlotRule(day, time, dish, true);
    }

    /** Private constructor used only by {@link #cancelled}. */
    private SlotRule(DayOfWeek day, Time time, Dish dish, boolean cancelled) {
        this.day       = day;
        this.time      = time;
        this.dish      = dish;
        this.foodType  = null;
        this.locked    = false;
        this.cancelled = cancelled;
    }

    /**
     * Assigns a FoodType to this slot during the shuffle process.
     * Only valid on FREE slots.
     *
     * @param foodType the FoodType to assign
     * @throws IllegalStateException if the slot is locked or cancelled
     */
    public void assignFoodType(FoodType foodType) {
        if (locked)    throw new IllegalStateException("Cannot reassign FoodType on a locked slot");
        if (cancelled) throw new IllegalStateException("Cannot assign FoodType to a cancelled slot");
        this.foodType = foodType;
    }

    public DayOfWeek getDay()     { return day; }
    public Time getTime()         { return time; }
    public Dish getDish()         { return dish; }
    public FoodType getFoodType() { return foodType; }
    public boolean isLocked()     { return locked; }
    public boolean isCancelled()  { return cancelled; }
    public boolean isFree()       { return !locked && !cancelled; }

    @Override
    public String toString() {
        return "SlotRule{day=" + day + ", time=" + time + ", dish=" + dish +
               ", foodType=" + foodType + ", locked=" + locked + ", cancelled=" + cancelled + "}";
    }
}
