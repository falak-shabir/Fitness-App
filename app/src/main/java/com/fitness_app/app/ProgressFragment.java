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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

public class ProgressFragment extends Fragment {

    private TextView tvProgressWeight, tvProgressBmi, tvBmiCategory;
    private TextView tvTotalWorkoutsCount, tvTotalMealsCount;
    private EditText etNewWeight;
    private Button btnUpdateWeight;
    private ProgressBar progressLoadingBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private double userHeightCm = 0;

    public ProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Bind Views
        tvProgressWeight = view.findViewById(R.id.tvProgressWeight);
        tvProgressBmi = view.findViewById(R.id.tvProgressBmi);
        tvBmiCategory = view.findViewById(R.id.tvBmiCategory);
        tvTotalWorkoutsCount = view.findViewById(R.id.tvTotalWorkoutsCount);
        tvTotalMealsCount = view.findViewById(R.id.tvTotalMealsCount);
        etNewWeight = view.findViewById(R.id.etNewWeight);
        btnUpdateWeight = view.findViewById(R.id.btnUpdateWeight);
        progressLoadingBar = view.findViewById(R.id.progressLoadingBar);

        // Load all data
        loadProgressData();

        // Update Weight Button Click
        btnUpdateWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserWeight();
            }
        });
    }

    private void loadProgressData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        String userId = user.getUid();
        progressLoadingBar.setVisibility(View.VISIBLE);

        // 1. Fetch User Profile (for Weight & Height)
        mFirestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Double weight = documentSnapshot.getDouble("weight");
                            Double height = documentSnapshot.getDouble("height");

                            if (height != null) {
                                userHeightCm = height;
                            }

                            if (weight != null) {
                                tvProgressWeight.setText(String.format(Locale.getDefault(), "%.1f kg", weight));
                                calculateAndDisplayBmi(weight, userHeightCm);
                            }
                        }
                    }
                });

        // 2. Fetch Total Workouts Count
        mFirestore.collection("users").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        tvTotalWorkoutsCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });

        // 3. Fetch Total Meals Count
        mFirestore.collection("users").document(userId).collection("nutrition")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        progressLoadingBar.setVisibility(View.GONE);
                        tvTotalMealsCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressLoadingBar.setVisibility(View.GONE);
                    }
                });
    }

    private void calculateAndDisplayBmi(double weightKg, double heightCm) {
        if (heightCm <= 0) {
            tvProgressBmi.setText("--");
            tvBmiCategory.setText("Status: Height not provided");
            return;
        }

        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);

        tvProgressBmi.setText(String.format(Locale.getDefault(), "%.1f", bmi));

        String category;
        if (bmi < 18.5) {
            category = "Underweight";
        } else if (bmi < 24.9) {
            category = "Normal weight";
        } else if (bmi < 29.9) {
            category = "Overweight";
        } else {
            category = "Obese";
        }

        tvBmiCategory.setText("Status: " + category);
    }

    private void updateUserWeight() {
        String newWeightStr = etNewWeight.getText().toString().trim();

        if (TextUtils.isEmpty(newWeightStr)) {
            etNewWeight.setError("Enter updated weight");
            etNewWeight.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        final double newWeight = Double.parseDouble(newWeightStr);
        progressLoadingBar.setVisibility(View.VISIBLE);
        btnUpdateWeight.setEnabled(false);

        mFirestore.collection("users").document(user.getUid())
                .update("weight", newWeight)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressLoadingBar.setVisibility(View.GONE);
                        btnUpdateWeight.setEnabled(true);
                        etNewWeight.setText("");
                        tvProgressWeight.setText(String.format(Locale.getDefault(), "%.1f kg", newWeight));
                        calculateAndDisplayBmi(newWeight, userHeightCm);
                        Toast.makeText(getContext(), "Weight updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressLoadingBar.setVisibility(View.GONE);
                        btnUpdateWeight.setEnabled(true);
                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}