package com.example.recetarium2.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecetasDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "RecetasDbHelper";
    private static final String DB_NAME = "recetas.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_ID = "id";
    public static final String COL_YEAR = "year";
    public static final String COL_MONTH = "month";
    public static final String COL_DAY = "day";
    public static final String COL_CONTENT = "content";

    private final Context context;

    public RecetasDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
        // Intentamos copiar una DB pre-existente desde el directorio proyecto (solo en entorno dev)
        tryCopyExternalDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_YEAR + " INTEGER NOT NULL, " +
                COL_MONTH + " INTEGER NOT NULL, " +
                COL_DAY + " INTEGER NOT NULL, " +
                COL_CONTENT + " TEXT" +
                ");";
        db.execSQL(sql);
        // Índice para búsquedas por fecha
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_recipes_fecha ON " + TABLE_RECIPES + "(" + COL_YEAR + "," + COL_MONTH + "," + COL_DAY + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En futuras versiones implementar migraciones
    }

    public String getRecipeContent(int year, int month, int day) {
        SQLiteDatabase db = getReadableDatabase();
        String content = "";
        Cursor c = null;
        try {
            c = db.query(TABLE_RECIPES, new String[]{COL_CONTENT}, COL_YEAR + "=? AND " + COL_MONTH + "=? AND " + COL_DAY + "=?",
                    new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(day)}, null, null, null);
            if (c != null && c.moveToFirst()) {
                content = c.getString(0);
            }
        } finally {
            if (c != null) c.close();
        }
        return content == null ? "" : content;
    }

    public void saveRecipeContent(int year, int month, int day, String content) {
        SQLiteDatabase db = getWritableDatabase();
        android.content.ContentValues vals = new android.content.ContentValues();
        vals.put(COL_YEAR, year);
        vals.put(COL_MONTH, month);
        vals.put(COL_DAY, day);
        vals.put(COL_CONTENT, content);
        // Primero intentamos actualizar
        int updated = db.update(TABLE_RECIPES, vals, COL_YEAR + "=? AND " + COL_MONTH + "=? AND " + COL_DAY + "=?",
                new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(day)});
        if (updated == 0) {
            db.insert(TABLE_RECIPES, null, vals);
        }
    }

    public void deleteRecipe(int year, int month, int day) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_RECIPES, COL_YEAR + "=? AND " + COL_MONTH + "=? AND " + COL_DAY + "=?",
                new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(day)});
    }

    /**
     * Returns all recipes stored in the database.
     */
    public java.util.List<RecipeRecord> getAllRecipes() {
        java.util.List<RecipeRecord> out = new java.util.ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor c = null;
        try {
            c = db.query(TABLE_RECIPES, new String[]{COL_YEAR, COL_MONTH, COL_DAY, COL_CONTENT},
                    null, null, null, null, COL_YEAR + "," + COL_MONTH + "," + COL_DAY);
            if (c != null && c.moveToFirst()) {
                do {
                    int y = c.getInt(0);
                    int m = c.getInt(1);
                    int d = c.getInt(2);
                    String content = c.getString(3);
                    out.add(new RecipeRecord(y, m, d, content == null ? "" : content));
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
        }
        return out;
    }

    // Intenta copiar una base de datos preexistente desde D:/comida/comida_database.db al path de la app
    private void tryCopyExternalDatabase() {
        File external = new File("D:/comida/comida_database.db");
        if (!external.exists()) return;

        File dbFile = context.getDatabasePath(DB_NAME);
        if (dbFile.exists() && dbFile.length() > 0) return; // ya existe

        dbFile.getParentFile().mkdirs();
        try (InputStream in = new FileInputStream(external);
             OutputStream out = new FileOutputStream(dbFile)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.flush();
            Log.i(TAG, "Base de datos copiada desde D:/comida/comida_database.db");
        } catch (IOException e) {
            Log.w(TAG, "No se pudo copiar DB externa: " + e.getMessage());
        }
    }
}


