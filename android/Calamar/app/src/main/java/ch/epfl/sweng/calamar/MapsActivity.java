package ch.epfl.sweng.calamar;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //TODO hence ideally check for play services must be done at app startup !
    //but maps fragment will do all the necessary if gplay services apk not present
    //see comment on setupMapIfNeeded
    //....maybe delegate all the work to the map fragment, I think google has correctly done the job...

    //now maybe one could instantiate GPSProvider at app startup and let it do all the verifications
    //but don't know if very clean....nor if I've done all that can be done

    private GPSProvider gpsProvider;
    private GPSProvider.Observer gpsObserver = new GPSProvider.Observer() {
        @Override
        public void update(Location newLocation) {
            assert mMap != null :
                    "map should be initialized and ready before accessed by location updater";

            double latitude = newLocation.getLatitude();
            double longitude = newLocation.getLongitude();
            LatLng myLoc = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(myLoc));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
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
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        gpsProvider = GPSProvider.getInstance(this);
        gpsProvider.addObserver(gpsObserver);
        gpsProvider.startLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
    }
}
