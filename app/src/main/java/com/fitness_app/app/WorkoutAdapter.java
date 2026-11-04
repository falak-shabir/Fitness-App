package com.fitness_app.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private final List<Workout> workoutList;

    public WorkoutAdapter(List<Workout> workoutList) {
        this.workoutList = workoutList;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        holder.tvItemExerciseName.setText(workout.getExerciseName());
        holder.tvItemSetsReps.setText("Sets: " + workout.getSets() + " | Reps: " + workout.getReps());
        holder.tvItemWeight.setText(workout.getWeight() + " kg");
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemExerciseName, tvItemSetsReps, tvItemWeight;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemExerciseName = itemView.findViewById(R.id.tvItemExerciseName);
            tvItemSetsReps = itemView.findViewById(R.id.tvItemSetsReps);
            tvItemWeight = itemView.findViewById(R.id.tvItemWeight);
        }
    }
}