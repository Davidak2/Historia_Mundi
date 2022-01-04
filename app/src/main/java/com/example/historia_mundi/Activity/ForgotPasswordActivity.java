package com.example.historia_mundi.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.historia_mundi.R;
import com.example.historia_mundi.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    /**
     * Activity to remind user of his password
     */

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}