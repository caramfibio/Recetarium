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
    private static final int DB_VERSION = 3;

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_ID = "id";
    // Reemplazamos las columnas de fecha por una columna que identifica la casilla/view
    public static final String COL_VIEW_ID = "view_id";
    public static final String COL_CONTENT = "content";
    public static final String COL_CONTENT2 = "content2";

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
                COL_VIEW_ID + " INTEGER UNIQUE, " +
                COL_CONTENT + " TEXT, " +
                COL_CONTENT2 + " TEXT" +
                ");";
        db.execSQL(sql);
        // Índice/constraint en view_id (ya definido como UNIQUE en la columna)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_recipes_viewid ON " + TABLE_RECIPES + "(" + COL_VIEW_ID + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migración v1 -> v2: migramos los contenidos desde la tabla antigua con columnas (year,month,day)
        // a la nueva tabla basada en view_id. En A2 decidimos migrar solo el contenido (pierde la asociación fecha->view).
        if (oldVersion < 2) {
            // Nombre de columna antigua que buscamos
            final String OLD_COL_YEAR = "year";
            Cursor c = null;
            boolean hasOldSchema = false;
            try {
                c = db.rawQuery("PRAGMA table_info(" + TABLE_RECIPES + ")", null);
                while (c.moveToNext()) {
                    String colName = c.getString(c.getColumnIndexOrThrow("name"));
                    if (OLD_COL_YEAR.equals(colName)) {
                        hasOldSchema = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // ignore - assume no old schema
            } finally {
                if (c != null) c.close();
            }

            if (hasOldSchema) {
                db.beginTransaction();
                try {
                    // Renombrar la tabla existente a temporal
                    db.execSQL("ALTER TABLE " + TABLE_RECIPES + " RENAME TO old_recipes;");

                    // Crear nueva tabla con esquema view_id
                    String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            COL_VIEW_ID + " INTEGER UNIQUE, " +
                            COL_CONTENT + " TEXT" +
                            ");";
                    db.execSQL(sql);
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_recipes_viewid ON " + TABLE_RECIPES + "(" + COL_VIEW_ID + ");");

                    // Copiar solo el contenido de la tabla antigua a la nueva (view_id se autogenera)
                    db.execSQL("INSERT INTO " + TABLE_RECIPES + " (" + COL_CONTENT + ") SELECT " + COL_CONTENT + " FROM old_recipes;");
                    // Asignar view_id = id para las filas insertadas
                    db.execSQL("UPDATE " + TABLE_RECIPES + " SET " + COL_VIEW_ID + " = " + COL_ID + " WHERE " + COL_VIEW_ID + " IS NULL;");

                    // Borrar la tabla antigua
                    db.execSQL("DROP TABLE IF EXISTS old_recipes;");

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        }
        if (oldVersion < 3) {
            // Add second content column for storing two recipes per view
            try {
                db.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " + COL_CONTENT2 + " TEXT;");
            } catch (Exception e) {
                // ignore if column exists or operation fails
            }
        }
    }

    public String getRecipeContent(int viewId) {
        SQLiteDatabase db = getReadableDatabase();
        String content = "";
        Cursor c = null;
            try {
                c = db.query(TABLE_RECIPES, new String[]{COL_CONTENT, COL_CONTENT2}, COL_VIEW_ID + "=?",
                        new String[]{String.valueOf(viewId)}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    String c1 = c.getString(0);
                    String c2 = c.getString(1);
                    if (c2 != null && !c2.isEmpty()) {
                        content = (c1 == null ? "" : c1) + "\n\n" + c2;
                    } else {
                        content = c1;
                    }
                }
            } finally {
                if (c != null) c.close();
            }
        return content == null ? "" : content;
    }

    public void saveRecipeContent(int viewId, String content) {
        SQLiteDatabase db = getWritableDatabase();
        android.content.ContentValues vals = new android.content.ContentValues();
        vals.put(COL_VIEW_ID, viewId);
        // Support storing two recipes separated by a blank line
        if (content != null && content.contains("\n\n")) {
            String[] parts = content.split("\\n\\n", 2);
            vals.put(COL_CONTENT, parts[0]);
            vals.put(COL_CONTENT2, parts.length > 1 ? parts[1] : "");
        } else {
            vals.put(COL_CONTENT, content);
            vals.put(COL_CONTENT2, "");
        }
        // Primero intentamos actualizar
        int updated = db.update(TABLE_RECIPES, vals, COL_VIEW_ID + "=?",
                new String[]{String.valueOf(viewId)});
        if (updated == 0) {
            db.insert(TABLE_RECIPES, null, vals);
        }
    }

    public void deleteRecipe(int viewId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_RECIPES, COL_VIEW_ID + "=?",
                new String[]{String.valueOf(viewId)});
    }

    /**
     * Insert a new recipe with only content and assign a generated view_id (equal to the row id).
     * Returns the assigned viewId.
     */
    public int saveRecipeContentNew(String content) {
        SQLiteDatabase db = getWritableDatabase();
        android.content.ContentValues vals = new android.content.ContentValues();
        String c1 = "";
        String c2 = "";
        if (content != null && content.contains("\n\n")) {
            String[] parts = content.split("\\n\\n", 2);
            c1 = parts[0] == null ? "" : parts[0];
            c2 = parts.length > 1 && parts[1] != null ? parts[1] : "";
        } else if (content != null) {
            c1 = content;
            c2 = "";
        }

        // Avoid inserting duplicate recipes: if an identical (content,content2) already exists, return its viewId
        android.database.Cursor lookup = null;
        try {
            lookup = db.query(TABLE_RECIPES, new String[]{COL_ID, COL_VIEW_ID},
                    COL_CONTENT + "=? AND " + COL_CONTENT2 + "=?",
                    new String[]{c1, c2}, null, null, null);
            if (lookup != null && lookup.moveToFirst()) {
                int foundId = lookup.getInt(0);
                int foundViewId = -1;
                try {
                    foundViewId = lookup.getInt(1);
                } catch (Exception e) {
                    // ignore
                }
                return foundViewId > 0 ? foundViewId : foundId;
            }
        } finally {
            if (lookup != null) lookup.close();
        }

        vals.put(COL_CONTENT, c1);
        vals.put(COL_CONTENT2, c2);
        long newId = db.insert(TABLE_RECIPES, null, vals);
        if (newId == -1) return -1;
        // Set view_id = id for the new row
        android.content.ContentValues upd = new android.content.ContentValues();
        upd.put(COL_VIEW_ID, (int)newId);
        db.update(TABLE_RECIPES, upd, COL_ID + "=?", new String[]{String.valueOf(newId)});
        return (int)newId;
    }

    /**
     * Returns all recipes stored in the database.
     */
    public java.util.List<RecipeRecord> getAllRecipes() {
        java.util.List<RecipeRecord> out = new java.util.ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor c = null;
        try {
            c = db.query(TABLE_RECIPES, new String[]{COL_VIEW_ID, COL_CONTENT},
                    null, null, null, null, COL_VIEW_ID);
            if (c != null && c.moveToFirst()) {
                do {
                    int vid = c.getInt(0);
                    String content = c.getString(1);
                    out.add(new RecipeRecord(vid, content == null ? "" : content));
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
        }
        return out;
    }

    // Compatibility wrappers for old date-based API (keep behavior deterministic).
    public String getRecipeContent(int year, int month, int day) {
        int viewId = year * 10000 + month * 100 + day;
        return getRecipeContent(viewId);
    }

    public void saveRecipeContent(int year, int month, int day, String content) {
        int viewId = year * 10000 + month * 100 + day;
        saveRecipeContent(viewId, content);
    }

    public void deleteRecipe(int year, int month, int day) {
        int viewId = year * 10000 + month * 100 + day;
        deleteRecipe(viewId);
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


