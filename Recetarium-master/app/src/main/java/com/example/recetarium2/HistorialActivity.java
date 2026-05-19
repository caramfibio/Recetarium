package com.example.recetarium2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
// ...existing code... (removed unused imports)
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistorialActivity extends AppCompatActivity {

    // Nombres de los días para cabecera
    private static final String[] DIAS_CORTOS = {"L", "M", "X", "J", "V", "S y D"};

    // Colores de cabecera (mismos que en dias_activity)
    private static final int[] COLORES_CABECERA = {
            0xFF5B9BD5,  // L - azul
            0xFF70AD47,  // M - verde
            0xFFC00000,  // X - rojo
            0xFFFFD966,  // J - amarillo
            0xFF70AD47,  // V - verde
            0xFFA9D18E   // S y D - verde claro
    };

    private static final float[] PESOS = {1f, 1f, 1f, 1f, 1f, 1.5f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvTitulo = findViewById(R.id.tvTituloMes);
        LinearLayout contenedor = findViewById(R.id.contenedorSemanas);
        Button btnVolver = findViewById(R.id.btnVolver);

        // --- Mes actual dinámico ---
        Calendar hoy = Calendar.getInstance();
        int mesActual = hoy.get(Calendar.MONTH);       // 0=enero … 11=diciembre
        int anioActual = hoy.get(Calendar.YEAR);

        // Nombre del mes en español
        SimpleDateFormat sdfMes = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
        String nombreMes = sdfMes.format(hoy.getTime());
        tvTitulo.setText(String.format(Locale.getDefault(), "Recetas del mes de %s:", nombreMes));

        // --- Calcular semanas del mes actual ---
        List<int[]> semanas = calcularSemanasDelMes(anioActual, mesActual);

        // Repositorio de recetas (SQLite)
        com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);

        for (int[] semana : semanas) {
            // semana[0] = día inicio, semana[1] = día fin (dentro del mes)
            int diaInicio = semana[0];
            int diaFin    = semana[1];

            // Comprueba si algún día de esa semana tiene receta guardada
            if (!semanaContieneReceta(repo, anioActual, mesActual, diaInicio, diaFin)) {
                continue; // omite semanas sin recetas
            }

            // Etiqueta "Semana 04 – 10:"
            TextView tvEtiqueta = new TextView(this);
            tvEtiqueta.setText(String.format(Locale.getDefault(), "Semana %02d – %02d:", diaInicio, diaFin));
            tvEtiqueta.setTextColor(Color.WHITE);
            tvEtiqueta.setTextSize(16f);
            LinearLayout.LayoutParams paramsLabel = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsLabel.setMargins(0, 16, 0, 6);
            tvEtiqueta.setLayoutParams(paramsLabel);
            contenedor.addView(tvEtiqueta);

            // Bloque de semana (cabecera + celdas)
            LinearLayout bloqueWrapper = new LinearLayout(this);
            bloqueWrapper.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams paramsBloque = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsBloque.setMargins(0, 0, 0, 8);
            bloqueWrapper.setLayoutParams(paramsBloque);

            // Fila cabecera
            LinearLayout filaCabecera = new LinearLayout(this);
            filaCabecera.setOrientation(LinearLayout.HORIZONTAL);
            filaCabecera.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(28)));

            // Fila celdas
            LinearLayout filaCeldas = new LinearLayout(this);
            filaCeldas.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsCeldas = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(40));
            filaCeldas.setLayoutParams(paramsCeldas);

            for (int i = 0; i < DIAS_CORTOS.length; i++) {
                // Celda cabecera
                TextView tvDia = new TextView(this);
                tvDia.setText(DIAS_CORTOS[i]);
                tvDia.setGravity(Gravity.CENTER);
                tvDia.setTextColor(i == 3 ? Color.parseColor("#333333") : Color.WHITE); // J texto oscuro
                tvDia.setTextSize(11f);
                tvDia.setBackgroundColor(COLORES_CABECERA[i]);
                LinearLayout.LayoutParams paramsCab = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, PESOS[i]);
                if (i > 0) paramsCab.setMarginStart(dpToPx(2));
                tvDia.setLayoutParams(paramsCab);
                filaCabecera.addView(tvDia);

                // Celda de contenido (verde claro si tiene receta, gris si no)
                View celda = new View(this);
                int diaReal = diaInicio + i; // día real dentro del mes
                // Ya no usamos fecha para las recetas; marcamos celda como "tiene receta" si hay cualquier receta almacenada
                boolean tieneReceta = !repo.getAllRecipes().isEmpty();
                celda.setBackgroundColor(tieneReceta ?
                        Color.parseColor("#C8E6C9") : Color.parseColor("#D9D9D9"));
                LinearLayout.LayoutParams paramsCelda = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, PESOS[i]);
                if (i > 0) paramsCelda.setMarginStart(dpToPx(2));
                celda.setLayoutParams(paramsCelda);
                filaCeldas.addView(celda);
            }

            bloqueWrapper.addView(filaCabecera);
            bloqueWrapper.addView(filaCeldas);

            // Al tocar el bloque → abre dias_activity para esa semana
            final int semanaInicio = diaInicio;
            bloqueWrapper.setOnClickListener(v -> {
                Intent intent = new Intent(this, dias_activity.class);
                intent.putExtra("anio", anioActual);
                intent.putExtra("mes", mesActual);
                intent.putExtra("diaInicio", semanaInicio);
                startActivity(intent);
            });

            contenedor.addView(bloqueWrapper);
        }

        btnVolver.setOnClickListener(v -> finish());
    }

    // ---------------------------------------------------------------
    // Calcula todas las semanas L-D del mes dado
    // Devuelve lista de {diaInicio, diaFin} dentro del mes
    // ---------------------------------------------------------------
    private List<int[]> calcularSemanasDelMes(int anio, int mes) {
        List<int[]> semanas = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(anio, mes, 1);

        int diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Día de la semana del día 1 (Calendar: 1=Dom, 2=Lun … 7=Sáb)
        int diaSemanaInicio = cal.get(Calendar.DAY_OF_WEEK);

        // Convertimos a índice lunes=0
        int offsetLunes = (diaSemanaInicio == Calendar.SUNDAY) ? 6 : diaSemanaInicio - 2;

        // Primer lunes del bloque (puede ser negativo si el mes empieza después del lunes)
        int lunes = 1 - offsetLunes;
        while (lunes <= diasEnMes) {
            int domingo = lunes + 6;
            int inicio = Math.max(lunes, 1);
            int fin    = Math.min(domingo, diasEnMes);
            if (fin >= 1) {
                semanas.add(new int[]{inicio, fin});
            }
            lunes += 7;
        }

        return semanas;
    }

    // ---------------------------------------------------------------
    // Comprueba si algún día de la semana tiene receta guardada
    // ---------------------------------------------------------------
    private boolean semanaContieneReceta(com.example.recetarium2.data.RecipeRepository repo,
                                         int anio, int mes,
                                         int diaInicio, int diaFin) {
        // Dado que las recetas ya no están vinculadas a fechas, consideramos que una semana contiene receta
        // si existe al menos una receta almacenada.
        return !repo.getAllRecipes().isEmpty();
    }

        // ...existing code...

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}