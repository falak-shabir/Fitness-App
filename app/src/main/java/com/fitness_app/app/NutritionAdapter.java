package com.fitness_app.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NutritionAdapter extends RecyclerView.Adapter<NutritionAdapter.MealViewHolder> {

    private final List<Meal> mealList;

    public NutritionAdapter(List<Meal> mealList) {
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.tvItemMealName.setText(meal.getMealName());
        holder.tvItemCalories.setText(meal.getCalories() + " kcal");
        holder.tvItemMacros.setText("P: " + meal.getProtein() + "g | C: " + meal.getCarbs() + "g | F: " + meal.getFat() + "g");
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemMealName, tvItemCalories, tvItemMacros;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemMealName = itemView.findViewById(R.id.tvItemMealName);
            tvItemCalories = itemView.findViewById(R.id.tvItemCalories);
            tvItemMacros = itemView.findViewById(R.id.tvItemMacros);
        }
    }
}