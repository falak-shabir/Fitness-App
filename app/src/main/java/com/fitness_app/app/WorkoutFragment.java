package com.fitness_app.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFragment extends Fragment {

    private EditText etExerciseName, etSets, etReps, etWorkoutWeight;
    private Button btnAddWorkout;
    private ProgressBar workoutProgressBar;
    private TextView tvEmptyWorkout;
    private RecyclerView rvWorkouts;

    private WorkoutAdapter adapter;
    private List<Workout> workoutList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public WorkoutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Bind Views
        etExerciseName = view.findViewById(R.id.etExerciseName);
        etSets = view.findViewById(R.id.etSets);
        etReps = view.findViewById(R.id.etReps);
        etWorkoutWeight = view.findViewById(R.id.etWorkoutWeight);
        btnAddWorkout = view.findViewById(R.id.btnAddWorkout);
        workoutProgressBar = view.findViewById(R.id.workoutProgressBar);
        tvEmptyWorkout = view.findViewById(R.id.tvEmptyWorkout);
        rvWorkouts = view.findViewById(R.id.rvWorkouts);

        // RecyclerView Setup
        workoutList = new ArrayList<>();
        adapter = new WorkoutAdapter(workoutList);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkouts.setAdapter(adapter);

        // Load existing logs
        loadWorkouts();

        // Add Workout Button Click
        btnAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveWorkout();
            }
        });
    }

    private void saveWorkout() {
        String exercise = etExerciseName.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String repsStr = etReps.getText().toString().trim();
        String weightStr = etWorkoutWeight.getText().toString().trim();

        if (TextUtils.isEmpty(exercise)) {
            etExerciseName.setError("Exercise name required");
            etExerciseName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(setsStr)) {
            etSets.setError("Sets required");
            etSets.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(repsStr)) {
            etReps.setError("Reps required");
            etReps.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(weightStr)) {
            etWorkoutWeight.setError("Weight required");
            etWorkoutWeight.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        workoutProgressBar.setVisibility(View.VISIBLE);
        btnAddWorkout.setEnabled(false);

        int sets = Integer.parseInt(setsStr);
        int reps = Integer.parseInt(repsStr);
        double weight = Double.parseDouble(weightStr);
        long timestamp = System.currentTimeMillis();

        DocumentReference docRef = mFirestore.collection("users")
                .document(user.getUid())
                .collection("workouts")
                .document();

        Workout workout = new Workout(docRef.getId(), exercise, sets, reps, weight, timestamp);

        docRef.set(workout)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        workoutProgressBar.setVisibility(View.GONE);
                        btnAddWorkout.setEnabled(true);
                        etExerciseName.setText("");
                        etSets.setText("");
                        etReps.setText("");
                        etWorkoutWeight.setText("");
                        Toast.makeText(getContext(), "Workout logged!", Toast.LENGTH_SHORT).show();
                        loadWorkouts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        workoutProgressBar.setVisibility(View.GONE);
                        btnAddWorkout.setEnabled(true);
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadWorkouts() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        workoutProgressBar.setVisibility(View.VISIBLE);

        mFirestore.collection("users")
                .document(user.getUid())
                .collection("workouts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        workoutProgressBar.setVisibility(View.GONE);
                        workoutList.clear();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Workout workout = doc.toObject(Workout.class);
                            workoutList.add(workout);
                        }

                        adapter.notifyDataSetChanged();

                        if (workoutList.isEmpty()) {
                            tvEmptyWorkout.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyWorkout.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        workoutProgressBar.setVisibility(View.GONE);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}