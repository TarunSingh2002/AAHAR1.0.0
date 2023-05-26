package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Receive extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityReceiveBinding binding;
    private FirebaseAuth authProfile;
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
                    Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(c).icon(setIcon(Receive.this, R.drawable.baseline_room_service_24)));
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
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Receive.this, R.raw.map_style));  // map style
        mMap.setPadding(0, 100, 0, 125); //left padding,top padding , right padding , bottom padding
    }
    public BitmapDescriptor setIcon(Activity context, int drawableId) {
        Drawable drawable = ActivityCompat.getDrawable(context, drawableId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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