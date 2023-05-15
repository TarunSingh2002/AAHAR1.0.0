package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class changePasswordActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;
    private TextInputEditText editTextPwdCurr, editTextPwdNew,editTextPwdConfirmNew ;
    private TextInputLayout editTextPwdCurrLayout, editTextPwdNewLayout,editTextPwdConfirmNewLayout;
    private TextView textViewAuthenticated;
    private AppCompatButton buttonChangePwd,buttonReAuthenticate;
    private String userPwdCurr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        progressBar=findViewById(R.id.progressBar);
        //editText
        editTextPwdCurr=findViewById(R.id.reg_oldpassword);
        editTextPwdNew=findViewById(R.id.reg_password);
        editTextPwdConfirmNew=findViewById(R.id.reg_password_two);
        //editTextLayout
        editTextPwdCurrLayout=findViewById(R.id.oldpasswordLayout);
        editTextPwdNewLayout=findViewById(R.id.passwordLayout);
        editTextPwdConfirmNewLayout=findViewById(R.id.id15_password_two);
        //text-view
        textViewAuthenticated=findViewById(R.id.textViewYouAreNotAuthorized);
        //AppCompact-button
        buttonChangePwd=findViewById(R.id.updateEmail);
        buttonReAuthenticate=findViewById(R.id.Authentication);

        editTextPwdConfirmNew.setEnabled(false);
        editTextPwdNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser= authProfile.getCurrentUser();

        if(firebaseUser == null)
        {
            Toast.makeText(changePasswordActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            Intent intent =new Intent(changePasswordActivity.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else{
            reAuthenticateUser(firebaseUser);
        }
    }
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwdCurr=editTextPwdCurr.getText().toString();
                if(TextUtils.isEmpty(userPwdCurr))
                {
                    editTextPwdCurrLayout.setError("Password did not match");
                    editTextPwdCurrLayout.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    AuthCredential credential= EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwdCurr);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                progressBar.setVisibility(View.GONE);
                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);
                                editTextPwdConfirmNew.setEnabled(true);

                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePwd.setEnabled(true);

                                Toast.makeText(changePasswordActivity.this, "Password has been verified. You can change password now", Toast.LENGTH_SHORT).show();

                                Drawable blueBackground = getResources().getDrawable(R.drawable.button_bg4);
                                Drawable grayBackground = getResources().getDrawable(R.drawable.button_bg6);
                                buttonReAuthenticate.setBackgroundDrawable(grayBackground);
                                buttonChangePwd.setBackgroundDrawable(blueBackground);
                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }
                            else{
                                try{
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(changePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {

        String userPwdNew=editTextPwdNew.getText().toString();
        String userPwdConfirmNew=editTextPwdConfirmNew.getText().toString();
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";
        if(TextUtils.isEmpty(userPwdNew))
        {
            editTextPwdNewLayout.setError("Field cannot be empty");
            editTextPwdNewLayout.requestFocus();
        }
        else if(userPwdCurr.matches(userPwdNew))
        {
            editTextPwdNewLayout.setError("New Password can not be same as old Password");
            editTextPwdNewLayout.requestFocus();
        }
        else if (!userPwdNew.matches(passwordVal)) {
            editTextPwdNewLayout.setError("Password is too weak");
            editTextPwdNewLayout.requestFocus();
        }
        else if(TextUtils.isEmpty(userPwdConfirmNew))
        {
            editTextPwdConfirmNewLayout.setError("Field cannot be empty");
            editTextPwdConfirmNewLayout.requestFocus();
        }
        else if(!userPwdNew.matches(userPwdConfirmNew))
        {
            editTextPwdConfirmNewLayout.setError("Password did not match");
            editTextPwdConfirmNewLayout.requestFocus();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(changePasswordActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(changePasswordActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        try{
                            throw task.getException();
                        }catch(Exception e)
                        {
                            Toast.makeText(changePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}