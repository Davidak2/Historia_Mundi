package com.example.historia_mundi.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.historia_mundi.R;
import com.example.historia_mundi.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private String email, password;

    /**
     *
     * Different Bindings for different button listeners
     * to allow switching between screens
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSignUp.setOnClickListener(view->{
            startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
        });

        binding.txtForgotPassword.setOnClickListener(view->{
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        binding.btnLogin.setOnClickListener(view -> {
            if(areFieldReady())
                login();
        });
    }

    /**
     * Function that checks if there is no existing user using the same user name
     * and sends out a conformation email
     */

    private void login() {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    if(firebaseAuth.getCurrentUser().isEmailVerified())
                    {
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> email) {
                                if(email.isSuccessful())
                                {
                                    Toast.makeText(LoginActivity.this,"Please Verify E-mail",Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(LoginActivity.this,"Error :Login (1)" + email.getException(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }else{
                    Toast.makeText(LoginActivity.this,"Error :Login (2)" + task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Checks if the fields necessery for login are ready (filled proparly)
     */

    private boolean areFieldReady()
    {
        email = binding.edtEmail.getText().toString().trim();
        password = binding.edtPassword.getText().toString().trim();

        boolean flag = false;
        View requestView = null;

        if(email.isEmpty())
        {
            binding.edtEmail.setError("Field Is Required");
            flag = true;
            requestView=binding.edtEmail;
        }
        else if(password.length()<6)
        {
            binding.edtPassword.setError("Field Is Required");
            flag = true;
            requestView=binding.edtPassword;
        }

        if(flag)
        {
            requestView.requestFocus();
            return false;
        }else{
            return true;
        }
    }
}