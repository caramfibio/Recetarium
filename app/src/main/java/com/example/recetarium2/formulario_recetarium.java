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

        // Leer extras (anio, mes, dia) pasados desde dias_activity
        Intent intent = getIntent();
        final int anio = intent.getIntExtra("anio", -1);
        final int mes = intent.getIntExtra("mes", -1);
        final int dia = intent.getIntExtra("dia", -1);

        btnGuardar.setOnClickListener(v -> {
            if (anio < 0 || mes < 0 || dia < 0) {
                Toast.makeText(this, "Fecha no válida", Toast.LENGTH_SHORT).show();
                return;
            }
            String contenido = etNombre.getText().toString() + "\n" + etPasos.getText().toString();
            com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
            repo.saveRecipe(anio, mes, dia, contenido);
            Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnVolver.setOnClickListener(v -> finish());
    }
}