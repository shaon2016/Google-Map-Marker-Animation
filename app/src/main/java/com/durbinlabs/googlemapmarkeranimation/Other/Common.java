package com.durbinlabs.googlemapmarkeranimation.Other;

import com.durbinlabs.googlemapmarkeranimation.Remote.IGoogleApi;
import com.durbinlabs.googlemapmarkeranimation.Remote.RetrofitClient;

/**
 * Created by hp on 12/3/2017.
 */

public class Common {
    public final static String BASEURL = "https://maps.googleapis.com/";

    public static IGoogleApi getGoogleApi() {
        return RetrofitClient.getRetrofit(BASEURL).create(IGoogleApi.class);
    }
}
