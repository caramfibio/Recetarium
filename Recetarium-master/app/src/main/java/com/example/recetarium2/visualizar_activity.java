package com.example.recetarium2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

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
        Button btnCrear = findViewById(R.id.btnCrearReceta);
        Button btnVer = findViewById(R.id.btnVerRecetas);

        btnCrear.setOnClickListener(v -> {
            Intent i = new Intent(visualizar_activity.this, formulario_recetarium.class);
            startActivity(i);
        });

        btnVer.setOnClickListener(v -> {
            Intent i = new Intent(visualizar_activity.this, lista_recetas_activity.class);
            startActivity(i);
        });
    }
}