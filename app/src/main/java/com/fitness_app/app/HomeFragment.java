package com.fitness_app.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private TextView tvHomeUserName;
    private Button btnOpenBmiCalculator, btnOpenCalorieCalculator, btnOpenExerciseDetail;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Bind Views
        tvHomeUserName = view.findViewById(R.id.tvHomeUserName);
        btnOpenBmiCalculator = view.findViewById(R.id.btnOpenBmiCalculator);
        btnOpenCalorieCalculator = view.findViewById(R.id.btnOpenCalorieCalculator);
        btnOpenExerciseDetail = view.findViewById(R.id.btnOpenExerciseDetail);

        // Load logged in user name
        loadUserData();

        // BMI Calculator Navigation
        btnOpenBmiCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), BMICalculatorActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Calorie Calculator Navigation
        btnOpenCalorieCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), CalorieCalculatorActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Exercise Detail Navigation
        btnOpenExerciseDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), ExerciseDetailActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        mFirestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                tvHomeUserName.setText(fullName);
                            }
                        }
                    }
                });
    }
}