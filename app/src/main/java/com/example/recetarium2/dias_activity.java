package com.example.recetarium2;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class dias_activity extends AppCompatActivity {

    private String diaPulsado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dias);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencias a cada casilla de día
        TextView tvLunes     = findViewById(R.id.tvLunes);
        TextView tvMartes    = findViewById(R.id.tvMartes);
        TextView tvMiercoles = findViewById(R.id.tvMiercoles);
        TextView tvJueves    = findViewById(R.id.tvJueves);
        TextView tvViernes   = findViewById(R.id.tvViernes);
        TextView tvSabado    = findViewById(R.id.tvSabado);
        TextView tvDomingo   = findViewById(R.id.tvDomingo);
        Button   btnVolver   = findViewById(R.id.btnVolver);
        Button   btnGenerarSemana = findViewById(R.id.btnGenerarSemana);
        Button   btnPresetsSemana = findViewById(R.id.btnPresetsSemana);

        // Lista para iterar fácilmente
        TextView[] dias = new TextView[]{tvLunes, tvMartes, tvMiercoles, tvJueves, tvViernes, tvSabado, tvDomingo};

        // Leer extras para saber qué semana/mes se está mostrando
        int anio, mes, diaInicio;
        if (getIntent().hasExtra("anio") && getIntent().hasExtra("mes") && getIntent().hasExtra("diaInicio")) {
            anio = getIntent().getIntExtra("anio", Calendar.getInstance().get(Calendar.YEAR));
            mes = getIntent().getIntExtra("mes", Calendar.getInstance().get(Calendar.MONTH));
            diaInicio = getIntent().getIntExtra("diaInicio", 1);
        } else if (getIntent().hasExtra("semana")) {
            // Si se lanzó desde SeleccionarSemanaActivity sólo tenemos el número de semana
            int semana = getIntent().getIntExtra("semana", 1);
            Calendar hoy = Calendar.getInstance();
            anio = hoy.get(Calendar.YEAR);
            mes = hoy.get(Calendar.MONTH);
            // Simplificación: semana 1 → diaInicio=1, semana 2 → diaInicio=8, etc.
            diaInicio = 1 + (semana - 1) * 7;
        } else {
            // Por defecto, mostrar la primera semana del mes actual
            Calendar hoy = Calendar.getInstance();
            anio = hoy.get(Calendar.YEAR);
            mes = hoy.get(Calendar.MONTH);
            diaInicio = 1;
        }

        // Repositorio que gestiona recetas (SQLite)
        final com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
        // Cargamos todas las recetas en memoria para poder mostrar hasta 2 por día
        java.util.List<com.example.recetarium2.data.RecipeRecord> allRecipes = repo.getAllRecipes();
        java.util.Random globalRnd = new java.util.Random();

        int diasEnMes;
        Calendar cal = Calendar.getInstance();
        cal.set(anio, mes, 1);
        diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Rellena las casillas con la receta (si existe) y registra para menú contextual
        for (int i = 0; i < dias.length; i++) {
            TextView tv = dias[i];
            int diaNumero = diaInicio + i;
            if (diaNumero > diasEnMes) {
                tv.setText("");
                tv.setEnabled(false);
                continue;
            }
            // Identificamos la casilla por su id de vista (no por fecha)
            int viewId = tv.getId();
            String texto = repo.getRecipe(viewId);
            // Mostrar hasta dos recetas por día: si el contenido almacenado ya contiene dos recetas
            // (separadas por una línea en blanco), respetarlas; en caso contrario, rellenar con otras recetas.
            String nombre1 = null;
            String nombre2 = null;
            if (texto != null && !texto.isEmpty()) {
                if (texto.contains("\n\n")) {
                    String[] parts = texto.split("\\n\\n", 2);
                    nombre1 = extractName(parts[0]);
                    nombre2 = extractName(parts.length > 1 ? parts[1] : "");
                } else {
                    nombre1 = extractName(texto);
                }
            }

            if ((nombre1 == null || nombre2 == null) && allRecipes != null && !allRecipes.isEmpty()) {
                // Need to find additional recipes to show
                if (nombre1 == null) {
                    if (allRecipes.size() == 1) {
                        nombre1 = extractName(allRecipes.get(0).getContent());
                    } else {
                        int i1 = globalRnd.nextInt(allRecipes.size());
                        int i2 = globalRnd.nextInt(allRecipes.size());
                        while (i2 == i1 && allRecipes.size() > 1) i2 = globalRnd.nextInt(allRecipes.size());
                        nombre1 = extractName(allRecipes.get(i1).getContent());
                        nombre2 = extractName(allRecipes.get(i2).getContent());
                    }
                } else if (nombre2 == null) {
                    // pick a recipe different from nombre1
                    for (com.example.recetarium2.data.RecipeRecord r : allRecipes) {
                        if (r == null) continue;
                        String c = r.getContent();
                        if (c == null) continue;
                        String candidate = extractName(c);
                        if (!candidate.equals(nombre1)) { nombre2 = candidate; break; }
                    }
                    if (nombre2 == null && allRecipes.size() > 1) {
                        int tries = 0;
                        while (tries < 5) {
                            int idx = globalRnd.nextInt(allRecipes.size());
                            String c = allRecipes.get(idx).getContent();
                            String candidate = extractName(c);
                            if (!candidate.equals(nombre1)) { nombre2 = candidate; break; }
                            tries++;
                        }
                    }
                }
            }

            if (nombre1 == null && nombre2 == null) {
                tv.setText(getString(R.string.empty_text));
            } else if (nombre2 == null) {
                tv.setText(nombre1);
            } else if (nombre1 == null) {
                tv.setText(nombre2);
            } else {
                tv.setText(nombre1 + "\n" + nombre2);
            }
            tv.setTag(viewId); // guardamos datos para el menú
            registerForContextMenu(tv);
        }

        // Botón Generar → genera menú semanal (respeta presets guardados si existen)
        if (btnGenerarSemana != null) btnGenerarSemana.setOnClickListener(v -> {
            // Reuse the repository instance declared above
            java.util.List<com.example.recetarium2.data.RecipeRecord> all = repo.getAllRecipes();
            if (all == null || all.isEmpty()) {
                android.widget.Toast.makeText(this, getString(R.string.msg_no_recetas_guardadas), android.widget.Toast.LENGTH_LONG).show();
                return;
            }

            // Leer presets de SharedPreferences
            SharedPreferences prefs = getSharedPreferences("menu_presets", Context.MODE_PRIVATE);
            String presetDish = prefs.getString("preset_dish", "ANY");
            String presetTime = prefs.getString("preset_time", "ANY");
            String presetFoodType = prefs.getString("preset_foodtype", "ANY");

            // Filtrar recetas por etiquetas (el contenido incluye una línea "Etiquetas: Dish=..., Time=..., FoodType=...")
            java.util.List<com.example.recetarium2.data.RecipeRecord> candidate = new java.util.ArrayList<>();
            for (com.example.recetarium2.data.RecipeRecord r : all) {
                String c = r.getContent();
                boolean ok = true;
                // extraer etiquetas si existen
                int idx = c.indexOf("Etiquetas:");
                String etiquetas = idx >= 0 ? c.substring(idx) : "";
                if (presetDish != null && !presetDish.equals("ANY") && !presetDish.isEmpty()) {
                    if (!etiquetas.contains("Dish=" + presetDish)) ok = false;
                }
                if (presetTime != null && !presetTime.equals("ANY") && !presetTime.isEmpty()) {
                    if (!etiquetas.contains("Time=" + presetTime)) ok = false;
                }
                if (presetFoodType != null && !presetFoodType.equals("ANY") && !presetFoodType.isEmpty()) {
                    if (!etiquetas.contains("FoodType=" + presetFoodType)) ok = false;
                }
                if (ok) candidate.add(r);
            }

            java.util.List<com.example.recetarium2.data.RecipeRecord> pool = candidate.isEmpty() ? all : candidate;

            int[] ids = new int[]{R.id.tvLunes, R.id.tvMartes, R.id.tvMiercoles, R.id.tvJueves, R.id.tvViernes, R.id.tvSabado, R.id.tvDomingo};
            java.util.Random rnd = new java.util.Random();
            // read week presets (per-day lunch/dinner selections - now storing FoodType labels)
            SharedPreferences weekPrefs = getSharedPreferences("menu_presets_week", Context.MODE_PRIVATE);
            // keys mapping for each day
            String[] lunchKeys = new String[]{"mon_lunch","tue_lunch","wed_lunch","thu_lunch","fri_lunch","sat_lunch","sun_lunch"};
            String[] dinnerKeys = new String[]{"mon_dinner","tue_dinner","wed_dinner","thu_dinner","fri_dinner","sat_dinner","sun_dinner"};

            for (int dayIndex = 0; dayIndex < ids.length; dayIndex++) {
                int vid = ids[dayIndex];
                String presetLunchLabel = weekPrefs.getString(lunchKeys[dayIndex], "NONE");
                String presetDinnerLabel = weekPrefs.getString(dinnerKeys[dayIndex], "NONE");

                if ((!presetLunchLabel.equals("NONE") && !presetLunchLabel.isEmpty()) || 
                    (!presetDinnerLabel.equals("NONE") && !presetDinnerLabel.isEmpty())) {
                    // Filter pool for lunch and dinner based on FoodType labels
                    java.util.List<com.example.recetarium2.data.RecipeRecord> lunchCandidates = new java.util.ArrayList<>();
                    java.util.List<com.example.recetarium2.data.RecipeRecord> dinnerCandidates = new java.util.ArrayList<>();

                    for (com.example.recetarium2.data.RecipeRecord r : pool) {
                        String c = r.getContent();
                        int idx = c.indexOf("Etiquetas:");
                        String etiquetas = idx >= 0 ? c.substring(idx) : "";
                        
                        if (!presetLunchLabel.equals("NONE") && !presetLunchLabel.isEmpty()) {
                            if (etiquetas.contains("FoodType=" + presetLunchLabel)) {
                                lunchCandidates.add(r);
                            }
                        } else {
                            lunchCandidates.add(r);
                        }
                        
                        if (!presetDinnerLabel.equals("NONE") && !presetDinnerLabel.isEmpty()) {
                            if (etiquetas.contains("FoodType=" + presetDinnerLabel)) {
                                dinnerCandidates.add(r);
                            }
                        } else {
                            dinnerCandidates.add(r);
                        }
                    }

                    String lunchContent = "";
                    String dinnerContent = "";

                    if (!lunchCandidates.isEmpty()) {
                        com.example.recetarium2.data.RecipeRecord r = lunchCandidates.get(rnd.nextInt(lunchCandidates.size()));
                        lunchContent = r.getContent();
                    }
                    if (!dinnerCandidates.isEmpty() && (!lunchContent.isEmpty() || lunchCandidates.isEmpty())) {
                        // Pick from dinner candidates, avoiding the lunch pick
                        com.example.recetarium2.data.RecipeRecord r = null;
                        if (lunchContent.isEmpty()) {
                            r = dinnerCandidates.get(rnd.nextInt(dinnerCandidates.size()));
                        } else {
                            // Try to pick a different recipe
                            boolean found = false;
                            for (int attempts = 0; attempts < 5; attempts++) {
                                com.example.recetarium2.data.RecipeRecord dinnerRecipe = dinnerCandidates.get(rnd.nextInt(dinnerCandidates.size()));
                                String candidateName = extractName(dinnerRecipe.getContent());
                                String lunchName = extractName(lunchContent);
                                if (!candidateName.equals(lunchName)) {
                                    r = dinnerRecipe;
                                    found = true;
                                    break;
                                }
                            }
                            if (!found && !dinnerCandidates.isEmpty()) {
                                r = dinnerCandidates.get(0);
                            }
                        }
                        if (r != null) dinnerContent = r.getContent();
                    }

                    String combined = "";
                    if (lunchContent != null && !lunchContent.isEmpty()) combined = lunchContent;
                    if (dinnerContent != null && !dinnerContent.isEmpty()) {
                        if (!combined.isEmpty()) combined = combined + "\n\n" + dinnerContent;
                        else combined = dinnerContent;
                    }
                    if (combined.isEmpty()) {
                        // fallback to random selection from pool if no candidates matched the labels
                        if (!pool.isEmpty()) {
                            com.example.recetarium2.data.RecipeRecord r1 = pool.get(rnd.nextInt(pool.size()));
                            com.example.recetarium2.data.RecipeRecord r2 = pool.size() > 1 ? pool.get((rnd.nextInt(pool.size()))) : r1;
                            while (pool.size() > 1 && r2.getViewId() == r1.getViewId()) r2 = pool.get(rnd.nextInt(pool.size()));
                            combined = (r1.getContent() == null ? "" : r1.getContent()) + "\n\n" + (r2.getContent() == null ? "" : r2.getContent());
                        }
                    }
                    repo.saveRecipe(vid, combined);
                } else {
                    // no presets for this day -> use existing pool behavior
                    if (pool.size() == 1) {
                        com.example.recetarium2.data.RecipeRecord r = pool.get(0);
                        repo.saveRecipe(vid, r.getContent());
                    } else {
                        int i1 = rnd.nextInt(pool.size());
                        int i2 = rnd.nextInt(pool.size());
                        while (i2 == i1 && pool.size() > 1) i2 = rnd.nextInt(pool.size());
                        com.example.recetarium2.data.RecipeRecord r1 = pool.get(i1);
                        com.example.recetarium2.data.RecipeRecord r2 = pool.get(i2);
                        String combined = (r1.getContent() == null ? "" : r1.getContent()) + "\n\n" + (r2.getContent() == null ? "" : r2.getContent());
                        repo.saveRecipe(vid, combined);
                    }
                }
            }
            android.widget.Toast.makeText(this, getString(R.string.msg_menu_generado), android.widget.Toast.LENGTH_SHORT).show();
            // refrescar la actividad actual para mostrar cambios
            recreate();
        });

        // Botón Presets → abre PresetsActivity
        if (btnPresetsSemana != null) btnPresetsSemana.setOnClickListener(v -> {
            Intent intent = new Intent(this, PresetsActivity.class);
            startActivity(intent);
        });

        // Botón Volver
        btnVolver.setOnClickListener(v -> finish());
    }

    // Helper para extraer el nombre (primera línea) del contenido de una receta
    private static String extractName(String content) {
        if (content == null) return null;
        // Prefer first non-empty line that is not the Etiquetas: line
        String[] lines = content.split("\\r?\\n");
        for (String l : lines) {
            if (l == null) continue;
            String t = l.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("Etiquetas:") || t.startsWith("Etiquetas:") ) continue;
            return t;
        }
        // fallback: first line or whole content
        return lines.length > 0 ? lines[0].trim() : content.trim();
    }

    // Se llama justo antes de mostrar el menú contextual
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // El tag contiene el viewId (Integer)
        Object tag = v.getTag();
        if (tag instanceof Integer) {
            int viewId = (Integer) tag;
            diaPulsado = String.valueOf(viewId);
        } else {
            diaPulsado = "";
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dia, menu);
        menu.setHeaderTitle(diaPulsado);
    }

    // Se llama cuando el usuario elige una opción del menú
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        // diaPulsado contiene el viewId
        if (diaPulsado.isEmpty()) return super.onContextItemSelected(item);
        int viewId = Integer.parseInt(diaPulsado);

            if (id == R.id.menu_ver_receta) {
            Intent intent = new Intent(this, formulario_verReceta.class);
            intent.putExtra("viewId", viewId);
            startActivityForResult(intent, 1002);
            return true;

        } else if (id == R.id.menu_aniadir_receta) {
            Intent intent = new Intent(this, formulario_recetarium.class);
            intent.putExtra("viewId", viewId);
            startActivity(intent);
            return true;

        } else if (id == R.id.menu_borrar_receta) {
            // Usamos repo para borrar
            com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
            repo.deleteRecipe(viewId);
            // Actualiza la vista correspondiente
            int[] ids = new int[]{R.id.tvLunes, R.id.tvMartes, R.id.tvMiercoles, R.id.tvJueves, R.id.tvViernes, R.id.tvSabado, R.id.tvDomingo};
            for (int vid : ids) {
                TextView tv = findViewById(vid);
                Object tag = tv.getTag();
                // comparar por tag Integer si existe
                if (tag instanceof Integer) {
                    int tid = (Integer) tag;
                    if (tid == viewId) {
                        tv.setText(getString(R.string.empty_text));
                        break;
                    }
                } else if (tv.getId() == viewId) {
                    tv.setText(getString(R.string.empty_text));
                    break;
                }
            }
            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002 && resultCode == RESULT_OK) {
            // Refresh the view to reflect edits/deletions done in the view activity
            recreate();
        }
    }

    // Limpia el texto del día correspondiente
    private void borrarReceta(String dia) {
        int viewId;
        switch (dia) {
            case "Lunes":     viewId = R.id.tvLunes;     break;
            case "Martes":    viewId = R.id.tvMartes;    break;
            case "Miercoles": viewId = R.id.tvMiercoles; break;
            case "Jueves":    viewId = R.id.tvJueves;    break;
            case "Viernes":   viewId = R.id.tvViernes;   break;
            case "Sabado":    viewId = R.id.tvSabado;    break;
            case "Domingo":   viewId = R.id.tvDomingo;   break;
            default: return;
        }
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText("");
    }
}