package com.durbinlabs.googlemapmarkeranimation

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

import androidx.fragment.app.FragmentActivity

import com.durbinlabs.googlemapmarkeranimation.adapter.PlaceArrayAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_dagger_mareker_dialog.*

import java.io.IOException
import java.util.Locale

class DaggerMarekerActivityDialog : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveCanceledListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private var marker: Marker? = null
    private var geocoder: Geocoder? = null
    private var addresses: List<Address>? = null
    private var mMap: GoogleMap? = null
    private var centerMarkerByImageView: ImageView? = null
    private var mAutocompleteTextView: AutoCompleteTextView? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mPlaceArrayAdapter: PlaceArrayAdapter? = null

    private val mAutocompleteClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        val item = mPlaceArrayAdapter!!.getItem(position)
        val placeId = item!!.placeId.toString()
        Log.i(TAG, "Selected: " + item.description)
        val placeResult = Places.GeoDataApi
                .getPlaceById(mGoogleApiClient!!, placeId)
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback)
        Log.i(TAG, "Fetching details for ID: " + item.placeId)
    }

    private val mUpdatePlaceDetailsCallback = ResultCallback<PlaceBuffer> { places ->
        if (!places.status.isSuccess) {
            Log.e(TAG, "Place query did not complete. Error: " + places.status.toString())
            return@ResultCallback
        }
        // Selecting the first object buffer.
        val place = places.get(0)

        Log.d("dataTag", place.address!!.toString() + "")

        marker!!.position = place.latLng
        marker!!.title = place.address!!.toString() + ""
        marker!!.showInfoWindow()
        val cameraPosition = CameraPosition.Builder()
                .target(place.latLng).zoom(10f)
                .build()
        mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dagger_mareker_dialog)
        centerMarkerByImageView = findViewById(R.id.centerMarkerByImageView)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        geocoder = Geocoder(this, Locale.getDefault())


        //auto complete api
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build()
        mAutocompleteTextView = findViewById<View>(R.id
                .autoCompleteTextView) as AutoCompleteTextView
        mAutocompleteTextView!!.threshold = 1

        mAutocompleteTextView!!.onItemClickListener = mAutocompleteClickListener
        mPlaceArrayAdapter = PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null)
        mAutocompleteTextView!!.setAdapter(mPlaceArrayAdapter)

        setToolbar()
    }

    private fun setToolbar() {
        draggableMarkerToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        draggableMarkerToolbar.setNavigationOnClickListener {
            finish()
        }
        draggableMarkerToolbar.title = "My Dialog"
        draggableMarkerToolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        marker = mMap!!.addMarker(MarkerOptions().position(sydney).title("Marker in " + "Sydney")
                .draggable(true))
        marker!!.showInfoWindow()
        val cameraPosition = CameraPosition.Builder().target(sydney).zoom(10f)
                .build()
        mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        mMap!!.setOnMarkerClickListener(this)
        mMap!!.setOnMarkerDragListener(this)
        mMap!!.setOnCameraIdleListener(this)
        mMap!!.setOnCameraMoveStartedListener(this)
        mMap!!.setOnCameraMoveListener(this)
        mMap!!.setOnCameraMoveCanceledListener(this)

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return true
    }

    override fun onMarkerDragStart(marker: Marker) {
        Log.d(TAG, "Drag Started")
    }

    override fun onMarkerDrag(marker: Marker) {
        Log.d(TAG, "Dragging")
    }

    override fun onMarkerDragEnd(marker: Marker) {
        Log.d(TAG, "Drag End")

        try {
            val newLatLng = LatLng(marker.position.latitude, marker.position
                    .longitude)
            addresses = geocoder!!.getFromLocation(newLatLng.latitude,
                    newLatLng.longitude, 1) // Here 1 represent max location
            // result to returned, by documents it recommended 1 to 5
            val address = addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            val city = addresses!![0].locality
            val state = addresses!![0].adminArea
            val country = addresses!![0].countryName
            val postalCode = addresses!![0].postalCode
            val knownName = addresses!![0].featureName
            marker.title = address
            marker.showInfoWindow()

            val cameraPosition = CameraPosition.Builder().target(newLatLng).zoom(10f)
                    .build()
            mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    override fun onConnected(bundle: Bundle?) {
        mPlaceArrayAdapter!!.setGoogleApiClient(mGoogleApiClient)
        Log.i(TAG, "Google Places API connected.")

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: " + connectionResult.errorCode)

        Toast.makeText(this,
                "Google Places API connection failed with error code:" + connectionResult.errorCode,
                Toast.LENGTH_LONG).show()
    }

    override fun onConnectionSuspended(i: Int) {
        mPlaceArrayAdapter!!.setGoogleApiClient(null)
        Log.e(TAG, "Google Places API connection suspended.")
    }


    // move the map and update the address
    override fun onCameraIdle() {
        try {
            centerMarkerByImageView!!.visibility = View.GONE
            val midLatLng = mMap!!.cameraPosition.target
            if (midLatLng != null) {
                marker!!.remove()
                addresses = geocoder!!.getFromLocation(midLatLng.latitude,
                        midLatLng.longitude, 1) // Here 1 represent max location
                val address = addresses!![0].getAddressLine(0)
                marker = mMap!!.addMarker(MarkerOptions().position(midLatLng).title(address)
                        .draggable(true))
                marker!!.showInfoWindow()
            } else
                Toast.makeText(this, "No Location found", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    override fun onCameraMoveCanceled() {

    }

    override fun onCameraMove() {
        centerMarkerByImageView!!.visibility = View.VISIBLE
    }

    override fun onCameraMoveStarted(i: Int) {

    }

    companion object {
        private val TAG = DraggableMarkerActivity::class.java.simpleName
        private val GOOGLE_API_CLIENT_ID = 0
        private val BOUNDS_MOUNTAIN_VIEW = LatLngBounds(
                LatLng(37.398160, -122.180831), LatLng(37.430610, -121.972090))
    }
}
