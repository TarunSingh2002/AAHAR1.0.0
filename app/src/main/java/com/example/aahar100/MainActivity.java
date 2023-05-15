package com.example.aahar100;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//texting
public class MainActivity extends AppCompatActivity {

    private CardView cardDonate,cardReceive,cardFoodMap,cardMyPin,cardHistory,profile,menu_setting,menu_logout;
    private FirebaseAuth authProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardDonate=findViewById(R.id.cardDonate);
        cardReceive=findViewById(R.id.cardReceive);
        cardFoodMap=findViewById(R.id.cardFoodMap);
        cardMyPin=findViewById(R.id.cardMyPin);
        cardHistory=findViewById(R.id.cardHistory);
        profile=findViewById(R.id.profile);
        menu_setting=findViewById(R.id.menu_setting);
        menu_logout=findViewById(R.id.menu_logout);

        authProfile=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=authProfile.getCurrentUser();

        if(firebaseUser == null)
        {
            Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        } else {
            checkIfEmailVerified(firebaseUser);
        }
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        menu_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, setting_activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        menu_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authProfile.signOut();
                Intent intent = new Intent(MainActivity.this, landing_page.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        /*cardReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, myPin.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        }); */
        cardDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Donate.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified())
        {
            showAlertDialog();
        }
    }
    private void showAlertDialog() {
        //setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. If you have already verified your email close the app and Restart the application again");
        builder.setCancelable(false);
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
        //Show the AlertDialog
        alertDialog.show();
    }
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        if(firebaseUser != null) {
            firebaseUser.reload();
            if(!firebaseUser.isEmailVerified()) {
                showAlertDialog();
            }
        }
    }
}