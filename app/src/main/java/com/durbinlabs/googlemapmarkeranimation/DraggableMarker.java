package com.durbinlabs.googlemapmarkeranimation;

import android.graphics.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DraggableMarker extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {
    private Marker marker;
    private Geocoder geocoder;
    private List<Address> addresses;
    private static final String TAG = DraggableMarker.class.getSimpleName();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draggable_marker);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this, Locale.getDefault());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in " +
                "Sydney")
                .draggable
                        (true));
        marker.showInfoWindow();
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(10)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "Drag Started");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "Dragging");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG, "Drag End");

        try {
            LatLng newLatLng = new LatLng(marker.getPosition().latitude, marker.getPosition()
                    .longitude);
            addresses = geocoder.getFromLocation(newLatLng.latitude,
                    newLatLng.longitude, 1); // Here 1 represent max location
            // result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            marker.setTitle(address);
            marker.showInfoWindow();

            CameraPosition cameraPosition = new CameraPosition.Builder().target(newLatLng).zoom(10)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
