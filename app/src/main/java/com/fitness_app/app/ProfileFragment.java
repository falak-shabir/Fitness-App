package com.fitness_app.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfileAge, tvProfileWeight, tvProfileHeight;
    private ProgressBar profileProgressBar;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Bind Views
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileAge = view.findViewById(R.id.tvProfileAge);
        tvProfileWeight = view.findViewById(R.id.tvProfileWeight);
        tvProfileHeight = view.findViewById(R.id.tvProfileHeight);
        profileProgressBar = view.findViewById(R.id.profileProgressBar);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Fetch User Data
        loadUserProfile();

        // Logout Handler
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();
        profileProgressBar.setVisibility(View.VISIBLE);

        mFirestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        profileProgressBar.setVisibility(View.GONE);
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String email = documentSnapshot.getString("email");
                            Long age = documentSnapshot.getLong("age");
                            Double weight = documentSnapshot.getDouble("weight");
                            Double height = documentSnapshot.getDouble("height");

                            tvProfileName.setText(fullName != null ? fullName : "--");
                            tvProfileEmail.setText(email != null ? email : "--");
                            tvProfileAge.setText(age != null ? String.valueOf(age) : "--");
                            tvProfileWeight.setText(weight != null ? weight + " kg" : "-- kg");
                            tvProfileHeight.setText(height != null ? height + " cm" : "-- cm");
                        } else {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Profile data not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        profileProgressBar.setVisibility(View.GONE);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}