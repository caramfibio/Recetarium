package com.example.recetarium2.data;

import android.content.Context;

public class RecipeRepositoryImpl implements RecipeRepository {
    private final RecetasDbHelper dbHelper;

    public RecipeRepositoryImpl(Context context) {
        this.dbHelper = new RecetasDbHelper(context.getApplicationContext());
    }

    @Override
    public String getRecipe(int viewId) {
        return dbHelper.getRecipeContent(viewId);
    }

    @Override
    public void saveRecipe(int viewId, String content) {
        dbHelper.saveRecipeContent(viewId, content);
    }

    @Override
    public int saveRecipeNew(String content) {
        return dbHelper.saveRecipeContentNew(content);
    }

    @Override
    public void deleteRecipe(int viewId) {
        dbHelper.deleteRecipe(viewId);
    }

    @Override
    public java.util.List<RecipeRecord> getAllRecipes() {
        return dbHelper.getAllRecipes();
    }
}

