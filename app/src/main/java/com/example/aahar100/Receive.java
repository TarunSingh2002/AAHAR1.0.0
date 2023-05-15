package com.example.aahar100;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.aahar100.databinding.ActivityReceiveBinding;
import com.google.firebase.auth.FirebaseAuth;

public class Receive extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityReceiveBinding binding;
    FirebaseAuth auth;
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
        auth=FirebaseAuth.getInstance();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);  //zoom in out control
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Receive.this,R.raw.map_style));  // map style
        mMap.setPadding(0,100,0,125); //left padding,top padding , right padding , bottom padding

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User does not exist", Toast.LENGTH_LONG).show();
            return;
        }else{
            Toast.makeText(this,auth.getCurrentUser().getUid(), Toast.LENGTH_LONG).show();
        }
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}