package com.example.recetarium2;

import android.os.Bundle;
import android.widget.Button;
import android.content.Context;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PresetsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_presets);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // New behavior: allow selecting a lunch and dinner recipe for each weekday
        int[] spinnerIds = new int[]{
                R.id.spinnerMonLunch, R.id.spinnerMonDinner,
                R.id.spinnerTueLunch, R.id.spinnerTueDinner,
                R.id.spinnerWedLunch, R.id.spinnerWedDinner,
                R.id.spinnerThuLunch, R.id.spinnerThuDinner,
                R.id.spinnerFriLunch, R.id.spinnerFriDinner,
                R.id.spinnerSatLunch, R.id.spinnerSatDinner,
                R.id.spinnerSunLunch, R.id.spinnerSunDinner
        };

        final com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
        java.util.List<com.example.recetarium2.data.RecipeRecord> all = repo.getAllRecipes();
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        // first entry = NONE
        labels.add("NONE"); ids.add(-1);
        if (all != null) {
            for (com.example.recetarium2.data.RecipeRecord r : all) {
                String name = extractName(r.getContent());
                labels.add(name == null || name.isEmpty() ? ("receta-" + r.getViewId()) : name);
                ids.add(r.getViewId());
            }
        }

        SharedPreferences prefs = getSharedPreferences("menu_presets_week", Context.MODE_PRIVATE);

        // Create adapters and attach to each spinner
        for (int sid : spinnerIds) {
            Spinner s = findViewById(sid);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (s != null) s.setAdapter(adapter);
            // restore selection from prefs if present
            if (s != null) {
                String key = getPrefKeyForSpinnerId(sid);
                int saved = prefs.getInt(key, -1);
                int pos = 0;
                for (int i = 0; i < ids.size(); i++) if (ids.get(i) == saved) { pos = i; break; }
                s.setSelection(pos);
            }
        }

        Button btnSave = findViewById(R.id.btnSavePresets);
        if (btnSave != null) btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor e = prefs.edit();
            for (int sid : spinnerIds) {
                Spinner s = findViewById(sid);
                if (s == null) continue;
                int selPos = s.getSelectedItemPosition();
                int selViewId = ids.get(selPos);
                String key = getPrefKeyForSpinnerId(sid);
                e.putInt(key, selViewId);
            }
            e.apply();
            Toast.makeText(this, "Presets guardados", Toast.LENGTH_SHORT).show();
            finish();
        });

        Button btnVolver = findViewById(R.id.btnVolver);
        if (btnVolver != null) btnVolver.setOnClickListener(v -> finish());
    }

    private static String extractName(String content) {
        if (content == null) return "";
        String[] lines = content.split("\\r?\\n");
        for (String l : lines) {
            if (l == null) continue;
            String t = l.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("Etiquetas:")) continue;
            return t;
        }
        return content.trim();
    }

    private String getPrefKeyForSpinnerId(int sid) {
        // Use if/else instead of switch because resource ids may not be compile-time constants
        if (sid == R.id.spinnerMonLunch) return "mon_lunch";
        if (sid == R.id.spinnerMonDinner) return "mon_dinner";
        if (sid == R.id.spinnerTueLunch) return "tue_lunch";
        if (sid == R.id.spinnerTueDinner) return "tue_dinner";
        if (sid == R.id.spinnerWedLunch) return "wed_lunch";
        if (sid == R.id.spinnerWedDinner) return "wed_dinner";
        if (sid == R.id.spinnerThuLunch) return "thu_lunch";
        if (sid == R.id.spinnerThuDinner) return "thu_dinner";
        if (sid == R.id.spinnerFriLunch) return "fri_lunch";
        if (sid == R.id.spinnerFriDinner) return "fri_dinner";
        if (sid == R.id.spinnerSatLunch) return "sat_lunch";
        if (sid == R.id.spinnerSatDinner) return "sat_dinner";
        if (sid == R.id.spinnerSunLunch) return "sun_lunch";
        if (sid == R.id.spinnerSunDinner) return "sun_dinner";
        return "unknown";
    }
}

