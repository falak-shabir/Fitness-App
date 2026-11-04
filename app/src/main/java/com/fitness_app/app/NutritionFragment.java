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

public class NutritionFragment extends Fragment {

    private EditText etMealName, etCalories, etProtein, etCarbs, etFat;
    private Button btnAddMeal;
    private ProgressBar nutritionProgressBar;
    private TextView tvEmptyNutrition, tvTotalCalories, tvTotalProtein;
    private RecyclerView rvNutrition;

    private NutritionAdapter adapter;
    private List<Meal> mealList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public NutritionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nutrition, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Bind Views
        etMealName = view.findViewById(R.id.etMealName);
        etCalories = view.findViewById(R.id.etCalories);
        etProtein = view.findViewById(R.id.etProtein);
        etCarbs = view.findViewById(R.id.etCarbs);
        etFat = view.findViewById(R.id.etFat);
        btnAddMeal = view.findViewById(R.id.btnAddMeal);
        nutritionProgressBar = view.findViewById(R.id.nutritionProgressBar);
        tvEmptyNutrition = view.findViewById(R.id.tvEmptyNutrition);
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvTotalProtein = view.findViewById(R.id.tvTotalProtein);
        rvNutrition = view.findViewById(R.id.rvNutrition);

        // RecyclerView Setup
        mealList = new ArrayList<>();
        adapter = new NutritionAdapter(mealList);
        rvNutrition.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNutrition.setAdapter(adapter);

        // Load existing meals
        loadMeals();

        // Add Meal Click Listener
        btnAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMeal();
            }
        });
    }

    private void saveMeal() {
        String mealName = etMealName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String proteinStr = etProtein.getText().toString().trim();
        String carbsStr = etCarbs.getText().toString().trim();
        String fatStr = etFat.getText().toString().trim();

        if (TextUtils.isEmpty(mealName)) {
            etMealName.setError("Meal name required");
            etMealName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(caloriesStr)) {
            etCalories.setError("Calories required");
            etCalories.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        nutritionProgressBar.setVisibility(View.VISIBLE);
        btnAddMeal.setEnabled(false);

        int calories = Integer.parseInt(caloriesStr);
        double protein = TextUtils.isEmpty(proteinStr) ? 0.0 : Double.parseDouble(proteinStr);
        double carbs = TextUtils.isEmpty(carbsStr) ? 0.0 : Double.parseDouble(carbsStr);
        double fat = TextUtils.isEmpty(fatStr) ? 0.0 : Double.parseDouble(fatStr);
        long timestamp = System.currentTimeMillis();

        DocumentReference docRef = mFirestore.collection("users")
                .document(user.getUid())
                .collection("nutrition")
                .document();

        Meal meal = new Meal(docRef.getId(), mealName, calories, protein, carbs, fat, timestamp);

        docRef.set(meal)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        nutritionProgressBar.setVisibility(View.GONE);
                        btnAddMeal.setEnabled(true);
                        etMealName.setText("");
                        etCalories.setText("");
                        etProtein.setText("");
                        etCarbs.setText("");
                        etFat.setText("");
                        Toast.makeText(getContext(), "Meal logged!", Toast.LENGTH_SHORT).show();
                        loadMeals();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        nutritionProgressBar.setVisibility(View.GONE);
                        btnAddMeal.setEnabled(true);
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadMeals() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        nutritionProgressBar.setVisibility(View.VISIBLE);

        mFirestore.collection("users")
                .document(user.getUid())
                .collection("nutrition")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        nutritionProgressBar.setVisibility(View.GONE);
                        mealList.clear();

                        int totalCals = 0;
                        double totalProt = 0.0;

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Meal meal = doc.toObject(Meal.class);
                            mealList.add(meal);
                            totalCals += meal.getCalories();
                            totalProt += meal.getProtein();
                        }

                        tvTotalCalories.setText(totalCals + " kcal");
                        tvTotalProtein.setText(String.format("%.1f", totalProt) + " g");

                        adapter.notifyDataSetChanged();

                        if (mealList.isEmpty()) {
                            tvEmptyNutrition.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyNutrition.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        nutritionProgressBar.setVisibility(View.GONE);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load meals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}