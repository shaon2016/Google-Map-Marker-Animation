package com.durbinlabs.googlemapmarkeranimation.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by hp on 12/3/2017.
 */

public interface IGoogleApi {
    @GET
    Call<String> getDataFromGoogle(@Url String url);
}
