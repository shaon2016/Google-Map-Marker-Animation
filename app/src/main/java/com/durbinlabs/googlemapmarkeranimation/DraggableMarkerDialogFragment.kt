package com.durbinlabs.googlemapmarkeranimation


import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
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
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_draggable_marker_dialog.*
import java.io.IOException
import java.util.*

class DraggableMarkerDialogFragment : DialogFragment(), OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private var marker: Marker? = null
    private var geocoder: Geocoder? = null
    private var addresses: List<Address>? = null
    private val TAG = DraggableMarkerActivity::class.java.simpleName
    private var mMap: GoogleMap? = null
    private val GOOGLE_API_CLIENT_ID = 0
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mPlaceArrayAdapter: PlaceArrayAdapter? = null
    private val BOUNDS_MOUNTAIN_VIEW = LatLngBounds(
            LatLng(37.398160, -122.180831), LatLng(37.430610, -121.972090))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Style for the full screen
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_draggable_marker_dialog, container, false)

        setToolbar(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val mapFragment = activity?.supportFragmentManager
                ?.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geocoder = Geocoder(context, Locale.getDefault())


        //auto complete api
        mGoogleApiClient = GoogleApiClient.Builder(context!!)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(activity!!, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build()

        autoCompleteTextView.threshold = 1

        autoCompleteTextView.onItemClickListener = mAutocompleteClickListener
        mPlaceArrayAdapter = PlaceArrayAdapter(context, android.R.layout.simple_list_item_1,
                BOUNDS_MOUNTAIN_VIEW, null)
        autoCompleteTextView.setAdapter(mPlaceArrayAdapter)

    }

    private val mAutocompleteClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        val item = mPlaceArrayAdapter?.getItem(position)
        val placeId = item!!.placeId.toString()
        Log.i(TAG, "Selected: " + item.description)
        val placeResult = Places.GeoDataApi
                .getPlaceById(mGoogleApiClient!!, placeId)
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback)
        Log.i(TAG, "Fetching details for ID: " + item!!.placeId)
    }

    private val mUpdatePlaceDetailsCallback = ResultCallback<PlaceBuffer> { places ->
        if (!places.status.isSuccess) {
            Log.e(TAG, "Place query did not complete. Error: " + places.status.toString())
            return@ResultCallback
        }
        // Selecting the first object buffer.
        val place = places.get(0)

        Log.d("dataTag", place.address!!.toString() + "")

        marker?.setPosition(place.latLng)
        marker?.setTitle(place.address!!.toString() + "")
        marker?.showInfoWindow()
        val cameraPosition = CameraPosition.Builder()
                .target(place.latLng).zoom(10f)
                .build()
        mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        marker = mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in " + "Sydney")
                .draggable(true))
        marker?.showInfoWindow()
        val cameraPosition = CameraPosition.Builder().target(sydney).zoom(10f)
                .build()
        mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        mMap?.setOnMarkerClickListener(this)
        mMap?.setOnMarkerDragListener(this)
        mMap?.setOnCameraIdleListener(this)
        mMap?.setOnCameraMoveStartedListener(this)
        mMap?.setOnCameraMoveListener(this)
        mMap?.setOnCameraMoveCanceledListener(this)

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
            addresses = geocoder?.getFromLocation(newLatLng.latitude,
                    newLatLng.longitude, 1) // Here 1 represent max location
            // result to returned, by documents it recommended 1 to 5
            val address = addresses?.get(0)?.getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            val city = addresses?.get(0)?.locality
            val state = addresses?.get(0)?.adminArea
            val country = addresses?.get(0)?.countryName
            val postalCode = addresses?.get(0)?.postalCode
            val knownName = addresses?.get(0)?.featureName
            marker.setTitle(address)
            marker.showInfoWindow()

            val cameraPosition = CameraPosition.Builder().target(newLatLng).zoom(10f)
                    .build()
            mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    override fun onConnected(bundle: Bundle?) {
        mPlaceArrayAdapter?.setGoogleApiClient(mGoogleApiClient)
        Log.i(TAG, "Google Places API connected.")

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: " + connectionResult.errorCode)

        Toast.makeText(context,
                "Google Places API connection failed with error code:" + connectionResult.errorCode,
                Toast.LENGTH_LONG).show()
    }

    override fun onConnectionSuspended(i: Int) {
        mPlaceArrayAdapter?.setGoogleApiClient(null)
        Log.e(TAG, "Google Places API connection suspended.")
    }


    // move the map and update the address
    override fun onCameraIdle() {
        try {
            centerMarkerByImageView.visibility = View.GONE
            val midLatLng = mMap?.cameraPosition?.target
            if (midLatLng != null) {
                marker?.remove()
                addresses = geocoder?.getFromLocation(midLatLng!!.latitude,
                        midLatLng!!.longitude, 1) // Here 1 represent max location
                val address = addresses?.get(0)?.getAddressLine(0)
                marker = mMap?.addMarker(MarkerOptions().position(midLatLng!!).title(address)
                        .draggable(true))
                marker?.showInfoWindow()
            } else
                Toast.makeText(context, "No Location found", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    override fun onCameraMoveCanceled() {

    }

    override fun onCameraMove() {
        centerMarkerByImageView.visibility = View.VISIBLE
    }

    override fun onCameraMoveStarted(i: Int) {

    }

    private fun setToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.draggableMarkerToolbar)
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
        toolbar.title = "My Dialog"
    }

    companion object {

        @JvmStatic
        fun newInstance() = DraggableMarkerDialogFragment()
    }


}
