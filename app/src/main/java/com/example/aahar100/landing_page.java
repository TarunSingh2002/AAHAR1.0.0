package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class landing_page extends AppCompatActivity {
    private TextInputLayout regEmailLayout , regPasswordLayout;
    private TextInputEditText editTextLoginEmail,editTextLoginPwd;
    private FirebaseAuth authProfile;
    private static final String  TAG="landing_page";
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        //Hooks
        regEmailLayout=findViewById(R.id.id7_email);
        regPasswordLayout=findViewById(R.id.id7_password);
        editTextLoginEmail=findViewById(R.id.id7_textUserEmail);
        editTextLoginPwd=findViewById(R.id.id7_textUserPassword);
        AppCompatButton forgetPassword=findViewById(R.id.id8_Forget_password);
        AppCompatButton callSignUp=findViewById(R.id.id10_sign_in);
        Button buttonLogin=findViewById(R.id.id9_login);
        authProfile=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progressBar);
        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(landing_page.this, sign_up.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(landing_page.this, forgotPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateEmail() | !validatePassword()){
                    return;
                }
                else{
                    String textEmail = editTextLoginEmail.getText().toString();
                    String textPwd = editTextLoginPwd.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail,textPwd);
                }
            }
        });
    }

    private void loginUser(String email, String pwd) {
        authProfile.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(landing_page.this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    //get instance of current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();
                    //Check if email is verified before  user can access  there profile
                    if(firebaseUser.isEmailVerified()){
                        startActivity(new Intent(landing_page.this,MainActivity.class));
                        finish();
                    }
                    else {
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        ShowAlertDialog();
                    }
                    progressBar.setVisibility(View.GONE);
                }
                else{
                    try
                    {
                        throw task.getException();
                    }
                    catch(FirebaseAuthInvalidUserException e){
                        regEmailLayout.setError("User does not exist");
                        regEmailLayout.requestFocus();
                    }
                    catch(FirebaseAuthInvalidCredentialsException e){
                        String errorCode = e.getErrorCode();
                        if (errorCode.equals("ERROR_INVALID_EMAIL")) {
                            regEmailLayout.setError("Invalid email address");
                            regEmailLayout.requestFocus();
                        } else if (errorCode.equals("ERROR_WRONG_PASSWORD")) {
                            regPasswordLayout.setError("Incorrect password");
                            regPasswordLayout.requestFocus();
                        } else {
                            regEmailLayout.setError("Invalid credential");
                            regEmailLayout.requestFocus();
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(landing_page.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void ShowAlertDialog() {
        //setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(landing_page.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification.");

        //open Email Apps if user clicks/taps Continue button
        builder.setPositiveButton("Get Verified", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent =new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   //TO email app in new Window and not within our app
                startActivity(intent);
            }
        });
        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();
        //change the button color (continue->blue)
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.light_blue));
                // Change the dialog box background color
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.button_bg1);

            }
        });
        //Show the AlertDialog
        alertDialog.show();
    }

    private boolean validateEmail() {
        String email = editTextLoginEmail.getText().toString();
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
    private Boolean validatePassword() {
        String val = editTextLoginPwd.getText().toString();
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

    //check if user is already logged in. In such case, Straightaway take the user to the User's profile
    @Override
    protected void onStart() {
        super.onStart();
        if(authProfile.getCurrentUser() != null){
            //start main activity
            startActivity(new Intent(landing_page.this,MainActivity.class));
            finish();
        }
        else{
            /* Toast.makeText(landing_page.this,"Please first login!",Toast.LENGTH_LONG).show();*/
        }
    }
}

