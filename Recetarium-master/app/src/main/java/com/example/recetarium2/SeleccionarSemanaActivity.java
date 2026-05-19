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

        // Botón Volver → regresa a la pantalla anterior
        btnVolver.setOnClickListener(v -> finish());
    }
}