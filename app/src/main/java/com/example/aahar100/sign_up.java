package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class sign_up extends AppCompatActivity {
    //Variables
    private TextInputEditText regName, regEmail, regPhoneNo, regPassword , regPasswordTwo;
    private TextInputLayout regNameLayout , regEmailLayout, regPhoneNoLayout, regPasswordLayout , regPasswordTwoLayout;
    private static final String TAG="sign_up";
    private ProgressBar progressBar;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        AppCompatButton regBtn, regToLoginBtn;
        regToLoginBtn=findViewById(R.id.id17_reg_login_btn);
        regBtn=findViewById(R.id.id16_reg_btn);

        regName=findViewById(R.id.reg_name);
        regEmail=findViewById(R.id.reg_email);
        regPhoneNo=findViewById(R.id.reg_phoneNo);
        regPassword=findViewById(R.id.reg_password);
        regPasswordTwo=findViewById(R.id.reg_password_two);

        regNameLayout=findViewById(R.id.id11_name);
        regEmailLayout=findViewById(R.id.id13_email);
        regPhoneNoLayout=findViewById(R.id.id14_phone_Number);
        regPasswordLayout=findViewById(R.id.id15_password);
        regPasswordTwoLayout=findViewById(R.id.id15_password_two);
        progressBar=findViewById(R.id.progressBar);
        regToLoginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(sign_up.this, landing_page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateName() | !validatePassword() | !validatePhoneNo() | !validateEmail() | !validateConfirmPassword()){
                    return;
                }
                String textFullName = Objects.requireNonNull(regName.getText()).toString();
                String  textEmail= Objects.requireNonNull(regEmail.getText()).toString();
                String textMobile = Objects.requireNonNull(regPhoneNo.getText()).toString();
                String textPwd = Objects.requireNonNull(regPassword.getText()).toString();
                String textConfirmPwd = Objects.requireNonNull(regPasswordTwo.getText()).toString();
                progressBar.setVisibility(View.VISIBLE);
                registerUser(textFullName,textEmail,textMobile,textPwd,textConfirmPwd);
            }
        });
    }
    private void registerUser(String textFullName, String textEmail, String textMobile, String textPwd, String textConfirmPwd) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //create user profile
        auth.createUserWithEmailAndPassword(textEmail,textPwd).addOnCompleteListener(sign_up.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    //update Display Name of user
                    UserProfileChangeRequest profileChangeRequest=new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileChangeRequest);
                    //Entre user Data into this FireBase Realtime Database
                    ReadWriteUserDetails writeUserDetails =new ReadWriteUserDetails(textMobile);

                    //Extracting user reference from DB for "Registered Users"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                // sending verification email to the user
                                firebaseUser.sendEmailVerification();
                                AlertDialog.Builder builder = new AlertDialog.Builder(sign_up.this);
                                builder.setTitle("Email Not Verified");
                                builder.setMessage("Please verify your email now. You can not login without email verification next time.");
                                builder.setCancelable(false);
                                builder.setPositiveButton("Get Verified", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent =new Intent(Intent.ACTION_MAIN);
                                        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   //TO email app in new Window and not within our app
                                        startActivity(intent);
                                        alertDialog.dismiss();
                                        closeAppDelayed();
                                    }
                                });
                                alertDialog = builder.create();
                                alertDialog.show();
                            }
                            else
                            {
                                Toast.makeText(sign_up.this, "Registration failed", Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }else{
                    try{
                        throw Objects.requireNonNull(task.getException());
                    }catch(FirebaseAuthWeakPasswordException e){
                        regPassword.setError("Password is too weak");
                        regPassword.requestFocus();
                    }catch(FirebaseAuthInvalidCredentialsException e)
                    {
                        regEmail.setError("Invalid Email address");
                        regEmail.requestFocus();
                    }
                    catch(FirebaseAuthUserCollisionException e)
                    {
                        regEmail.setError("Email is already registered");
                        regEmail.requestFocus();
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(sign_up.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    private void closeAppDelayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Close the app
                finish();

                // Remove the app from app history
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // Forcefully kill the app process
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }, 2000);   //Wait for 2 seconds before closing the app to allow the email app to open
    }


    private Boolean validateConfirmPassword()
    {
        String val = Objects.requireNonNull(regPassword.getText()).toString();
        String val2 = Objects.requireNonNull(regPasswordTwo.getText()).toString();
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";
        if (val.isEmpty()) {
            regPasswordTwoLayout.setError("Password Field cannot be empty");
            regPasswordTwoLayout.requestFocus();
            return false;
        } else if (!val.matches(passwordVal)) {
            regPasswordTwoLayout.setError("Password is too weak");
            regPasswordTwoLayout.requestFocus();
            return false;
        }else if(!val2.equals(val)){
            regPasswordTwoLayout.setError("Password confirmation is required");
            regPasswordTwoLayout.requestFocus();
            return false;
        }
        else {
            regPasswordTwoLayout.setError(null);
            regPasswordTwoLayout.setErrorEnabled(false);
            return true;
        }
    }
    private Boolean validateName(){
        String val = Objects.requireNonNull(regName.getText()).toString();
        if(val.isEmpty())
        {
            regNameLayout.setError("Field cannot be empty");
            regNameLayout.requestFocus();
            return false;
        }
        else
        {
            regNameLayout.setError(null);
            regNameLayout.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = Objects.requireNonNull(regEmail.getText()).toString();
        if (email.isEmpty()) {
            regEmailLayout.setError("Email address is required");
            regEmailLayout.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmailLayout.setError("Invalid email address");
            regEmailLayout.requestFocus();
            return false;
        } else {
            regEmailLayout.setError(null);
            return true;
        }
    }

    private boolean validatePhoneNo() {
        String phoneNumber = Objects.requireNonNull(regPhoneNo.getText()).toString();
        if (phoneNumber.isEmpty()) {
            regPhoneNoLayout.setError("Phone number is required");
            regPhoneNoLayout.requestFocus();
            return false;
        } else if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            regPhoneNoLayout.setError("Invalid phone number");
            regPhoneNoLayout.requestFocus();
            return false;
        } else {
            regPhoneNoLayout.setError(null);
            return true;
        }
    }
    private Boolean validatePassword() {
        String val = Objects.requireNonNull(regPassword.getText()).toString();
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";
        if (val.isEmpty()) {
            regPasswordLayout.setError("Field cannot be empty");
            regPasswordLayout.requestFocus();
            return false;
        } else if (!val.matches(passwordVal)) {
            regPasswordLayout.setError("Password is too weak");
            regPasswordLayout.requestFocus();
            return false;
        } else {
            regPasswordLayout.setError(null);
            regPasswordLayout.setErrorEnabled(false);
            return true;
        }
    }

}
