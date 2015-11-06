package ch.epfl.sweng.calamar;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by LPI on 06.11.2015.
 */
public final class GPSProvider implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static volatile GPSProvider instance = null;

    // LogCat tag
    private static final String TAG = GPSProvider.class.getSimpleName();

    // request codes
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int ERROR_RESOLUTION_REQUEST = 1001;
    private static final int CHECK_SETTINGS_REQUEST = 1002;

    private Location lastLocation;
    private LocationRequest locationRequest;


    // Google client to interact with Google API
    private GoogleApiClient googleApiClient;
    private boolean resolvingError = false;


    // caller activity
    private static Activity parentActivity;
    //TODO check if sufficent...maybe set it to mainactivity at startup and basta

    private Set<Observer> observers = new HashSet<>();

    private GPSProvider() {
        //todo i suggest
        // check availability of play services
        if (checkPlayServices()) {
            // Builds the GoogleApi client
            buildGoogleApiClient();
            //creates the location request
            createLocationRequest();
            googleApiClient.connect();
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
                .addLocationRequest(locationRequest)
                .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request);

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
        googleApiClient = new GoogleApiClient.Builder(parentActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    /**
     * Creates location request
     * */
    private void createLocationRequest() {
        //TODO tweak constants
        locationRequest = new LocationRequest();
        locationRequest.setInterval(50);
        locationRequest.setFastestInterval(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(5); // 5 meters
    }

    /**
     * Starts the location updates
     * */
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    /**
     * Stops the location updates
     */
    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    private void notifyObservers(Location location) {
        for(GPSProvider.Observer observer : observers) {
            observer.update(location);
        }
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO think what to do if error..
        if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                resolvingError = true;
                connectionResult.startResolutionForResult(parentActivity, ERROR_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
            }
        } else {
            resolvingError = true;
            Toast.makeText(parentActivity,
                    "google API client failed to connect: no automatic resolution, error = "
                            + connectionResult.getErrorCode(), Toast.LENGTH_LONG)
                    .show();
            Log.i(TAG, "google API client failed to connect: no automatic resolution, error = "
                    + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        Toast.makeText(parentActivity, "google API client connected", Toast.LENGTH_SHORT).show();
        //TODO to remove
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        //stopLocationUpdates();
        //TODO when do we start/stop location requests
        lastLocation = location;
        notifyObservers(location);
    }

    public void addObserver(GPSProvider.Observer observer) {
        this.observers.add(observer);
    }

    public boolean removeObserver(GPSProvider.Observer observer) {
        return this.observers.remove(observer);
    }

    public final static GPSProvider getInstance(Activity parentActivity) {
        //avoid call to synchronized if already instantiated
        if (GPSProvider.instance == null) {
            //avoid multiple instantiations by different threads
            synchronized(GPSProvider.class) {
                if (GPSProvider.instance == null) {
                    GPSProvider.instance = new GPSProvider();
                }
            }
        }
        GPSProvider.setParentActivity(parentActivity);
        return GPSProvider.instance;
    }

    private static void setParentActivity(Activity parentActivity) {
        if(null == parentActivity) {
            throw new IllegalArgumentException("parentActivity cannot be null !");
        }
        GPSProvider.parentActivity = parentActivity;
    }

    abstract static class Observer {
        abstract public void update(Location  newLocation);
    }
}

