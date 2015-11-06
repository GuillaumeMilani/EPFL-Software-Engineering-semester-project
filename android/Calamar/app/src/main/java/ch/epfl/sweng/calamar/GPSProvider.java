package ch.epfl.sweng.calamar;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;


/**
 * Created by LPI on 06.11.2015.
 */
public enum GPSProvider implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

    INSTANCE();


    // LogCat tag
    private static final String TAG = GPSProvider.class.getSimpleName();

    // request codes
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int ERROR_RESOLUTION_REQUEST = 1001;
    private static final int CHECK_SETTINGS_REQUEST = 1002;

    private Location mLastLocation;
    private LocationRequest mLocationRequest;


    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // caller activity
    private Activity parentActivity;

    private Set<Observer> observers = new HashSet<>();

    GPSProvider() {
        // check availability of play services
        if (checkPlayServices()) {
            // Builds the GoogleApi client
            buildGoogleApiClient();
            //creates the location request
            createLocationRequest();
            mGoogleApiClient.connect();
            //todo where do we start stop location updates ?
            //checkLocationSettings()
        }
    }

    /**
     * Verifies google play services availability on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(parentActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, parentActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(parentActivity,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                parentActivity.finish();
                //TODO what should we do when play services not available ?
                //shouldn't we do these kind of check at app startup ?
            }
            return false;
        }
        return true;
    }

    /**
     * Verifies location services availability on the device
     * */
    private void checkLocationSettings() {

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, request);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests.
                        startLocationUpdates();
                        Toast.makeText(parentActivity,
                                "Location settings OK",
                                Toast.LENGTH_LONG).show();
                        //todo probably to remove
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog and check the result in onActivityResult().
                            Toast.makeText(parentActivity,
                                    "Location settings not satisfied",
                                    Toast.LENGTH_LONG).show();
                            status.startResolutionForResult(parentActivity,
                                    CHECK_SETTINGS_REQUEST);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Toast.makeText(parentActivity,
                                "SETTINGS CHANGE UNAVAILABLE.. but mandatory..",
                                Toast.LENGTH_LONG).show();
                        parentActivity.finish();
                        break;
                        //TODO same remark
                }
            }
        });
    }

    /**
     * Creates google api client object
     * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi.html">
     *      FusedLocationProviderAPI</a>
     * */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(parentActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    /**
     * Creates location request
     * */
    private void createLocationRequest() {
        //TODO tweak constants
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(50);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5); // 5 meters
    }

    /**
     * Starts the location updates
     * */
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Stops the location updates
     */
    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void notifyObservers(Location location) {
        for(GPSProvider.Observer observer : observers) {
            observer.update(location);
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        //stopLocationUpdates();
        //TODO when do we start/stop location requests
        mLastLocation = location;
        notifyObservers(location);
    }

    public void setParentActivity(Activity parentActivity) {
        if(null == parentActivity) {
            throw new IllegalArgumentException("parentActivity cannot be null !");
        }
        this.parentActivity = parentActivity;
    }

    public void addObserver(GPSProvider.Observer observer) {
        this.observers.add(observer);
    }

    public boolean removeObserver(GPSProvider.Observer observer) {
        return this.observers.remove(observer);
    }

    abstract class Observer extends Observable {
        abstract public void update(Location  newLocation);
    }
}

