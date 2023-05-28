package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.aahar100.databinding.ActivityDonateBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Donate extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityDonateBinding binding;
    private AppCompatButton mButtonAddPin;
    private TextInputEditText foodItem, description;
    private TextInputLayout foodItemLayout, descriptionLayout;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static double latitude;
    private static double longitude;
    String number ="not a number flag";
    private LocationManager locationManager;
    public static int LOCATION_REQUEST_CODE = 100;
    private void FoodMapDataPushFunction(String key) {
        LatLng userLocation = new LatLng(latitude, longitude);
        String food = foodItem.getText().toString(), desc = description.getText().toString();
        String name = getName(), val = foodItem.getText().toString();
        DatabaseReference refTwo = FirebaseDatabase.getInstance().getReference("FoodMap");
        ReadWriteLocation readWriteLocation = new ReadWriteLocation(latitude, longitude, getName() , number, val);
        refTwo.child(key).setValue(readWriteLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Donate.this, "isSuccessful", Toast.LENGTH_SHORT).show();
                    //ADD a marker to user location
                    Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(name + "||" + food).icon(setIcon(Donate.this, R.drawable.marker_donator_style)));
                    // Disable the navigation button
                    mMap.getUiSettings().setMapToolbarEnabled(false);
                    //Animate the marker to the centre of the screen
                    marker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 2000, null);
                    //making button and edu=it text un-clickable
                    mButtonAddPin.setEnabled(false);
                    foodItem.setEnabled(false);
                    foodItem.setFocusable(false);
                    description.setEnabled(false);
                    description.setFocusable(false);
                    //changing color of button
                    Drawable grayBackground = getResources().getDrawable(R.drawable.button_bg6);
                    mButtonAddPin.setBackgroundDrawable(grayBackground);
                    insertDataInHistoryNodeInDataBase();
                } else {
                    Toast.makeText(Donate.this, "error3", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void MapWorkingRandomKeyWithUid(String key) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DonateIdMapping");
        ReadWriteMapDonationAndUserId readWriteMapDonationAndUserId = new ReadWriteMapDonationAndUserId(key);
        ref.child(firebaseUser.getUid()).setValue(readWriteMapDonationAndUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // FoodMapPinFlag = true;
                    FoodMapDataPushFunction(key);
                } else {
                    Toast.makeText(Donate.this, "error2", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void WorkingRandomKey() {
        String randomKey = generateRandomKey();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uniqueIdListRef = databaseReference.child("UniqueIdList");
        uniqueIdListRef.orderByValue().equalTo(randomKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Toast.makeText(Donate.this,"exist", Toast.LENGTH_SHORT).show();
                    WorkingRandomKey();
                } else {
                    //Toast.makeText(Donate.this,randomKey, Toast.LENGTH_SHORT).show();
                    uniqueIdListRef.push().setValue(randomKey);
                    MapWorkingRandomKeyWithUid(randomKey); // do proper mapping + also on success place a pin
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Donate.this, "error1", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static String generateRandomKey() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int length = 10;
        StringBuilder keyBuilder = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            keyBuilder.append(randomChar);
        }

        String key = keyBuilder.toString();

        if (!key.matches(".*\\d.*")) {
            // If the key doesn't contain a digit, replace a random character with a random digit
            int randomIndex = random.nextInt(length);
            key = key.substring(0, randomIndex) + random.nextInt(10) + key.substring(randomIndex + 1);
        }

        if (!key.matches(".*[A-Z].*")) {
            // If the key doesn't contain an uppercase letter, replace a random character with a random uppercase letter
            int randomIndex = random.nextInt(length);
            key = key.substring(0, randomIndex) + (char) (random.nextInt(26) + 'A') + key.substring(randomIndex + 1);
        }

        return key;
    }
    private void insertDataInHistoryNodeInDataBase() {
        Map<String, Object> map = new HashMap<>();
        map.put("food", foodItem.getText().toString());
        map.put("description", description.getText().toString());
        FirebaseDatabase.getInstance().getReference().child("History").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push()
                .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // do nothing
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Donate.this, "error while pushing data into History node", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //--------------------------------------------------------------------------------------------
    private void fetchFoodAndDescription() {
        if (!validateFood() | !validateDescription()) {
            return;
        } else {
            showAlertDialog();
        }
    }
    private Boolean validateFood() {
        String val = foodItem.getText().toString();
        if (val.isEmpty()) {
            foodItemLayout.setError("Field cannot be empty");
            foodItemLayout.requestFocus();
            return false;
        } else {
            foodItemLayout.setError(null);
            foodItemLayout.setErrorEnabled(false);
            return true;
        }
    }
    private Boolean validateDescription() {
        String val = description.getText().toString();
        if (val.isEmpty()) {
            descriptionLayout.setError("Field cannot be empty");
            descriptionLayout.requestFocus();
            return false;
        } else {
            descriptionLayout.setError(null);
            descriptionLayout.setErrorEnabled(false);
            return true;
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Donate.this, R.raw.map_style));    // map style
    }
    private String getName() {
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        return firebaseUser.getDisplayName();
    }
    public BitmapDescriptor setIcon(Activity context, int drawableId) {
        Drawable drawable = ActivityCompat.getDrawable(context, drawableId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        progressBar = findViewById(R.id.progressBar);
        mButtonAddPin = findViewById(R.id.button_add_pin);
        foodItem = findViewById(R.id.foodItem);
        description = findViewById(R.id.Description);
        foodItemLayout = findViewById(R.id.foodItemLayout);
        descriptionLayout = findViewById(R.id.DescriptionLayout);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestForPermission();
        }//runtime permission
        else {
            // Get the user's current location
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            // Save the latitude and longitude as global double variables
            Donate.latitude = latitude;
            Donate.longitude = longitude;
            mButtonAddPin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressBar.setVisibility(View.VISIBLE);
                    fetchFoodAndDescription();
                }
            });
        }
    }
    private void requestForPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Is Permission Accepted For This User", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Donate.this, UserProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "permission Rejected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static double getLatitude() {
        return latitude;
    }
    public static double getLongitude() {
        return longitude;
    }
    private void showAlertDialog() {
        //setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Donate.this);
        builder.setTitle("Phone number ");
        builder.setMessage("Do you want to show your Phone number show to receivers, it will help to connect with you");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                        if (readUserDetails != null) {
                            number = readUserDetails.mobile;
                            WorkingRandomKey();
                        } else {
                            Toast.makeText(Donate.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Donate.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(Donate.this, "you rejected Number request", Toast.LENGTH_SHORT).show();
                WorkingRandomKey();
            }
        });
        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();
        //change the button color (continue->red)
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.light_blue));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.light_blue));
                // Change the dialog box text color
                // Change the dialog box background color
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.button_bg1);

            }
        });
        //Show the AlertDialog
        alertDialog.show();
    }
}