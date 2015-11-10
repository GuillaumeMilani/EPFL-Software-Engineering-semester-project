package ch.epfl.sweng.calamar;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();

    //TODO : add two buttons begin checks stop checks
    // that will : checklocation settings + startlocation updates

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //however google play services are checked at app startup...and
    // maps fragment will do all the necessary if gplay services apk not present
    //see comment on setupMapIfNeeded
    //....maybe delegate all the work to the map fragment, I think google has correctly done the job...

    private GPSProvider gpsProvider;
    private final GPSProvider.Observer gpsObserver = new GPSProvider.Observer() {
        @Override
        public void update(Location newLocation) {
            assert mMap != null :
                    "map should be initialized and ready before accessed by location updater";

            //TODO here place useful stuff, display items and position
            //ex:
            double latitude = newLocation.getLatitude();
            double longitude = newLocation.getLongitude();
            LatLng myLoc = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(myLoc));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
        }
    };

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GPSProvider.CHECK_SETTINGS_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //reiterate the process
                        gpsProvider.startLocationUpdates(this);
                        //TODO activate/deactivate UI
                        break;
                    default:
                        Log.e(MapsActivity.TAG, "user declined offer to set location settings");
                        //finish();//TODO activate/deactivate UI...
                        //what to do ?
                }
                break;
            default: throw new IllegalStateException("onActivityResult : unknown request ! ");
        }
    }

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
        gpsProvider = GPSProvider.getInstance();
        gpsProvider.addObserver(gpsObserver);
        gpsProvider.startLocationUpdates(this);
    }
}
