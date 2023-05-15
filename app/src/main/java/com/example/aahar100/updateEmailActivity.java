package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class updateEmailActivity extends AppCompatActivity {
    private AppCompatButton Authenticate,update_email;
    private TextInputEditText textNewEmail,textPwd;
    private TextInputLayout newEmailLayout,textPwdLayout;
    private TextView textViewAuthenticated,oldEmail;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    String userOldEmail,userNewEmail,userPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        progressBar=findViewById(R.id.progressBar);
        //editText and editTextLayout
        textPwdLayout=findViewById(R.id.passwordLayout);
        textPwd=findViewById(R.id.reg_password);
        newEmailLayout=findViewById(R.id.newEmail);
        textNewEmail=findViewById(R.id.newTextUserEmail);
        //buttons
        Authenticate=findViewById(R.id.Authentication);
        update_email=findViewById(R.id.updateEmail);
        //textView
        textViewAuthenticated=findViewById(R.id.textViewYouAreNotAuthorized);
        oldEmail=findViewById(R.id.email);
        update_email.setEnabled(false);
        textNewEmail.setEnabled(false);

        authProfile=FirebaseAuth.getInstance();
        firebaseUser=authProfile.getCurrentUser();
        userOldEmail=firebaseUser.getEmail();
        oldEmail.setText(userOldEmail);
        //if(firebaseUser.equals("")){
        if(firebaseUser == null) {
            Toast.makeText(updateEmailActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            Intent intent =new Intent(updateEmailActivity.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }else{
            reAuthenticate(firebaseUser);
        }
    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        Authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwd=textPwd.getText().toString();
                if(TextUtils.isEmpty(userPwd)){
                    newEmailLayout.setError("Email address is required");
                    newEmailLayout.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    AuthCredential credential= EmailAuthProvider.getCredential(userOldEmail,userPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(updateEmailActivity.this, "Password has been verified", Toast.LENGTH_SHORT).show();
                                textViewAuthenticated.setText("You can update your Email now");
                                update_email.setEnabled(true);
                                textNewEmail.setEnabled(true);
                                Authenticate.setEnabled(false);
                                textPwd.setEnabled(false);
                                Drawable blueBackground = getResources().getDrawable(R.drawable.button_bg4);
                                Drawable grayBackground = getResources().getDrawable(R.drawable.button_bg6);
                                Authenticate.setBackgroundDrawable(grayBackground);
                                update_email.setBackgroundDrawable(blueBackground);

                                update_email.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userNewEmail=textNewEmail.getText().toString();
                                        if(TextUtils.isEmpty(userNewEmail)) {
                                            newEmailLayout.setError("Email address is required");
                                            newEmailLayout.requestFocus();
                                        }
                                        else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                            newEmailLayout.setError("Invalid email address");
                                            newEmailLayout.requestFocus();

                                        }else if(userNewEmail.equals(userOldEmail)){
                                            newEmailLayout.setError("New Email cannot be same as old Email");
                                            newEmailLayout.requestFocus();
                                        }
                                        else{
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            }
                            else{
                                progressBar.setVisibility(View.GONE);
                                try{
                                    throw  task.getException();
                                } catch(Exception e){
                                    Toast.makeText(updateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete())
                {
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(updateEmailActivity.this, "Email has been updated. Please verify your new Email", Toast.LENGTH_SHORT).show();
                    Intent intent =new Intent(updateEmailActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else {
                    try{
                        throw task.getException();
                    }catch(Exception e)
                    {
                        Toast.makeText(updateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}