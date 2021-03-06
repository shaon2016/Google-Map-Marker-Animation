package com.durbinlabs.googlemapmarkeranimation;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.durbinlabs.googlemapmarkeranimation.Other.Common;
import com.durbinlabs.googlemapmarkeranimation.Remote.IGoogleApi;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int ANIMATE_SPEEED = 1500;
    private static final int BEARING_OFFSET = 20;
    private static final int ANIMATE_SPEEED_TURN = 1000;
    private final Interpolator interpolator = new LinearInterpolator();
    private final long start = SystemClock.uptimeMillis();

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private List<LatLng> polyLinesList;
    private Marker marker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition;
    private int index, next, stepOfPolyLine;
    private Button btnGo;
    private EditText evSearch;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylinesOptions;
    private Polyline blackPolyline, greyPolyline;

    private LatLng myPosition;

    private IGoogleApi service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // mapFragment.getMapAsync(this);

        polyLinesList = new ArrayList<>();
        btnGo = findViewById(R.id.btnSearch);
        evSearch = findViewById(R.id.evSearch);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destination = evSearch.getText().toString();
                destination.replace(" ", "+");
                mapFragment.getMapAsync(MapsActivity.this);
            }
        });

        service = Common.getGoogleApi();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker in Sydney and move the camera
        final LatLng mirpur = new LatLng(23.8223, 90.3654);
        final LatLng dhanmondi = new LatLng(23.7465, 90.3760);
        mMap.addMarker(new MarkerOptions().position(mirpur).title("Marker in Mirpur"));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mirpur)
                .zoom(17)
                .bearing(30)
                .tilt(45)
                .build();
        //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                ANIMATE_SPEEED_TURN,null);

        String requestUrl = null;

        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "origin=" + mirpur.latitude + "," + mirpur.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_maps_key);
            Log.d("URL", requestUrl);
            service.getDataFromGoogle(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        Log.d("Response", response.body().toString());
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            Log.d("PolyLines", "PolyLines in String: " + polyline + "");
                            polyLinesList = decodePoly(polyline);
                        }

                        Log.d("PolyLines", polyLinesList.size() + "");

                        // Adjusting bounds
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : polyLinesList)
                            builder.include(latLng);

                        LatLngBounds latLngBounds = builder.build();
                        final CameraUpdate cameraUpdate =
                                CameraUpdateFactory.newLatLngBounds(latLngBounds, 2);
                        mMap.animateCamera(cameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLinesList);
                        greyPolyline = mMap.addPolyline(polylineOptions);

                        blackPolylinesOptions = new PolylineOptions();
                        blackPolylinesOptions.color(Color.BLACK);
                        blackPolylinesOptions.width(5);
                        blackPolylinesOptions.startCap(new SquareCap());
                        blackPolylinesOptions.endCap(new SquareCap());
                        blackPolylinesOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLinesList);
                        blackPolyline = mMap.addPolyline(blackPolylinesOptions);

                        mMap.addMarker(new MarkerOptions().position(
                                polyLinesList.get(polyLinesList.size() - 1)));

                        //Animator
                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                        polyLineAnimator.setDuration(2000);
                        polyLineAnimator.setInterpolator(new LinearInterpolator());
                        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                List<LatLng> points = greyPolyline.getPoints();
                                int percentValue = (int) animation.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size * (percentValue / 100.0f));
                                List<LatLng> p = points.subList(0, newPoints);
                                blackPolyline.setPoints(p);
                            }
                        });
                        polyLineAnimator.start();

                        marker = mMap.addMarker(new MarkerOptions().position(mirpur).flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable
                                        .ic_car_icon)));

                        Log.d("Data", polyLinesList.size() + "");
                        //Movement
                        handler = new Handler();
                        index = -1;
                        next = 1;

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if (index < polyLinesList.size() - 1) {
                                    index++;
                                    next = index + 1;
                                }

                                if (index < polyLinesList.size() - 1) {
                                    startPosition = polyLinesList.get(index);
                                    endPosition = polyLinesList.get(next);
                                }

                                final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                                valueAnimator.setDuration(3000);
                                valueAnimator.setInterpolator(new LinearInterpolator());
                                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        v = animation.getAnimatedFraction();
                                        lng = v * endPosition.longitude + (1 - v)
                                                * startPosition.longitude;
                                        lat = v * endPosition.latitude + (1 - v)
                                                * startPosition.latitude;
                                        Log.d("LatLng", "New Post Lat Lng: " + lat + " "
                                                + lng);
                                        LatLng newPos = new LatLng(lat, lng);

                                        marker.setPosition(newPos);
                                        marker.setAnchor(0.5f, 0.5f);
                                        //marker.setRotation(getBearing(startPosition, newPos));

                                        marker.setRotation(bearingBetweenLatLngs(startPosition,
                                                newPos));

                                        CameraPosition cameraPosition = new CameraPosition
                                                .Builder()
                                                .target(newPos)
                                                .zoom(15f)
                                                .build();

                                        mMap.animateCamera(CameraUpdateFactory
                                        .newCameraPosition(cameraPosition), 2000, null);

//                                        mMap.moveCamera(CameraUpdateFactory.
//                                                newCameraPosition(cameraPosition));

                                    }
                                });
                                valueAnimator.start();
                                handler.postDelayed(this, 16);

                            }
                        }, 3000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(MapsActivity.this, ""
                            + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private float getBearing(LatLng startPosition, LatLng newPost) {
        double lat = Math.abs(startPosition.latitude - newPost.latitude);
        double lng = Math.abs(startPosition.longitude - newPost.longitude);

        if (startPosition.latitude < newPost.latitude && startPosition.longitude < newPost.longitude)
            return (float) Math.toDegrees(Math.atan(lng / lat));
        else if (startPosition.latitude >= newPost.latitude && startPosition.longitude < newPost.longitude)
            return (float) (Math.toDegrees(90 - Math.atan(lng / lat)) + 90);
        else if (startPosition.latitude >= newPost.latitude && startPosition.longitude >= newPost.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (startPosition.latitude < newPost.latitude && startPosition.longitude >= newPost.longitude)
            return (float) (Math.toDegrees(90 - Math.atan(lng / lat)) + 270);
        return -1;
    }

    public static List<LatLng> decodePoly(final String encodedPath) {
        int len = encodedPath.length();

        // For speed we preallocate to an upper bound on the final length, then
        // truncate the array before returning.
        final List<LatLng> path = new ArrayList<LatLng>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }

        return path;
    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private float bearingBetweenLatLngs(LatLng begin, LatLng end) {
        Location beginL = convertLatLngToLocation(begin);
        Location endL = convertLatLngToLocation(end);

        return beginL.bearingTo(endL);
    }

    private void changeCameraPosition(CameraPosition cameraPosition, boolean animate) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        if (animate) {
            mMap.animateCamera(cameraUpdate);
        } else {
            mMap.moveCamera(cameraUpdate);
        }
    }
}
