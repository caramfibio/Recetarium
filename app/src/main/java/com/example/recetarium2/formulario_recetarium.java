package com.example.recetarium2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class formulario_recetarium extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_recetarium);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Referencias a UI
        EditText etNombre = findViewById(R.id.etNombrePlato);
        EditText etPasos = findViewById(R.id.etPasosReceta);
        Button btnGuardar = findViewById(R.id.btnGuardarReceta);
        Button btnVolver = findViewById(R.id.btnVolver);

        // Leer extra viewId pasado desde dias_activity
        Intent intent = getIntent();
        final int viewId = intent.getIntExtra("viewId", -1);

        btnGuardar.setOnClickListener(v -> {
            String contenido = etNombre.getText().toString() + "\n" + etPasos.getText().toString();
            com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
            if (viewId < 0) {
                // Guardar como nueva receta: repo asignará un viewId automáticamente
                int assigned = repo.saveRecipeNew(contenido);
                if (assigned < 0) {
                    Toast.makeText(this, "Error al guardar receta", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                repo.saveRecipe(viewId, contenido);
            }
            Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnVolver.setOnClickListener(v -> finish());
    }
}