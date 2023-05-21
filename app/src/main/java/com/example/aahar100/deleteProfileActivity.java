package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class deleteProfileActivity extends AppCompatActivity {
    private TextInputEditText editTextUserPwd;
    private TextInputLayout regPasswordLayout;
    private TextView texViewAuthenticated;
    private AppCompatButton buttonDeleteUser,buttonReAuthenticate;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private String userPwd;
    private ProgressBar progressBar;
    private static final  String TAG="deleteProfileActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        progressBar=findViewById(R.id.progressBar);
        texViewAuthenticated=findViewById(R.id.textViewYouAreNotAuthorized);
        buttonReAuthenticate=findViewById(R.id.Authentication);
        buttonDeleteUser=findViewById(R.id.deleteProfie);
        editTextUserPwd=findViewById(R.id.reg_oldpassword);
        regPasswordLayout=findViewById(R.id.oldpasswordLayout);
        buttonDeleteUser.setEnabled(false);
        authProfile=FirebaseAuth.getInstance();
        firebaseUser=authProfile.getCurrentUser();
        if(firebaseUser==null)
        {
            Toast.makeText(deleteProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(deleteProfileActivity.this , UserProfileActivity.class );
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else {
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser)  {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwd=editTextUserPwd.getText().toString();
                if(TextUtils.isEmpty(userPwd))
                {
                    Toast.makeText(deleteProfileActivity.this, "Password is needed", Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("Field cannot be empty");
                    editTextUserPwd.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    AuthCredential credential= EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                progressBar.setVisibility(View.GONE);
                                editTextUserPwd.setEnabled(false);

                                buttonReAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                Drawable blueBackground = getResources().getDrawable(R.drawable.button_bg4);
                                Drawable grayBackground = getResources().getDrawable(R.drawable.button_bg6);
                                buttonReAuthenticate.setBackgroundDrawable(grayBackground);
                                buttonDeleteUser.setBackgroundDrawable(blueBackground);

                                Toast.makeText(deleteProfileActivity.this, "Password has been verified. You can delete your profile now.", Toast.LENGTH_SHORT).show();
                                texViewAuthenticated.setText("You are authenticated. You can delete your profile now");
                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showAlertDialog();
                                    }
                                });
                            }
                            else{
                                try{
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(deleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        //setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(deleteProfileActivity.this);
        builder.setTitle("Delete Profile and Related Data");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible");

        //open Email Apps if user clicks/taps Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData(firebaseUser);
            }
        });
        //return back to mainActivity if USer presses cancel button
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent =new Intent(deleteProfileActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();
        //change the button color (continue->red)
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
                // Change the dialog box text color
                // Change the dialog box background color
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.button_bg1);

            }
        });
        //Show the AlertDialog
        alertDialog.show();
    }

    private void deleteUser() {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    authProfile.signOut();
                    Toast.makeText(deleteProfileActivity.this, "User has been deleted", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(deleteProfileActivity.this,sign_up.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    try{
                        throw task.getException();
                    }catch(Exception e)
                    {
                        Toast.makeText(deleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void deleteUserData(FirebaseUser firebaseUser) {
        //delete pic
        if(firebaseUser.getPhotoUrl()!=null)
        {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference=firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG,"onSuccess: Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(deleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        DatabaseReference databaseReferenceTwo = FirebaseDatabase.getInstance().getReference("FoodPinAvailable");
        DatabaseReference databaseReferenceThree = FirebaseDatabase.getInstance().getReference("History");
        // deleting Registered users
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: User data1 Deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(deleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //deleting FoodPins
        databaseReferenceTwo.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: User data2 Deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(deleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //deleting History
        databaseReferenceThree.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: User data2 Deleted");
                deleteUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(deleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}