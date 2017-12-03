package com.durbinlabs.googlemapmarkeranimation.interfaces;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hp on 12/3/2017.
 */

public interface LatLngInterpolator {
    public LatLng interpolate(float fraction, LatLng a, LatLng b);
}
