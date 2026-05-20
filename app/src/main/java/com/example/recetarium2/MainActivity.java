package com.example.recetarium2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnRecetariumMenu, btnHistorialMenu;

        btnRecetariumMenu = findViewById(R.id.BtnRecetarium);
        btnHistorialMenu = findViewById(R.id.BtnHistorial);
        final com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);



        // Botón principal: gestión de menús / recetas
        btnRecetariumMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre la pantalla de gestión/visualización de recetas y menús
                Intent intent = new Intent(MainActivity.this, visualizar_activity.class);
                startActivity(intent);
            }
        });

        // Botón secundario: menús semanales (selección de semana)
        btnHistorialMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                java.util.List<com.example.recetarium2.data.RecipeRecord> allRecipes = repo.getAllRecipes();
                int totalRecetas = allRecipes != null ? allRecipes.size() : 0;

                if (totalRecetas >= 14) {
                    Intent intent = new Intent(MainActivity.this, dias_activity.class);
                    startActivity(intent);
                }else{

                    Toast.makeText(MainActivity.this, String.format(java.util.Locale.getDefault(), "necesitas 14 recetas mas para generar recetas (%d/14)", totalRecetas), Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
    }