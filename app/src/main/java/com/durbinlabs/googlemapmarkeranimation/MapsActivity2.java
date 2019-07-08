package com.durbinlabs.googlemapmarkeranimation;

import android.app.ActionBar;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hp on 12/4/2017.
 */
public class MapsActivity2 extends AppCompatActivity implements OnMapReadyCallback {
    // Keep track of our markers
    private List<Marker> markers = new ArrayList<Marker>();
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private final Handler mHandler = new Handler();

    private Marker selectedMarker;

    Handler handler = new Handler();
    Random random = new Random();
    Runnable runner = new Runnable() {
        @Override
        public void run() {

        }
    };
    Button btnRemove, btnClear, btnAdd, btnStartAnim, btnStopAnim, btnToggle;

    protected Toolbar getToolBar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        Toolbar toolbar = getToolBar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity2.this);

//        btnRemove = findViewById(R.id.action_bar_remove_location);
//        btnClear = findViewById(R.id.action_bar_clear_locations);
//        btnToggle = findViewById(R.id.action_bar_toggle_style);
//        btnStopAnim = findViewById(R.id.action_bar_stop_animation);
//        btnStartAnim = findViewById(R.id.action_bar_start_animation);
//        btnAdd = findViewById(R.id.action_bar_add_default_locations);
//
//        btnRemove.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                removeSelectedMarker();
//            }
//        });
//        btnAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addDefaultLocations();
//            }
//        });
//        btnClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                clearMarkers();
//            }
//        });
//        btnStartAnim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                animator.startAnimation(true);
//            }
//        });
//        btnStopAnim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                animator.stopAnimation();
//            }
//        });
//        btnToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleStyle();
//            }
//        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        final LatLng mirpur = new LatLng(23.8223, 90.3654);
        addMarkerToMap(mirpur);
        Marker marker = this.googleMap.addMarker(new MarkerOptions().position(mirpur).title
                ("Marker " +
                        "in Mirpur"));
        markers.add(marker);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(mirpur));
        this.googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(googleMap.getCameraPosition().target)
                .zoom(10)
                .bearing(30)
                .tilt(45)
                .build()));

    }

    private void addDefaultLocations() {
        addMarkerToMap(new LatLng(23.7465, 90.3760));
        addMarkerToMap(new LatLng(23.7925, 90.4078));
        addMarkerToMap(new LatLng(23.7384, 90.3959));
        addMarkerToMap(new LatLng(22.3475, 91.8123));
        addMarkerToMap(new LatLng(23.644480, 90.598434));
//        addMarkerToMap(new LatLng(50.95936754348453, 3.518972061574459));
//        addMarkerToMap(new LatLng(50.95877285446026, 3.5199161991477013));
//        addMarkerToMap(new LatLng(50.958179213755905, 3.520646095275879));
//        addMarkerToMap(new LatLng(50.95901719316589, 3.5222768783569336));
//        addMarkerToMap(new LatLng(50.95954430150347, 3.523542881011963));
//        addMarkerToMap(new LatLng(50.95873336312275, 3.5244011878967285));
//        addMarkerToMap(new LatLng(50.95955781702322, 3.525688648223877));
//        addMarkerToMap(new LatLng(50.958855004782116, 3.5269761085510254));
    }


    private Animator animator = new Animator();


    int currentPt;

    GoogleMap.CancelableCallback MyCancelableCallback =
            new GoogleMap.CancelableCallback() {
                @Override
                public void onCancel() {
                    System.out.println("onCancelled called");
                }
                @Override
                public void onFinish() {
                    if (++currentPt < markers.size()) {
                        float targetBearing = bearingBetweenLatLngs(googleMap.getCameraPosition()
                                .target, markers.get(currentPt).getPosition());

                        LatLng targetLatLng = markers.get(currentPt).getPosition();
                        //float targetZoom = zoomBar.getProgress();

                        System.out.println("currentPt  = " + currentPt);
                        System.out.println("size  = " + markers.size());
                        //Create a new CameraPosition
                        CameraPosition cameraPosition =
                                new CameraPosition.Builder()
                                        .target(targetLatLng)
                                        .tilt(currentPt < markers.size() - 1 ? 90 : 0)
                                        .bearing(targetBearing)
                                        .zoom(googleMap.getCameraPosition().zoom)
                                        .build();

                        googleMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                3000,
                                MyCancelableCallback);
                        System.out.println("Animate to: " + markers.get(currentPt).getPosition() + "\n" +
                                "Bearing: " + targetBearing);

                        markers.get(currentPt).showInfoWindow();

                    } else {
                        //info.setText("onFinish()");
                    }

                }

            };


    public class Animator implements Runnable {

        private static final int ANIMATE_SPEEED = 1500;
        private static final int ANIMATE_SPEEED_TURN = 1000;
        private static final int BEARING_OFFSET = 20;

        private final Interpolator interpolator = new LinearInterpolator();

        int currentIndex = 0;

        float tilt = 90;
        float zoom = 15.5f;
        boolean upward = true;

        long start = SystemClock.uptimeMillis();

        LatLng endLatLng = null;
        LatLng beginLatLng = null;

        boolean showPolyline = false;

        private Marker trackingMarker;

        public void reset() {
            resetMarkers();
            start = SystemClock.uptimeMillis();
            currentIndex = 0;
            endLatLng = getEndLatLng();
            beginLatLng = getBeginLatLng();

        }

        public void stop() {
            trackingMarker.remove();
            mHandler.removeCallbacks(animator);
        }

        public void initialize(boolean showPolyLine) {
            reset();
            this.showPolyline = showPolyLine;

            highLightMarker(0);

            if (showPolyLine) {
                polyLine = initializePolyLine();
            }

            // We first need to put the camera in the correct position for the first run (we need 2 markers for this).....
            LatLng markerPos = markers.get(0).getPosition();
            LatLng secondPos = markers.get(1).getPosition();

            setupCameraPositionForMovement(markerPos, secondPos);

        }

        private void setupCameraPositionForMovement(LatLng markerPos,
                                                    LatLng secondPos) {

            float bearing = bearingBetweenLatLngs(markerPos, secondPos);

            trackingMarker = googleMap.addMarker(new MarkerOptions().position(markerPos)
                    .title("title")
                    .snippet("snippet"));

            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(markerPos)
                            .bearing(bearing + BEARING_OFFSET)
                            .tilt(90)
                            .zoom(googleMap.getCameraPosition().zoom >= 9 ? googleMap
                                    .getCameraPosition().zoom : 9)
                            .build();

            googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATE_SPEEED_TURN,
                    new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            System.out.println("finished camera");
                            animator.reset();
                            Handler handler = new Handler();
                            handler.post(animator);
                        }

                        @Override
                        public void onCancel() {
                            System.out.println("cancelling camera");
                        }
                    }
            );
        }

        private Polyline polyLine;
        private PolylineOptions rectOptions = new PolylineOptions();


        private Polyline initializePolyLine() {
            //polyLinePoints = new ArrayList<LatLng>();
            rectOptions.add(markers.get(0).getPosition());
            return googleMap.addPolyline(rectOptions);
        }

        /**
         * Add the marker to the polyline.
         */
        private void updatePolyLine(LatLng latLng) {
            List<LatLng> points = polyLine.getPoints();
            points.add(latLng);
            polyLine.setPoints(points);
        }


        public void stopAnimation() {
            animator.stop();
        }

        public void startAnimation(boolean showPolyLine) {
            if (markers.size() > 2) {
                animator.initialize(showPolyLine);
            }
        }


        @Override
        public void run() {

            long elapsed = SystemClock.uptimeMillis() - start;
            double t = interpolator.getInterpolation((float) elapsed / ANIMATE_SPEEED);

//			LatLng endLatLng = getEndLatLng();
//			LatLng beginLatLng = getBeginLatLng();

            double lat = t * endLatLng.latitude + (1 - t) * beginLatLng.latitude;
            double lng = t * endLatLng.longitude + (1 - t) * beginLatLng.longitude;
            LatLng newPosition = new LatLng(lat, lng);

            trackingMarker.setPosition(newPosition);

            if (showPolyline) {
                updatePolyLine(newPosition);
            }

            // It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
            //navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
            //navigateToPoint(newPosition,false);

            if (t < 1) {
                mHandler.postDelayed(this, 16);
            } else {

                System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + markers.size());
                // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                if (currentIndex < markers.size() - 2) {

                    currentIndex++;

                    endLatLng = getEndLatLng();
                    beginLatLng = getBeginLatLng();


                    start = SystemClock.uptimeMillis();

                    LatLng begin = getBeginLatLng();
                    LatLng end = getEndLatLng();

                    float bearingL = bearingBetweenLatLngs(begin, end);

                    highLightMarker(currentIndex);

                    CameraPosition cameraPosition =
                            new CameraPosition.Builder()
                                    .target(end) // changed this...
                                    .bearing(bearingL + BEARING_OFFSET)
                                    .tilt(tilt)
                                    .zoom(googleMap.getCameraPosition().zoom)
                                    .build();


                    googleMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATE_SPEEED_TURN,
                            null
                    );

                    start = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);

                } else {
                    currentIndex++;
                    highLightMarker(currentIndex);
                    stopAnimation();
                }

            }
        }


        private LatLng getEndLatLng() {
            return markers.get(currentIndex + 1).getPosition();
        }

        private LatLng getBeginLatLng() {
            return markers.get(currentIndex).getPosition();
        }

        private void adjustCameraPosition() {
            //System.out.println("tilt = " + tilt);
            //System.out.println("upward = " + upward);
            //System.out.println("zoom = " + zoom);
            if (upward) {

                if (tilt < 90) {
                    tilt++;
                    zoom -= 0.01f;
                } else {
                    upward = false;
                }

            } else {
                if (tilt > 0) {
                    tilt--;
                    zoom += 0.01f;
                } else {
                    upward = true;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.animating_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_bar_remove_location) {
            removeSelectedMarker();
            return true;
        } else if (item.getItemId() == R.id.action_bar_add_default_locations) {
            addDefaultLocations();
            return true;
        } else if (item.getItemId() == R.id.action_bar_start_animation) {
            animator.startAnimation(true);
            return true;
        } else if (item.getItemId() == R.id.action_bar_stop_animation) {
            animator.stopAnimation();
            return true;
        } else if (item.getItemId() == R.id.action_bar_clear_locations) {
            clearMarkers();
            return true;
        } else if (item.getItemId() == R.id.action_bar_toggle_style) {
            toggleStyle();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ;

    /**
     * Allows us to navigate to a certain point.
     */
    public void navigateToPoint(LatLng latLng, float tilt, float bearing, float zoom, boolean animate) {
        CameraPosition position =
                new CameraPosition.Builder().target(latLng)
                        .zoom(zoom)
                        .bearing(bearing)
                        .tilt(tilt)
                        .build();

        changeCameraPosition(position, animate);

    }

    public void navigateToPoint(LatLng latLng, boolean animate) {
        CameraPosition position = new CameraPosition.Builder().target(latLng).build();
        changeCameraPosition(position, animate);
    }

    private void changeCameraPosition(CameraPosition cameraPosition, boolean animate) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        if (animate) {
            googleMap.animateCamera(cameraUpdate);
        } else {
            googleMap.moveCamera(cameraUpdate);
        }
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

    public void toggleStyle() {
        if (GoogleMap.MAP_TYPE_NORMAL == googleMap.getMapType()) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }


    /**
     * Adds a marker to the map.
     */
    public void addMarkerToMap(LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                .title("title")
                .snippet("snippet"));
        markers.add(marker);

    }

    /**
     * Clears all markers from the map.
     */
    public void clearMarkers() {
        googleMap.clear();
        markers.clear();
    }

    /**
     * Remove the currently selected marker.
     */
    public void removeSelectedMarker() {
        this.markers.remove(this.selectedMarker);
        this.selectedMarker.remove();
    }

    /**
     * Highlight the marker by index.
     */
    private void highLightMarker(int index) {
        highLightMarker(markers.get(index));
    }

    /**
     * Highlight the marker by marker.
     */
    private void highLightMarker(Marker marker) {

		/*
        for (Marker foundMarker : this.markers) {
			if (!foundMarker.equals(marker)) {
				foundMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			} else {
				foundMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				foundMarker.showInfoWindow();
			}
		}
		*/
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.showInfoWindow();

        //Utils.bounceMarker(googleMap, marker);

        this.selectedMarker = marker;
    }

    private void resetMarkers() {
        for (Marker marker : this.markers) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }

}