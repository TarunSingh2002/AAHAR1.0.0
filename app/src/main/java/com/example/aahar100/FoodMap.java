package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FoodMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityFoodMapBinding binding;
    private FirebaseAuth authProfile;
    private ChildEventListener childEventListener;
    private Map<String, Marker> markerMap; // Store marker references with child keys
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
        markerMap = new HashMap<>(); // Initialize the marker map
        DatabaseReference foodMapRef = FirebaseDatabase.getInstance().getReference("FoodMap");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                double a = dataSnapshot.child("lat").getValue(double.class);
                double b = dataSnapshot.child("lng").getValue(double.class);
                String c = dataSnapshot.child("name").getValue(String.class);
                LatLng userLocation = new LatLng(a, b);
                Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(c).icon(setIcon(FoodMap.this, R.drawable.baseline_room_service_24)));
                String childKey = dataSnapshot.getKey(); // Get the child key
                markerMap.put(childKey, marker); // Store the marker reference with the child key
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Handle the case when a child's data is updated
                Toast.makeText(FoodMap.this, "T1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(FoodMap.this, "T3", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the database operation
                Toast.makeText(FoodMap.this, "T4", Toast.LENGTH_SHORT).show();
            }
        };
        foodMapRef.addChildEventListener(childEventListener);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);  //zoom in out control
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(FoodMap.this,R.raw.map_style));  // map style
        mMap.setPadding(0,100,0,125); //left padding,top padding , right padding , bottom padding
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }
    public BitmapDescriptor setIcon(Activity context, int drawableId) {
        Drawable drawable=ActivityCompat.getDrawable(context,drawableId);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        Bitmap bitmap=Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
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
}