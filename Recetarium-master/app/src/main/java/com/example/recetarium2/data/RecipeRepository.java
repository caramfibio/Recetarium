package com.example.recetarium2.data;

import java.util.List;

public interface RecipeRepository {
    // Operate by view id (no date involved)
    String getRecipe(int viewId);
    void saveRecipe(int viewId, String content);
    int saveRecipeNew(String content);
    void deleteRecipe(int viewId);
    // Return all stored recipes as records
    List<RecipeRecord> getAllRecipes();
}

