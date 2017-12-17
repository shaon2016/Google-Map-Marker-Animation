package com.durbinlabs.googlemapmarkeranimation;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.durbinlabs.googlemapmarkeranimation.adapter.PlaceArrayAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UpdatePositionMovingTheMap extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private Marker marker;
    private Geocoder geocoder;
    private List<Address> addresses;
    private static final String TAG = DraggableMarker.class.getSimpleName();
    private GoogleMap mMap;
    private ImageView centerMarkerByImageView;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mAutocompleteTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_position_moving_the_map);
        centerMarkerByImageView = findViewById(R.id.centerMarkerByImageView);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        //auto complete api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        mAutocompleteTextView = (AutoCompleteTextView) findViewById(R.id
                .autoCompleteTextView);
        mAutocompleteTextView.setThreshold(1);

        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);

            Log.d("dataTag", place.getAddress() + "");

            marker.setPosition(place.getLatLng());
            marker.setTitle(place.getAddress() + "");
            marker.showInfoWindow();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(place.getLatLng()).zoom(10)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    };

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
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);

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

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(TAG, "Google Places API connection suspended.");
    }


    // move the map and update the address
    @Override
    public void onCameraIdle() {
        try {
            centerMarkerByImageView.setVisibility(View.GONE);
            LatLng midLatLng = mMap.getCameraPosition().target;
            if (midLatLng != null) {
                marker.remove();
                addresses = geocoder.getFromLocation(midLatLng.latitude,
                        midLatLng.longitude, 1); // Here 1 represent max location
                String address = addresses.get(0).getAddressLine(0);
                marker = mMap.addMarker(new MarkerOptions().position(midLatLng).title(address)
                        .draggable(true));
                marker.showInfoWindow();
            } else Toast.makeText(this, "No Location found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {
        centerMarkerByImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCameraMoveStarted(int i) {

    }
}