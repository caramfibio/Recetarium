package com.example.recetarium2.data;

/**
 * Simple data holder for a recipe stored in the local DB.
 */
public class RecipeRecord {
    private final int year;
    private final int month;
    private final int day;
    private final String content;

    public RecipeRecord(int year, int month, int day, String content) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.content = content;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public String getContent() { return content; }
}

