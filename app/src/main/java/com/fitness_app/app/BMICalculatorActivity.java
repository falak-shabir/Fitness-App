package com.fitness_app.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BMICalculatorActivity extends AppCompatActivity {

    private EditText etBmiWeight, etBmiHeight;
    private Button btnCalculateBmi, btnSaveBmiToProfile;
    private LinearLayout layoutBmiResult;
    private TextView tvBmiScore, tvBmiCategoryResult, tvBmiDescription;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private double calculatedWeight = 0;
    private double calculatedHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Bind Views
        etBmiWeight = findViewById(R.id.etBmiWeight);
        etBmiHeight = findViewById(R.id.etBmiHeight);
        btnCalculateBmi = findViewById(R.id.btnCalculateBmi);
        btnSaveBmiToProfile = findViewById(R.id.btnSaveBmiToProfile);
        layoutBmiResult = findViewById(R.id.layoutBmiResult);
        tvBmiScore = findViewById(R.id.tvBmiScore);
        tvBmiCategoryResult = findViewById(R.id.tvBmiCategoryResult);
        tvBmiDescription = findViewById(R.id.tvBmiDescription);

        btnCalculateBmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBmi();
            }
        });

        btnSaveBmiToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToUserProfile();
            }
        });
    }

    private void calculateBmi() {
        String weightStr = etBmiWeight.getText().toString().trim();
        String heightStr = etBmiHeight.getText().toString().trim();

        if (TextUtils.isEmpty(weightStr)) {
            etBmiWeight.setError("Enter your weight");
            etBmiWeight.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(heightStr)) {
            etBmiHeight.setError("Enter your height");
            etBmiHeight.requestFocus();
            return;
        }

        calculatedWeight = Double.parseDouble(weightStr);
        calculatedHeight = Double.parseDouble(heightStr);

        if (calculatedWeight <= 0 || calculatedHeight <= 0) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        double heightInMeters = calculatedHeight / 100.0;
        double bmi = calculatedWeight / (heightInMeters * heightInMeters);

        tvBmiScore.setText(String.format(Locale.getDefault(), "%.1f", bmi));

        if (bmi < 18.5) {
            tvBmiCategoryResult.setText("Category: Underweight");
            tvBmiCategoryResult.setTextColor(0xFFE67E22);
            tvBmiDescription.setText("Consider a calorie surplus and resistance training to gain healthy mass.");
        } else if (bmi < 24.9) {
            tvBmiCategoryResult.setText("Category: Normal Weight");
            tvBmiCategoryResult.setTextColor(0xFF28A745);
            tvBmiDescription.setText("Great job! Keep maintaining a balanced diet and regular physical activity.");
        } else if (bmi < 29.9) {
            tvBmiCategoryResult.setText("Category: Overweight");
            tvBmiCategoryResult.setTextColor(0xFFE67E22);
            tvBmiDescription.setText("Consider a slight calorie deficit and regular cardio/strength sessions.");
        } else {
            tvBmiCategoryResult.setText("Category: Obese");
            tvBmiCategoryResult.setTextColor(0xFFDC3545);
            tvBmiDescription.setText("Prioritize structured lifestyle changes, cardio, and balanced nutrition.");
        }

        layoutBmiResult.setVisibility(View.VISIBLE);
    }

    private void saveToUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveBmiToProfile.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("weight", calculatedWeight);
        updates.put("height", calculatedHeight);

        mFirestore.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        btnSaveBmiToProfile.setEnabled(true);
                        Toast.makeText(BMICalculatorActivity.this, "Profile updated with new stats!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        btnSaveBmiToProfile.setEnabled(true);
                        Toast.makeText(BMICalculatorActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}