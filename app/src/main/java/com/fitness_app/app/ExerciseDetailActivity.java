package com.fitness_app.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDetailActivity extends AppCompatActivity {

    private EditText etSearchExercise;
    private RecyclerView rvExercises;
    private ExerciseAdapter adapter;
    private List<Exercise> fullExerciseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        etSearchExercise = findViewById(R.id.etSearchExercise);
        rvExercises = findViewById(R.id.rvExercises);

        fullExerciseList = new ArrayList<>();
        populateExerciseLibrary();

        adapter = new ExerciseAdapter(new ArrayList<>(fullExerciseList));
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setAdapter(adapter);

        etSearchExercise.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExercises(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterExercises(String query) {
        List<Exercise> filtered = new ArrayList<>();
        for (Exercise ex : fullExerciseList) {
            if (ex.getName().toLowerCase().contains(query.toLowerCase())
                    || ex.getMuscleGroup().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(ex);
            }
        }
        adapter.filterList(filtered);
    }

    private void populateExerciseLibrary() {
        fullExerciseList.add(new Exercise(
                "Barbell Bench Press",
                "Chest (Pectorals, Triceps, Anterior Deltoids)",
                "Barbell & Flat Bench",
                "1. Lie flat on the bench with eyes under the bar.\n2. Grip bar slightly wider than shoulder width.\n3. Lower bar with control to mid-chest.\n4. Press upward explosively while keeping shoulder blades retracted."
        ));

        fullExerciseList.add(new Exercise(
                "Incline Dumbbell Press",
                "Upper Chest (Clavicular Head) & Triceps",
                "Dumbbells & Incline Bench",
                "1. Set bench to a 30 to 45 degree angle.\n2. Press dumbbells upward until arms are extended.\n3. Lower with control until weights reach chest level."
        ));

        fullExerciseList.add(new Exercise(
                "Barbell Back Squat",
                "Legs (Quadriceps, Glutes, Hamstrings, Core)",
                "Barbell & Squat Rack",
                "1. Rest bar securely across upper traps.\n2. Keep feet shoulder-width apart, toes angled slightly out.\n3. Break at hips and knees, squatting down until thighs are parallel to ground.\n4. Drive through midfoot to stand back up."
        ));

        fullExerciseList.add(new Exercise(
                "Romanian Deadlift (RDL)",
                "Posterior Chain (Hamstrings, Glutes, Lower Back)",
                "Barbell or Dumbbells",
                "1. Stand tall holding weight with a slight bend in knees.\n2. Hinge backward at the hips, keeping back flat and bar close to legs.\n3. Lower until feeling a hamstring stretch, then squeeze glutes to return."
        ));

        fullExerciseList.add(new Exercise(
                "Pull-Up / Chin-Up",
                "Back (Latissimus Dorsi, Biceps, Upper Back)",
                "Pull-up Bar",
                "1. Grip bar overhand or underhand.\n2. Pull your chest toward the bar by driving elbows down.\n3. Lower down under control to a full stretch."
        ));

        fullExerciseList.add(new Exercise(
                "Overhead Shoulder Press (OHP)",
                "Shoulders (Deltoids, Upper Traps, Triceps)",
                "Barbell or Dumbbells",
                "1. Hold weight at shoulder height with core braced.\n2. Press straight overhead until arms lockout.\n3. Lower under control back to collarbone level."
        ));

        fullExerciseList.add(new Exercise(
                "Barbell Bicep Curl",
                "Arms (Biceps Brachii)",
                "Barbell or EZ-Curl Bar",
                "1. Stand straight holding the bar with an underhand grip.\n2. Curl the weight up while keeping elbows pinned to your sides.\n3. Squeeze at top and lower under control."
        ));

        fullExerciseList.add(new Exercise(
                "Triceps Skull Crushers",
                "Arms (Triceps Brachii)",
                "EZ-Curl Bar & Flat Bench",
                "1. Lie flat, extending arms upward with the bar.\n2. Bend at elbows to lower the weight toward your forehead.\n3. Extend elbows back to lockout, isolating the triceps."
        ));

        fullExerciseList.add(new Exercise(
                "Hanging Leg Raises",
                "Core (Rectus Abdominis & Hip Flexors)",
                "Pull-up Bar",
                "1. Hang from a pull-up bar with arms straight.\n2. Raise legs up until parallel to ground (or knees to chest).\n3. Lower slowly without swinging."
        ));
    }
}