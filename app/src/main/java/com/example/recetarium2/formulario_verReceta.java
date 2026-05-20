package com.example.recetarium2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
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
        // Referencias UI (read-only view)
        TextView etNombre = findViewById(R.id.etNombrePlato);
        TextView etPasos = findViewById(R.id.etPasosReceta);
        TextView tvDish = findViewById(R.id.tvDish);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvFoodType = findViewById(R.id.tvFoodType);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnEditar = findViewById(R.id.btnEditar);
        Button btnEliminar = findViewById(R.id.btnEliminar);

        Intent intent = getIntent();
        final int viewId = intent.getIntExtra("viewId", -1);

        if (viewId < 0) {
            Toast.makeText(this, getString(R.string.msg_viewid_invalid), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
        String contenido = repo.getRecipe(viewId);
        if (contenido == null || contenido.isEmpty()) {
            etNombre.setText("");
            etPasos.setText("");
            tvDish.setText("");
            tvTime.setText("");
            tvFoodType.setText("");
            Toast.makeText(this, getString(R.string.msg_no_hay_receta), Toast.LENGTH_SHORT).show();
        } else {
            // Nombre en la primera línea
            String[] lines = contenido.split("\\n");
            etNombre.setText(lines.length > 0 ? lines[0] : "");

            // Intentar extraer la línea de etiquetas que empieza por "Etiquetas:"
            String etiquetasLine = null;
            for (String l : lines) {
                if (l != null && l.trim().startsWith("Etiquetas:")) {
                    etiquetasLine = l.trim();
                    break;
                }
            }
            if (etiquetasLine != null) {
                String payload = etiquetasLine.substring("Etiquetas:".length()).trim();
                String[] parts = payload.split(",");
                String dishVal = "";
                String timeVal = "";
                String foodTypeVal = "";
                for (String p : parts) {
                    String[] kv = p.split("=");
                    if (kv.length == 2) {
                        String key = kv[0].trim();
                        String val = kv[1].trim();
                        if (key.startsWith("Dish")) dishVal = val;
                        else if (key.startsWith("Time")) timeVal = val;
                        else if (key.startsWith("FoodType")) foodTypeVal = val;
                    }
                }
                tvDish.setText(getString(R.string.label_dish, dishVal));
                tvTime.setText(getString(R.string.label_time, timeVal));
                tvFoodType.setText(getString(R.string.label_foodtype, foodTypeVal));
            } else {
                tvDish.setText("");
                tvTime.setText("");
                tvFoodType.setText("");
            }

            // El resto (pasos) asumimos que viene después de una línea en blanco tras etiquetas
            int idx = contenido.indexOf("\n\n");
            String pasos = "";
            if (idx >= 0 && idx + 2 < contenido.length()) {
                pasos = contenido.substring(idx + 2);
            } else if (lines.length > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < lines.length; i++) {
                    sb.append(lines[i]);
                    if (i < lines.length - 1) sb.append("\n");
                }
                pasos = sb.toString();
            }
            etPasos.setText(pasos);
        }

        // La vista usa TextView, por tanto ya es no-editable
        // Habilitar scroll interno para el TextView de pasos (si es necesario)
        etPasos.setMovementMethod(new ScrollingMovementMethod());

        btnVolver.setOnClickListener(v -> finish());

        // Editar: abrir formulario de edición pasando viewId
        if (btnEditar != null) btnEditar.setOnClickListener(v -> {
            Intent intentEdit = new Intent(this, formulario_recetarium.class);
            intentEdit.putExtra("viewId", viewId);
            // Start for result so we can refresh the view when editing finishes
            startActivityForResult(intentEdit, 1003);
        });

        // Eliminar: borrar la receta de la DB y cerrar
        if (btnEliminar != null) btnEliminar.setOnClickListener(v -> {
            // reuse repo instance declared above
            repo.deleteRecipe(viewId);
            Toast.makeText(this, getString(R.string.msg_receta_eliminada), Toast.LENGTH_SHORT).show();
            // Signal deletion to caller and finish so parent can refresh immediately
            Intent data = new Intent();
            data.putExtra("deletedViewId", viewId);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If edit finished successfully, reload the content from DB and refresh the UI
        if (requestCode == 1003 && resultCode == RESULT_OK) {
            // Editing finished in the child form: notify parent and close this view so callers refresh
            setResult(RESULT_OK);
            finish();
        }
    }
}