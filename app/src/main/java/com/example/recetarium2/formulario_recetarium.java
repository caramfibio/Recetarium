package com.example.recetarium2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
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

        // Spinners (dish, time, foodtype)
        Spinner spinnerDish = findViewById(R.id.spinnerDish);
        Spinner spinnerTime = findViewById(R.id.spinnerTime);
        Spinner spinnerFoodType = findViewById(R.id.spinnerFoodType);

        ArrayAdapter<String> adapterDish = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapterDish.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (com.example.recetarium2.domain.food.Dish d : com.example.recetarium2.domain.food.Dish.values()) {
            adapterDish.add(d.name());
        }
        spinnerDish.setAdapter(adapterDish);

        ArrayAdapter<String> adapterTime = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (com.example.recetarium2.domain.food.Time t : com.example.recetarium2.domain.food.Time.values()) {
            adapterTime.add(t.name());
        }
        spinnerTime.setAdapter(adapterTime);

        ArrayAdapter<String> adapterFoodType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapterFoodType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (com.example.recetarium2.domain.food.FoodType f : com.example.recetarium2.domain.food.FoodType.values()) {
            adapterFoodType.add(f.name());
        }
        spinnerFoodType.setAdapter(adapterFoodType);

        btnGuardar.setOnClickListener(v -> {
            String selectedDish = spinnerDish.getSelectedItem() != null ? spinnerDish.getSelectedItem().toString() : "";
            String selectedTime = spinnerTime.getSelectedItem() != null ? spinnerTime.getSelectedItem().toString() : "";
            String selectedFoodType = spinnerFoodType.getSelectedItem() != null ? spinnerFoodType.getSelectedItem().toString() : "";

            StringBuilder sb = new StringBuilder();
            sb.append(etNombre.getText().toString()).append("\n");
            sb.append("Etiquetas: ")
                    .append("Dish=").append(selectedDish).append(", ")
                    .append("Time=").append(selectedTime).append(", ")
                    .append("FoodType=").append(selectedFoodType).append("\n\n");
            sb.append(etPasos.getText().toString());
            String contenido = sb.toString();

            com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
            if (viewId >= 0) {
                // Update or insert with explicit viewId (e.g., from calendar day)
                repo.saveRecipe(viewId, contenido);
                Toast.makeText(this, getString(R.string.msg_receta_guardada), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // No viewId provided (la actividad se abrió desde la pantalla principal). Crear nueva fila y obtener viewId.
                int newViewId = repo.saveRecipeNew(contenido);
                if (newViewId == -1) {
                    Toast.makeText(this, "Error al crear receta", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.msg_receta_guardada) + " (id=" + newViewId + ")", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        btnVolver.setOnClickListener(v -> finish());
    }
}