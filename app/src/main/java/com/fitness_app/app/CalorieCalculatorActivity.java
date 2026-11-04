package com.fitness_app.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CalorieCalculatorActivity extends AppCompatActivity {

    private RadioButton rbMale;
    private EditText etCalAge, etCalWeight, etCalHeight;
    private Spinner spinnerActivityLevel;
    private Button btnCalculateCalories;
    private LinearLayout layoutCalorieResults;
    private TextView tvBmrValue, tvMaintenanceCals, tvWeightLossCals, tvWeightGainCals;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private final String[] activityLevels = {
            "Sedentary (little or no exercise)",
            "Lightly active (light exercise 1-3 days/week)",
            "Moderately active (moderate exercise 3-5 days/week)",
            "Very active (hard exercise 6-7 days/week)",
            "Extremely active (hard training or physical job)"
    };

    private final double[] activityMultipliers = {
            1.2,
            1.375,
            1.55,
            1.725,
            1.9
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_calculator);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Bind Views
        rbMale = findViewById(R.id.rbMale);
        etCalAge = findViewById(R.id.etCalAge);
        etCalWeight = findViewById(R.id.etCalWeight);
        etCalHeight = findViewById(R.id.etCalHeight);
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel);
        btnCalculateCalories = findViewById(R.id.btnCalculateCalories);
        layoutCalorieResults = findViewById(R.id.layoutCalorieResults);
        tvBmrValue = findViewById(R.id.tvBmrValue);
        tvMaintenanceCals = findViewById(R.id.tvMaintenanceCals);
        tvWeightLossCals = findViewById(R.id.tvWeightLossCals);
        tvWeightGainCals = findViewById(R.id.tvWeightGainCals);

        // Populate Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                activityLevels
        );
        spinnerActivityLevel.setAdapter(spinnerAdapter);

        // Prefill user data from Firestore if available
        prefillUserData();

        btnCalculateCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDailyCalories();
            }
        });
    }

    private void prefillUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        mFirestore.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc.exists()) {
                            Long age = doc.getLong("age");
                            Double weight = doc.getDouble("weight");
                            Double height = doc.getDouble("height");

                            if (age != null) etCalAge.setText(String.valueOf(age));
                            if (weight != null) etCalWeight.setText(String.valueOf(weight));
                            if (height != null) etCalHeight.setText(String.valueOf(height));
                        }
                    }
                });
    }

    private void calculateDailyCalories() {
        String ageStr = etCalAge.getText().toString().trim();
        String weightStr = etCalWeight.getText().toString().trim();
        String heightStr = etCalHeight.getText().toString().trim();

        if (TextUtils.isEmpty(ageStr)) {
            etCalAge.setError("Enter age");
            etCalAge.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(weightStr)) {
            etCalWeight.setError("Enter weight");
            etCalWeight.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(heightStr)) {
            etCalHeight.setError("Enter height");
            etCalHeight.requestFocus();
            return;
        }

        int age = Integer.parseInt(ageStr);
        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);
        boolean isMale = rbMale.isChecked();

        if (age <= 0 || weight <= 0 || height <= 0) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mifflin-St Jeor Formula
        // Men: BMR = (10 * weight) + (6.25 * height) - (5 * age) + 5
        // Women: BMR = (10 * weight) + (6.25 * height) - (5 * age) - 161
        double bmr;
        if (isMale) {
            bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0;
        } else {
            bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) - 161.0;
        }

        int selectedIndex = spinnerActivityLevel.getSelectedItemPosition();
        double multiplier = activityMultipliers[selectedIndex];

        double tdee = bmr * multiplier;
        double weightLoss = Math.max(1200, tdee - 500);
        double weightGain = tdee + 300;

        tvBmrValue.setText("Basal Metabolic Rate (BMR): " + Math.round(bmr) + " kcal/day");
        tvMaintenanceCals.setText(Math.round(tdee) + " kcal/day");
        tvWeightLossCals.setText(Math.round(weightLoss) + " kcal/day");
        tvWeightGainCals.setText(Math.round(weightGain) + " kcal/day");

        layoutCalorieResults.setVisibility(View.VISIBLE);
    }
}