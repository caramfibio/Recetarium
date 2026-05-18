package com.example.recetarium2.data;

import android.content.Context;

public class RecipeRepositoryImpl implements RecipeRepository {
    private final RecetasDbHelper dbHelper;

    public RecipeRepositoryImpl(Context context) {
        this.dbHelper = new RecetasDbHelper(context.getApplicationContext());
    }

    @Override
    public String getRecipe(int year, int month, int day) {
        return dbHelper.getRecipeContent(year, month, day);
    }

    @Override
    public void saveRecipe(int year, int month, int day, String content) {
        dbHelper.saveRecipeContent(year, month, day, content);
    }

    @Override
    public void deleteRecipe(int year, int month, int day) {
        dbHelper.deleteRecipe(year, month, day);
    }

    @Override
    public java.util.List<RecipeRecord> getAllRecipes() {
        return dbHelper.getAllRecipes();
    }
}

