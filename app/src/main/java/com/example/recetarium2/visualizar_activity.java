package com.example.recetarium2;

import android.content.Intent;
import android.os.Bundle;
// ...existing code...
import android.widget.Button;
import android.widget.Toast;

// ...existing code...

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class visualizar_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_visualizar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        Button btnCrear = findViewById(R.id.btnCrearReceta);
        Button btnVer = findViewById(R.id.btnVerRecetas);
        Button btnVolver = findViewById(R.id.btnVolverMain);

        btnCrear.setOnClickListener(v -> {
            Intent i = new Intent(visualizar_activity.this, formulario_recetarium.class);
            startActivity(i);
        });

        btnVer.setOnClickListener(v -> {
            Intent i = new Intent(visualizar_activity.this, lista_recetas_activity.class);
            startActivity(i);
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    

    // Genera un menú semanal aleatorio usando las recetas disponibles.
    // Inserta cada receta generada en el repositorio usando los viewId correspondientes
    private void generarMenuSemanal() {
        com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
        java.util.List<com.example.recetarium2.data.RecipeRecord> all = repo.getAllRecipes();
        if (all == null || all.isEmpty()) {
            Toast.makeText(this, "No hay recetas guardadas. Añade recetas antes de generar.", Toast.LENGTH_LONG).show();
            return;
        }

        // IDs de las casillas que representa cada día en dias_activity
        int[] ids = new int[]{R.id.tvLunes, R.id.tvMartes, R.id.tvMiercoles, R.id.tvJueves, R.id.tvViernes, R.id.tvSabado, R.id.tvDomingo};
        java.util.Random rnd = new java.util.Random();

        for (int vid : ids) {
            // Elegir una receta al azar (se permiten repeticiones)
            int idx = rnd.nextInt(all.size());
            com.example.recetarium2.data.RecipeRecord r = all.get(idx);
            // Guardamos el contenido de la receta en la casilla correspondiente (vid)
            repo.saveRecipe(vid, r.getContent());
        }

        Toast.makeText(this, "Menú semanal generado (aleatorio)", Toast.LENGTH_SHORT).show();
        // Abrir la pantalla de selección de semana/días para ver el resultado
        android.content.Intent intent = new android.content.Intent(this, dias_activity.class);
        startActivity(intent);
    }
}