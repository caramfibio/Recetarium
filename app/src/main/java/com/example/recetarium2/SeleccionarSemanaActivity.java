package com.example.recetarium2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SeleccionarSemanaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_semanas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout layoutSemana1 = findViewById(R.id.layoutSemana1);
        LinearLayout layoutSemana2 = findViewById(R.id.layoutSemana2);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnGenerarSemana = findViewById(R.id.btnGenerarSemana);
        Button btnPresetsSemana = findViewById(R.id.btnPresetsSemana);

        // Al tocar la Semana 1 → abre dias_activity con el número de semana
        layoutSemana1.setOnClickListener(v -> {
            Intent intent = new Intent(this, dias_activity.class);
            intent.putExtra("semana", 1);
            startActivity(intent);
        });

        // Al tocar la Semana 2 → abre dias_activity con el número de semana
        layoutSemana2.setOnClickListener(v -> {
            Intent intent = new Intent(this, dias_activity.class);
            intent.putExtra("semana", 2);
            startActivity(intent);
        });

        // Botón Generar → genera menú semanal aleatorio y abre dias_activity
        if (btnGenerarSemana != null) btnGenerarSemana.setOnClickListener(v -> {
            com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
            java.util.List<com.example.recetarium2.data.RecipeRecord> all = repo.getAllRecipes();
            if (all == null || all.isEmpty()) {
                android.widget.Toast.makeText(this, "No hay recetas guardadas. Añade recetas antes de generar.", android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            int[] ids = new int[]{R.id.tvLunes, R.id.tvMartes, R.id.tvMiercoles, R.id.tvJueves, R.id.tvViernes, R.id.tvSabado, R.id.tvDomingo};
            java.util.Random rnd = new java.util.Random();
            for (int vid : ids) {
                int idx = rnd.nextInt(all.size());
                com.example.recetarium2.data.RecipeRecord r = all.get(idx);
                repo.saveRecipe(vid, r.getContent());
            }
            android.widget.Toast.makeText(this, "Menú semanal generado (aleatorio)", android.widget.Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, dias_activity.class);
            startActivity(intent);
        });

        // Botón Presets → abre PresetsActivity
        if (btnPresetsSemana != null) btnPresetsSemana.setOnClickListener(v -> {
            Intent intent = new Intent(this, PresetsActivity.class);
            startActivity(intent);
        });

        // Botón Volver → regresa a la pantalla anterior
        btnVolver.setOnClickListener(v -> finish());
    }
}