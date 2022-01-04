package com.example.historia_mundi.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.historia_mundi.Constants.AllConstants;
import com.example.historia_mundi.Permissions.AppPermissions;
import com.example.historia_mundi.databinding.ActivitySignUpBinding;
import com.example.historia_mundi.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private Uri imageUri;
    private AppPermissions appPermissions;
    private String email,password,username;
    private StorageReference storageReference;

    /**
     * Signup Activity if user is not already exists in system
     * Sends out a conformation Email for given Email address
     * Sets up user in app database so they could log in next time
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appPermissions = new AppPermissions();
        storageReference = FirebaseStorage.getInstance().getReference();

        binding.btnSignUp.setOnClickListener(view -> {
            if(areFieldReady())
            {
                if(imageUri != null)
                {
                    signUp();
                }else{
                    Toast.makeText(this,"Image Is Required",Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.imgPick.setOnClickListener(view -> {
            if(AppPermissions.isStorageOk(this)) {
                pickImage();
            }else{
                appPermissions.requestStoragePermission(this);
            }
        });
    }


    /**
     * Function to pick an image
     */
    private void pickImage() {

        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this);
    }

    /**
     * Function that checks if the
     * fields are filled proparly
     */
    private boolean areFieldReady()
    {
        email = binding.edtEmail.getText().toString().trim();
        password = binding.edtPassword.getText().toString().trim();
        username = binding.edtUserName.getText().toString().trim();

        boolean flag = false;
        View requestView = null;

        if(username.isEmpty())
        {
            binding.edtUserName.setError("Field Is Required");
            flag = true;
            requestView=binding.edtUserName;
        }
        else if(email.isEmpty())
        {
            binding.edtEmail.setError("Field Is Required");
            flag = true;
            requestView=binding.edtEmail;
        }
        else if(password.length()<6)
        {
            binding.edtPassword.setError("Minimum 6 Characters");
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

    /**
     * Funtion to add the user to the database
     * gives every user a unique ID in the firebase database
     */
    private void signUp()
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> signUp) {

                if(signUp.isSuccessful())
                {
                    storageReference.child(firebaseAuth.getUid() + AllConstants.IMAGE_PATH).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> image = taskSnapshot.getStorage().getDownloadUrl();
                            image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> imageTask) {

                                    if(imageTask.isSuccessful())
                                    {
                                        String url = imageTask.getResult().toString();
                                        UserProfileChangeRequest profileChangeRequest= new UserProfileChangeRequest.Builder()
                                                .setDisplayName(username)
                                                .setPhotoUri(Uri.parse(url))
                                                .build();

                                        firebaseAuth.getCurrentUser().updateProfile(profileChangeRequest)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            UserModel userModel = new UserModel(email,username,url,true);
                                                            databaseReference.child(firebaseAuth.getUid())
                                                                    .setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    firebaseAuth.getCurrentUser().sendEmailVerification()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Toast.makeText(SignUpActivity.this,"Verify Sent Email",Toast.LENGTH_LONG).show();
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                        }

                                                    }
                                                });
                                    }else{
                                        Log.d("TAG","Oncomplete: Image Path" + imageTask.getException());
                                        Toast.makeText(SignUpActivity.this,"Image Path" + imageTask.getException(),Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                    });
                }else{
                    Log.d("TAG","Oncomplete: Create User" + signUp.getException());
                    Toast.makeText(SignUpActivity.this,""+signUp.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                imageUri=result.getUri();
                Glide.with(this).load(imageUri).into(binding.imgPick);
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception exception = result.getError();
                Log.d("TAG","OnActivityResult:" + exception);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == AllConstants.STORAGE_REQUEST_CODE)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                pickImage();
            }else{
                Toast.makeText(this,"Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}