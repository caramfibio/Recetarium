package com.example.recetarium2;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetarium2.data.RecipeRecord;
import com.example.recetarium2.data.RecipeRepository;
import com.example.recetarium2.data.RecipeRepositoryImpl;

import java.util.List;

public class lista_recetas_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_recetas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        android.widget.Button btnVolverLista = findViewById(R.id.btnVolverLista);
        if (btnVolverLista != null) btnVolverLista.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvRecetas);
        rv.setLayoutManager(new LinearLayoutManager(this));

        RecipeRepository repo = new RecipeRepositoryImpl(this);
        List<RecipeRecord> recipes = repo.getAllRecipes();
        RecipeAdapter adapter = new RecipeAdapter(recipes);
        rv.setAdapter(adapter);
    }
}

