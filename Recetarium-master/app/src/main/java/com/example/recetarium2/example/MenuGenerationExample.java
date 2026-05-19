package com.example.recetarium2.example;

import com.example.recetarium2.Infrastructure.persistence.PersistenceService;
import com.example.recetarium2.Infrastructure.persistence.SqliteMealRepository;
import com.example.recetarium2.application.usecase.MenuGeneratorService;
import com.example.recetarium2.domain.food.Dish;
import com.example.recetarium2.domain.food.FoodType;
import com.example.recetarium2.domain.food.Time;
import com.example.recetarium2.domain.meal.Meal;
import com.example.recetarium2.domain.menu.DayOfWeek;
import com.example.recetarium2.domain.menu.Menu;
import com.example.recetarium2.domain.preset.FoodTypeEntry;
import com.example.recetarium2.domain.preset.MenuPreset;
import com.example.recetarium2.domain.preset.SlotRule;

/**
 * Complete example showing how to use the backend system:
 * 1. Create and store meals in the database
 * 2. Create and store menu presets
 * 3. Generate a menu from a preset
 * 4. Store the generated menu
 * 5. Retrieve and display the menu
 *
 * This example demonstrates the typical flow for a mobile app backend.
 */
public class MenuGenerationExample {

    public static void main(String[] args) {
        try {
            PersistenceService persistenceService = new PersistenceService();

            // ========== STEP 1: LOAD/CREATE MEALS ==========
            System.out.println("=== Loading/Creating Meals ===");
            
            // In a real scenario, these might be pre-loaded from a seed database
            // For this example, we'll create them
            Meal vegSoup = new Meal("m1", "Vegetable Soup", FoodType.VEGETABLE, Time.LUNCH, Dish.FIRST);
            Meal friedRice = new Meal("m2", "Fried Rice", FoodType.CEREAL, Time.LUNCH, Dish.SECOND);
            Meal grilledChicken = new Meal("m3", "Grilled Chicken", FoodType.MEAT, Time.DINNER, Dish.MAIN);
            Meal fishFillet = new Meal("m4", "Fish Fillet", FoodType.FISH, Time.DINNER, Dish.MAIN);
            
            persistenceService.createMeal(vegSoup);
            persistenceService.createMeal(friedRice);
            persistenceService.createMeal(grilledChicken);
            persistenceService.createMeal(fishFillet);
            
            System.out.println("✓ Meals created and stored in database");

            // ========== STEP 2: CREATE AND STORE A MENU PRESET ==========
            System.out.println("\n=== Creating Menu Preset ===");
            
            MenuPreset preset = new MenuPreset("preset1", "Balanced Weekly Menu");
            
            // Add slot rules for the week
            preset.addSlotRule(new SlotRule(DayOfWeek.MONDAY, Time.LUNCH, Dish.FIRST));
            preset.addSlotRule(new SlotRule(DayOfWeek.MONDAY, Time.LUNCH, Dish.SECOND));
            preset.addSlotRule(new SlotRule(DayOfWeek.MONDAY, Time.DINNER, Dish.MAIN));
            
            preset.addSlotRule(new SlotRule(DayOfWeek.TUESDAY, Time.LUNCH, Dish.FIRST));
            preset.addSlotRule(new SlotRule(DayOfWeek.TUESDAY, Time.LUNCH, Dish.SECOND));
            preset.addSlotRule(new SlotRule(DayOfWeek.TUESDAY, Time.DINNER, Dish.MAIN));
            
            // Add more days as needed...
            for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, 
                                                  DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY}) {
                preset.addSlotRule(new SlotRule(day, Time.LUNCH, Dish.FIRST));
                preset.addSlotRule(new SlotRule(day, Time.LUNCH, Dish.SECOND));
                preset.addSlotRule(new SlotRule(day, Time.DINNER, Dish.MAIN));
            }
            
            // Add food type pool
            preset.addFoodTypeEntry(new FoodTypeEntry(FoodType.VEGETABLE, 7));  // 7 first courses
            preset.addFoodTypeEntry(new FoodTypeEntry(FoodType.CEREAL, 7));     // 7 second courses
            preset.addFoodTypeEntry(new FoodTypeEntry(FoodType.MEAT, 4));       // 4 meat dinners
            preset.addFoodTypeEntry(new FoodTypeEntry(FoodType.FISH, 3));       // 3 fish dinners
            
            persistenceService.createMenuPreset(preset);
            
            System.out.println("✓ Menu preset created with:");
            System.out.println("  - " + preset.getSlotRules().size() + " slot rules");
            System.out.println("  - " + preset.getFoodTypePool().size() + " food type entries");

            // ========== STEP 3: GENERATE A MENU FROM PRESET ==========
            System.out.println("\n=== Generating Weekly Menu ===");
            
            // Create the meal repository that reads from database
            SqliteMealRepository mealRepository = new SqliteMealRepository();
            MenuGeneratorService generator = new MenuGeneratorService(mealRepository);
            
            // Generate the menu
            Menu generatedMenu = generator.generate(preset, "Week 1 - May 2026");
            
            System.out.println("✓ Menu generated successfully: " + generatedMenu.getId());

            // ========== STEP 4: STORE THE GENERATED MENU ==========
            System.out.println("\n=== Storing Generated Menu ===");
            
            persistenceService.createMenu(generatedMenu);
            
            System.out.println("✓ Menu stored in database");

            // ========== STEP 5: RETRIEVE AND DISPLAY ==========
            System.out.println("\n=== Displaying Weekly Menu ===");
            
            Menu retrievedMenu = persistenceService.getMenuById(generatedMenu.getId());
            
            if (retrievedMenu != null) {
                System.out.println("\nMenu: " + retrievedMenu.getName());
                System.out.println("ID: " + retrievedMenu.getId());
                
                for (DayOfWeek day : DayOfWeek.values()) {
                    System.out.println("\n" + day + ":");
                    
                    var dayMenu = retrievedMenu.getDay(day);
                    
                    if (dayMenu.getLunch() != null) {
                        System.out.print("  LUNCH: ");
                        if (dayMenu.getLunch().isCourse()) {
                            System.out.println(dayMenu.getLunch().getFirst().getName() + 
                                             " + " + dayMenu.getLunch().getSecond().getName());
                        } else {
                            System.out.println(dayMenu.getLunch().getMain().getName());
                        }
                    } else {
                        System.out.println("  LUNCH: Eating out");
                    }
                    
                    if (dayMenu.getDinner() != null) {
                        System.out.print("  DINNER: ");
                        if (dayMenu.getDinner().isCourse()) {
                            System.out.println(dayMenu.getDinner().getFirst().getName() + 
                                             " + " + dayMenu.getDinner().getSecond().getName());
                        } else {
                            System.out.println(dayMenu.getDinner().getMain().getName());
                        }
                    } else {
                        System.out.println("  DINNER: Eating out");
                    }
                }
            }

            // ========== BONUS: RETRIEVE ALL STORED DATA ==========
            System.out.println("\n=== Database Statistics ===");
            System.out.println("Total meals: " + persistenceService.getAllMeals().size());
            System.out.println("Total presets: " + persistenceService.getAllMenuPresets().size());
            System.out.println("Total menus: " + persistenceService.getAllMenus().size());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

