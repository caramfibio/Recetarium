package com.example.recetarium2.data;

/**
 * Simple data holder for a recipe stored in the local DB.
 */
public class RecipeRecord {
    // Identificador de la vista (sin fecha)
    private final int viewId;
    private final String content;

    public RecipeRecord(int viewId, String content) {
        this.viewId = viewId;
        this.content = content;
    }

    public int getViewId() { return viewId; }
    public String getContent() { return content; }
}

