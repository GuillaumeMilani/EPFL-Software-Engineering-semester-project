package ch.epfl.sweng.calamar.map;


import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ch.epfl.sweng.calamar.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    public static final String TAG = MapFragment.class.getSimpleName();

    //TODO : add two buttons begin checks stop checks
    // that will : checklocation settings + startlocation updates
    //TODO : manage activity lifecycle : start stop location updates when not needed, plus many potential problems

    private GoogleMap map; // Might be null if Google Play services APK is not available.
    // however google play services are checked at app startup...and
    // maps fragment will do all the necessary if gplay services apk not present
    // see comment on setupMapIfNeeded
    // ....maybe delegate all the work to the map fragment, I think google has correctly done the job...

    private GPSProvider gpsProvider;
    private final GPSProvider.Observer gpsObserver = new GPSProvider.Observer() {
        @Override
        public void update(Location newLocation) {
            assert map != null :
                    "map should be initialized and ready before accessed by location updater";

            // TODO here place useful stuff, display items and position
            // ex:
            double latitude = newLocation.getLatitude();
            double longitude = newLocation.getLongitude();
            LatLng myLoc = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(myLoc));
            map.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
        }
    };

    public MapFragment() {
        // Required empty public constructor
    }

    // *********************************************************************************************
    // map fragment lifecycle callbacks
    // https://developer.android.com/guide/components/fragments.html
    /**
     * Replaces the "onCreate" method from an Activity
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded(); // if needed, onMapReady is called with the map
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        setUpGPS(); // register to the GPSProvider location updates
    }

    @Override
    public void onStop() {
        super.onStop();
        //TODO
        // think about when should we start stop locationUpdates
        // on user demand ? via buttons, adds interaction and "user control"
        // or on create / stop ??
        // gpsProvider.stopLocationUpdates();
    }
    // *********************************************************************************************

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpGPS()} once when {@link #map} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpGPS() {
        gpsProvider = GPSProvider.getInstance();
        gpsProvider.addObserver(gpsObserver);
        gpsProvider.startLocationUpdates(getActivity());
    }
}
