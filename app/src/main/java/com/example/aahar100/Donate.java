package com.example.aahar100;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Donate extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityDonateBinding binding;
    //new  code
    private AppCompatButton mButtonAddPin;
    private TextInputEditText foodItem,description;
    private TextInputLayout foodItemLayout,descriptionLayout;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    public static int LOCATION_REQUEST_CODE = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
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
        progressBar=findViewById(R.id.progressBar);
        mButtonAddPin = findViewById(R.id.button_add_pin);
        foodItem=findViewById(R.id.foodItem);
        description=findViewById(R.id.Description);
        foodItemLayout=findViewById(R.id.foodItemLayout);
        descriptionLayout=findViewById(R.id.DescriptionLayout);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mButtonAddPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchFoodAndDescription();
            }
        });
    }
    private void fetchFoodAndDescription() {
        if(!validateFood()  | !validateDescription()){
            return;
        }
        else{
            checkLocationPermission();
        }
    }
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }//runtime permission
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng userLocation = new LatLng(latitude, longitude);
                    String food=foodItem.getText().toString(),desc=description.getText().toString();
                    String name=getName();
                    //ADD a marker to user location
                    Marker marker =mMap.addMarker(new MarkerOptions().position(userLocation).title(name +"||"+food)
                            .icon(setIcon(Donate.this, R.drawable.baseline_room_service_24)));
                    // Disable the navigation button
                    mMap.getUiSettings().setMapToolbarEnabled(false);
                    //Animate the marker to the centre of the screen
                    marker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()),2000,null);
                    // saving suer location in FoodPinAvailable
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FoodPinAvailable");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(uid , new GeoLocation(latitude , longitude));
                    //making button and edu=it text un-clickable
                    mButtonAddPin.setEnabled(false);
                    foodItem.setEnabled(false);foodItem.setFocusable(false);
                    description.setEnabled(false);description.setFocusable(false);
                    //changing color of button
                    Drawable grayBackground = getResources().getDrawable(R.drawable.button_bg6);
                    mButtonAddPin.setBackgroundDrawable(grayBackground);
                    insertDataInHistoryNodeInDataBase();
                }
            }
        });
    }
    private void insertDataInHistoryNodeInDataBase(){
        Map<String , Object> map = new HashMap<>();
        map.put("food",foodItem.getText().toString());
        map.put("description",description.getText().toString());
        FirebaseDatabase.getInstance().getReference().child("History").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push()
                .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                      // do nothing
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Donate.this, "error while pushing data into History node", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            requestForPermission();
        }
    }
    private Boolean validateFood(){
        String val = foodItem.getText().toString();
        if(val.isEmpty())
        {
            foodItemLayout.setError("Field cannot be empty");
            foodItemLayout.requestFocus();
            return false;
        }
        else
        {
            foodItemLayout.setError(null);
            foodItemLayout.setErrorEnabled(false);
            return true;
        }
    }
    private Boolean validateDescription(){
        String val = description.getText().toString();
        if(val.isEmpty())
        {
            descriptionLayout.setError("Field cannot be empty");
            descriptionLayout.requestFocus();
            return false;
        }
        else
        {
            descriptionLayout.setError(null);
            descriptionLayout.setErrorEnabled(false);
            return true;
        }
    }
    private void requestForPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission Accepted", Toast.LENGTH_SHORT).show();
                getUserLocation();
            } else {
                Toast.makeText(this, "permission Rejected", Toast.LENGTH_SHORT).show();

            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Donate.this,R.raw.map_style));    // map style
    }
    private String getName() {
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        return firebaseUser.getDisplayName();
    }
    public BitmapDescriptor setIcon(Activity context, int drawableId) {
        Drawable drawable=ActivityCompat.getDrawable(context,drawableId);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        Bitmap bitmap=Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}