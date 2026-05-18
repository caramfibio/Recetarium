package com.example.recetarium2.application.usecase;

import com.example.recetarium2.domain.food.Dish;
import com.example.recetarium2.domain.food.FoodType;
import com.example.recetarium2.domain.food.Time;
import com.example.recetarium2.domain.meal.Meal;
import com.example.recetarium2.domain.menu.DayOfWeek;
import com.example.recetarium2.domain.menu.Menu;
import com.example.recetarium2.domain.menu.MealSet;
import com.example.recetarium2.domain.preset.FoodTypeEntry;
import com.example.recetarium2.domain.preset.MenuPreset;
import com.example.recetarium2.domain.preset.SlotRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain Service responsible for generating a randomized {@link Menu}
 * from a {@link MenuPreset} and a pool of available {@link Meal}s.

 * Lives in the domain layer because the generation logic (shuffle, locked/cancelled
 * rules, MealSet assembly) is pure business logic — it does not depend on
 * any infrastructure or framework.

 * Generation steps:
 *   1. Expand the FoodType pool respecting quantities.
 *   2. Shuffle the pool and assign FoodTypes to FREE slots.
 *   3. For each active slot pick a random Meal filtered by FoodType + Time + Dish.
 *   4. Assemble MealSets and populate the Menu day by day.
 */
public class MenuGeneratorService {

    /**
     * Repository interface defined in the domain — implemented in the data layer.
     * Follows the Dependency Inversion Principle.
     */
    public interface MealRepository {
        /**
         * Returns all meals matching the given filters.
         *
         * @param foodType the required food type
         * @param time     the required time of day
         * @param dish     the required dish position
         * @return list of matching meals, may be empty
         */
        List<Meal> findBy(FoodType foodType, Time time, Dish dish);
    }

    private final MealRepository mealRepository;

    /**
     * @param mealRepository source of available meals, injected via DI
     */
    public MenuGeneratorService(MealRepository mealRepository) {
        if (mealRepository == null)
            throw new IllegalArgumentException("MealRepository cannot be null");
        this.mealRepository = mealRepository;
    }

    /**
     * Generates a new {@link Menu} from the given preset.
     *
     * @param preset   the generation rules and FoodType pool
     * @param menuName display name for the resulting menu
     * @return a fully populated Menu
     * @throws IllegalStateException if the pool is smaller than the free slots,
     *                               or no meals are available for a given slot
     */
    public Menu generate(MenuPreset preset, String menuName) {

        // Step 1: expand pool and shuffle
        List<FoodType> pool = buildPool(preset.getFoodTypePool());
        Collections.shuffle(pool);

        // Step 2: assign shuffled FoodTypes to FREE slots
        List<SlotRule> freeSlots = preset.getSlotRules().stream()
                .filter(SlotRule::isFree)
                .collect(java.util.stream.Collectors.toList());

        if (pool.size() < freeSlots.size())
            throw new IllegalStateException(
                    "FoodType pool (" + pool.size() + ") has fewer entries " +
                    "than free slots (" + freeSlots.size() + ")");

        for (int i = 0; i < freeSlots.size(); i++) {
            freeSlots.get(i).assignFoodType(pool.get(i));
        }

        // Step 3 & 4: build the Menu
        Menu menu = new Menu(UUID.randomUUID().toString(), menuName);

        Map<DayOfWeek, Map<Time, List<SlotRule>>> grouped =
                preset.getSlotRules().stream()
                        .filter(r -> !r.isCancelled())
                        .collect(java.util.stream.Collectors.groupingBy(
                                SlotRule::getDay,
                                java.util.stream.Collectors.groupingBy(SlotRule::getTime)
                        ));

        for (Map.Entry<DayOfWeek, Map<Time, List<SlotRule>>> dayEntry : grouped.entrySet()) {
            DayOfWeek day = dayEntry.getKey();

            for (Map.Entry<Time, List<SlotRule>> timeEntry : dayEntry.getValue().entrySet()) {
                Time time = timeEntry.getKey();
                List<SlotRule> slots = timeEntry.getValue();
                MealSet mealSet = assembleMealSet(slots);

                if (time == Time.LUNCH) {
                    menu.setLunch(day, mealSet);
                } else {
                    menu.setDinner(day, mealSet);
                }
            }
        }

        return menu;
    }

    /**
     * Expands FoodTypeEntries into a flat shuffleable list.
     * Example: [VEGETABLE x2, CEREAL x1] → [VEGETABLE, VEGETABLE, CEREAL]
     */
    private List<FoodType> buildPool(List<FoodTypeEntry> entries) {
        List<FoodType> pool = new ArrayList<>();
        for (FoodTypeEntry entry : entries) {
            for (int i = 0; i < entry.getQuantity(); i++) {
                pool.add(entry.getFoodType());
            }
        }
        return pool;
    }

    /**
     * Assembles a {@link MealSet} from the slot rules of a single (day + time).
     * Enforces: FIRST+SECOND together, MAIN alone.
     *
     * @param slots the rules for this (day + time) combination
     * @return the assembled MealSet
     * @throws IllegalStateException if the combination is invalid
     */
    private MealSet assembleMealSet(List<SlotRule> slots) {
        boolean hasMain   = slots.stream().anyMatch(s -> s.getDish() == Dish.MAIN);
        boolean hasFirst  = slots.stream().anyMatch(s -> s.getDish() == Dish.FIRST);
        boolean hasSecond = slots.stream().anyMatch(s -> s.getDish() == Dish.SECOND);

        if (hasMain) {
            SlotRule rule = slots.stream()
                    .filter(s -> s.getDish() == Dish.MAIN)
                    .findFirst().orElseThrow(() -> new IllegalStateException("MAIN slot not found"));
            return MealSet.ofMain(pickRandom(rule));
        }

        if (hasFirst && hasSecond) {
            SlotRule firstRule  = slots.stream().filter(s -> s.getDish() == Dish.FIRST)
                    .findFirst().orElseThrow(() -> new IllegalStateException("FIRST slot not found"));
            SlotRule secondRule = slots.stream().filter(s -> s.getDish() == Dish.SECOND)
                    .findFirst().orElseThrow(() -> new IllegalStateException("SECOND slot not found"));
            return MealSet.ofCourse(pickRandom(firstRule), pickRandom(secondRule));
        }

        throw new IllegalStateException(
                "Invalid slot combination — must be MAIN alone or FIRST+SECOND together. Slots: " + slots);
    }

    /**
     * Picks a random Meal from the repository matching the slot's FoodType, Time and Dish.
     *
     * @param rule the slot rule to satisfy
     * @return a randomly selected Meal
     * @throws IllegalStateException if no meals are available for this slot
     */
    private Meal pickRandom(SlotRule rule) {
        List<Meal> candidates = mealRepository.findBy(
                rule.getFoodType(),
                rule.getTime(),
                rule.getDish()
        );
        if (candidates == null || candidates.isEmpty())
            throw new IllegalStateException("No meals available for slot: " + rule);
        Collections.shuffle(candidates);
        return candidates.get(0);
    }
}
