package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
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
import com.example.aahar100.databinding.ActivityReceiveBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Receive extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityReceiveBinding binding;
    private FirebaseAuth authProfile;
    private ChildEventListener childEventListener;
    private Map<String, Marker> markerMap; // Store marker references with child keys
    //new code
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker userMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiveBinding.inflate(getLayoutInflater());
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
        //new code
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        authProfile = FirebaseAuth.getInstance();
        markerMap = new HashMap<>(); // Initialize the marker map
        DatabaseReference foodMapRef = FirebaseDatabase.getInstance().getReference("FoodMap");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                double a = dataSnapshot.child("lat").getValue(double.class);
                double b = dataSnapshot.child("lng").getValue(double.class);
                String c = dataSnapshot.child("name").getValue(String.class);
                String d= dataSnapshot.child("number").getValue(String.class);
                String e= dataSnapshot.child("food").getValue(String.class);
                LatLng userLocation = new LatLng(a, b);
                if (d.equals("not a number flag")) {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(c+"||"+e).icon(setIcon(Receive.this, R.drawable.marker_donator_style)));
                    String childKey = dataSnapshot.getKey(); // Get the child key
                    markerMap.put(childKey, marker); // Store the marker reference with the child key
                } else {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(c+"||"+d+"||"+e).icon(setIcon(Receive.this, R.drawable.marker_donator_style)));
                    String childKey = dataSnapshot.getKey(); // Get the child key
                    markerMap.put(childKey, marker); // Store the marker reference with the child key
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Handle the case when a child's data is updated
                Toast.makeText(Receive.this, "T1", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle the case when a child is removed
                String childKey = dataSnapshot.getKey(); // Get the child key
                Marker marker = markerMap.get(childKey); // Get the corresponding marker
                if (marker != null) {
                    marker.remove(); // Remove the marker from the map
                    markerMap.remove(childKey); // Remove the marker reference from the map
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Handle the case when a child changes position within the list
                Toast.makeText(Receive.this, "T3", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the database operation
                Toast.makeText(Receive.this, "T4", Toast.LENGTH_SHORT).show();
            }
        };
        foodMapRef.addChildEventListener(childEventListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);  //zoom in out control
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Receive.this, R.raw.map_style));  // map style
        mMap.setPadding(0, 100, 0, 125); //left padding,top padding , right padding , bottom padding
        //new code
        checkLocationPermission();
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
    protected void onDestroy() {
        super.onDestroy();
        // Remove the child event listener when the activity is destroyed
        DatabaseReference foodMapRef = FirebaseDatabase.getInstance().getReference("FoodMap");
        foodMapRef.removeEventListener(childEventListener);
    }

    //new code
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get user's location
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }//runtime permission check
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // User location obtained, move camera and add marker
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            if (userMarker != null) {
                                userMarker.remove();
                            }
                            userMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(getName()+"|| Your Location").icon(setIcon(Receive.this, R.drawable.framereceiver_two)));
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 20);
                            mMap.animateCamera(cameraUpdate);
                        } else {
                            Toast.makeText(Receive.this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionGranted = false;
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                    break;
                }
            }
            if (permissionGranted) {
                getUserLocation();
            }
            // Start the main activity regardless of permission result
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private String getName() {
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        return firebaseUser.getDisplayName();
    }
}
//DonateIdMapping
//UniqueIdList
//FoodMap