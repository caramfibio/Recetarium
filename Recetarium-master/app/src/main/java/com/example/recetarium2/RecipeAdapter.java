package com.example.recetarium2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetarium2.data.RecipeRecord;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final List<RecipeRecord> items;

    public RecipeAdapter(List<RecipeRecord> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeRecord r = items.get(position);
        String[] parts = r.getContent().split("\\n", 2);
        String title = parts.length > 0 ? parts[0] : "(sin título)";
        String snippet = parts.length > 1 ? parts[1] : "";
        holder.tvTitle.setText(title);
        holder.tvSnippet.setText(snippet.length() > 80 ? snippet.substring(0, 80) + "..." : snippet);
        holder.tvDate.setText(String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", r.getYear(), r.getMonth(), r.getDay()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvSnippet, tvDate;
        ViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvRecipeTitle);
            tvSnippet = v.findViewById(R.id.tvRecipeSnippet);
            tvDate = v.findViewById(R.id.tvRecipeDate);
        }
    }
}

