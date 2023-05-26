package com.example.aahar100;

import androidx.annotation.NonNull;
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
import android.view.WindowManager;
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
import com.example.aahar100.databinding.ActivityFoodMapBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FoodMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityFoodMapBinding binding;
    private FirebaseAuth authProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoodMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //full screen map showing
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        DatabaseReference foodMapRef = FirebaseDatabase.getInstance().getReference("FoodMap");
        foodMapRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve the user ID
                    //String userId = userSnapshot.getKey();
                    double a = childSnapshot.child("lat").getValue(double.class);
                    double b = childSnapshot.child("lng").getValue(double.class);
                    String c = childSnapshot.child("name").getValue(String.class);
                    LatLng userLocation = new LatLng(a,b);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(c).icon(setIcon(FoodMap.this, R.drawable.baseline_room_service_24)));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur during the database operation
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);  //zoom in out control
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(FoodMap.this,R.raw.map_style));  // map style
        mMap.setPadding(0,100,0,125); //left padding,top padding , right padding , bottom padding
        mMap.getUiSettings().setMapToolbarEnabled(false);
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
    /*
    public static int LOCATION_REQUEST_CODE = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
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

     private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            requestForPermission();
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
                    String name=getName();
                    //ADD a marker to user location
                    Marker marker =mMap.addMarker(new MarkerOptions().position(userLocation).title(name +"|| Donator")
                            .icon(setIcon(FoodMap.this, R.drawable.baseline_room_service_24)));
                    // Disable the navigation button
                    mMap.getUiSettings().setMapToolbarEnabled(false);
                    //Animate the marker to the centre of the screen
                    marker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,8));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()),2000,null);
                }
            }
        });
    }

    inside oncreate-->
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
   */
}