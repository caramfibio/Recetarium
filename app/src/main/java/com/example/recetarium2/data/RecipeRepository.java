package com.example.recetarium2.data;

import java.util.List;

public interface RecipeRepository {
    String getRecipe(int year, int month, int day);
    void saveRecipe(int year, int month, int day, String content);
    void deleteRecipe(int year, int month, int day);
    // Return all stored recipes as records
    List<RecipeRecord> getAllRecipes();
}

