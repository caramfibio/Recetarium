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

public class formulario_verReceta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_ver_receta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Referencias UI
        EditText etNombre = findViewById(R.id.etNombrePlato);
        EditText etPasos = findViewById(R.id.etPasosReceta);
        Button btnVolver = findViewById(R.id.btnVolver);

        Intent intent = getIntent();
        final int viewId = intent.getIntExtra("viewId", -1);

        if (viewId < 0) {
            Toast.makeText(this, "Elemento no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
        String contenido = repo.getRecipe(viewId);
        if (contenido == null || contenido.isEmpty()) {
            etNombre.setText("");
            etPasos.setText("");
            Toast.makeText(this, "No hay receta", Toast.LENGTH_SHORT).show();
        } else {
            // Separar nombre y pasos por la línea nueva que usamos al guardar
            String[] partes = contenido.split("\\n", 2);
            etNombre.setText(partes.length > 0 ? partes[0] : "");
            etPasos.setText(partes.length > 1 ? partes[1] : "");
        }

        // Evitar edición al ver
        etNombre.setEnabled(false);
        etPasos.setEnabled(false);

        btnVolver.setOnClickListener(v -> finish());
    }
}