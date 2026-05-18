package com.example.recetarium2;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import java.util.Calendar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class dias_activity extends AppCompatActivity {

    private String diaPulsado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dias);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencias a cada casilla de día
        TextView tvLunes     = findViewById(R.id.tvLunes);
        TextView tvMartes    = findViewById(R.id.tvMartes);
        TextView tvMiercoles = findViewById(R.id.tvMiercoles);
        TextView tvJueves    = findViewById(R.id.tvJueves);
        TextView tvViernes   = findViewById(R.id.tvViernes);
        TextView tvSabado    = findViewById(R.id.tvSabado);
        TextView tvDomingo   = findViewById(R.id.tvDomingo);
        Button   btnVolver   = findViewById(R.id.btnVolver);

        // Lista para iterar fácilmente
        TextView[] dias = new TextView[]{tvLunes, tvMartes, tvMiercoles, tvJueves, tvViernes, tvSabado, tvDomingo};

        // Leer extras para saber qué semana/mes se está mostrando
        int anio, mes, diaInicio;
        if (getIntent().hasExtra("anio") && getIntent().hasExtra("mes") && getIntent().hasExtra("diaInicio")) {
            anio = getIntent().getIntExtra("anio", Calendar.getInstance().get(Calendar.YEAR));
            mes = getIntent().getIntExtra("mes", Calendar.getInstance().get(Calendar.MONTH));
            diaInicio = getIntent().getIntExtra("diaInicio", 1);
        } else if (getIntent().hasExtra("semana")) {
            // Si se lanzó desde SeleccionarSemanaActivity sólo tenemos el número de semana
            int semana = getIntent().getIntExtra("semana", 1);
            Calendar hoy = Calendar.getInstance();
            anio = hoy.get(Calendar.YEAR);
            mes = hoy.get(Calendar.MONTH);
            // Simplificación: semana 1 → diaInicio=1, semana 2 → diaInicio=8, etc.
            diaInicio = 1 + (semana - 1) * 7;
        } else {
            // Por defecto, mostrar la primera semana del mes actual
            Calendar hoy = Calendar.getInstance();
            anio = hoy.get(Calendar.YEAR);
            mes = hoy.get(Calendar.MONTH);
            diaInicio = 1;
        }

        // Repositorio que gestiona recetas (SQLite)
        final com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);

        int diasEnMes;
        Calendar cal = Calendar.getInstance();
        cal.set(anio, mes, 1);
        diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Rellena las casillas con la receta (si existe) y registra para menú contextual
        for (int i = 0; i < dias.length; i++) {
            TextView tv = dias[i];
            int diaNumero = diaInicio + i;
            if (diaNumero > diasEnMes) {
                tv.setText("");
                tv.setEnabled(false);
                continue;
            }
            String texto = repo.getRecipe(anio, mes, diaNumero);
            tv.setText(texto == null || texto.isEmpty() ? "vacio" : texto);
            tv.setTag(new int[]{anio, mes, diaNumero}); // guardamos datos para el menú
            registerForContextMenu(tv);
        }

        // Botón Volver
        btnVolver.setOnClickListener(v -> finish());
    }

    // Se llama justo antes de mostrar el menú contextual
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // El tag contiene int[] {anio, mes, dia}
        Object tag = v.getTag();
        if (tag instanceof int[]) {
            int[] datos = (int[]) tag;
            diaPulsado = datos[0] + "_" + datos[1] + "_" + datos[2];
        } else {
            diaPulsado = "";
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dia, menu);
        menu.setHeaderTitle(diaPulsado);
    }

    // Se llama cuando el usuario elige una opción del menú
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        // diaPulsado tiene el formato "anio_mes_dia"
        String[] parts = diaPulsado.split("_");
        if (parts.length != 3) return super.onContextItemSelected(item);
        int anio = Integer.parseInt(parts[0]);
        int mes = Integer.parseInt(parts[1]);
        int dia = Integer.parseInt(parts[2]);

        if (id == R.id.menu_ver_receta) {
            Intent intent = new Intent(this, formulario_verReceta.class);
            intent.putExtra("anio", anio);
            intent.putExtra("mes", mes);
            intent.putExtra("dia", dia);
            startActivity(intent);
            return true;

        } else if (id == R.id.menu_aniadir_receta) {
            Intent intent = new Intent(this, formulario_recetarium.class);
            intent.putExtra("anio", anio);
            intent.putExtra("mes", mes);
            intent.putExtra("dia", dia);
            startActivity(intent);
            return true;

        } else if (id == R.id.menu_borrar_receta) {
            // Usamos repo para borrar
            com.example.recetarium2.data.RecipeRepository repo = new com.example.recetarium2.data.RecipeRepositoryImpl(this);
            repo.deleteRecipe(anio, mes, dia);
            // Actualiza la vista correspondiente
            int[] ids = new int[]{R.id.tvLunes, R.id.tvMartes, R.id.tvMiercoles, R.id.tvJueves, R.id.tvViernes, R.id.tvSabado, R.id.tvDomingo};
            for (int viewId : ids) {
                TextView tv = findViewById(viewId);
                Object tag = tv.getTag();
                if (tag instanceof int[]) {
                    int[] datos = (int[]) tag;
                    if (datos[0] == anio && datos[1] == mes && datos[2] == dia) {
                        tv.setText("vacio");
                        break;
                    }
                }
            }
            return true;
        }

        return super.onContextItemSelected(item);
    }

    // Limpia el texto del día correspondiente
    private void borrarReceta(String dia) {
        int viewId;
        switch (dia) {
            case "Lunes":     viewId = R.id.tvLunes;     break;
            case "Martes":    viewId = R.id.tvMartes;    break;
            case "Miercoles": viewId = R.id.tvMiercoles; break;
            case "Jueves":    viewId = R.id.tvJueves;    break;
            case "Viernes":   viewId = R.id.tvViernes;   break;
            case "Sabado":    viewId = R.id.tvSabado;    break;
            case "Domingo":   viewId = R.id.tvDomingo;   break;
            default: return;
        }
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText("");
    }
}