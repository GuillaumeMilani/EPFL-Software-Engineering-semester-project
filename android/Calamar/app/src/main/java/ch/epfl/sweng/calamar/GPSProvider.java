package ch.epfl.sweng.calamar;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by LPI on 06.11.2015.
 */
public final class GPSProvider implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private static volatile GPSProvider instance = null;

    // LogCat tag
    private static final String TAG = GPSProvider.class.getSimpleName();

    // request codes
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int ERROR_RESOLUTION_REQUEST = 1001;
    private static final int CHECK_SETTINGS_REQUEST = 1002;

    // location
    private Location lastLocation;
    private Date lastUpdateTime;
    private LocationRequest locationRequest;


    // Google client to interact with Google API
    private GoogleApiClient googleApiClient;
    private boolean resolvingError = false;
    private GoogleApiAvailability apiAvailabilitySingleton = GoogleApiAvailability.getInstance();



    // caller activity
    private static Activity parentActivity;
    //TODO to remove, ideally current on-screen activity ref. should be accessible

    private Set<Observer> observers = new HashSet<>();

    //TODO see remark below in checkplay services, I think that googleApiclient and play services
    //errors should be handled in main activity at app startup
    private GPSProvider() {
        // check availability of play services
        if (checkPlayServices()) {
            // Builds the GoogleApi client
            buildGoogleApiClient();
            //creates the location request
            createLocationRequest();
        }
    }

    /**
     * Verifies google play services availability on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = apiAvailabilitySingleton.isGooglePlayServicesAvailable(parentActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailabilitySingleton.isUserResolvableError(resultCode)) {
                this.apiAvailabilitySingleton.getErrorDialog(parentActivity, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                //TODO need to establish communication protocol with activities
                //or move this check at app startup by mainactivity
            } else {
                Log.e(TAG, "This device is not supported. play services unavailable " +
                        "and automatic error resolution failed. error code : " + resultCode);
                //parentActivity.finish();
                //TODO what should we do when play services not available ?
            }
            return false;
        }
        return true;
    }

    /**
     * Verifies location services availability on the device, and initiate location updates if OK
     * if KO, attempt to resolve error by showing user a dialog. <br>
     * is called by startLocationUpdates()
     * */
    private void checkLocationSettings() {

        //build location settings status requests
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        //checks the location settings using the client and request above
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request);

        //sets the callback for result
        result.setResultCallback(new ResultCallback<LocationSettingsResult>()
        {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests.
                        Log.i(GPSProvider.TAG, "Location settings OK");
                        //start location requests
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                googleApiClient, locationRequest, GPSProvider.instance);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog and check the result in onActivityResult().
                            Log.e(GPSProvider.TAG, "Location settings not satisfied");
                            status.startResolutionForResult(parentActivity,
                                    CHECK_SETTINGS_REQUEST);
                            //TODO establish communication protocol with the activities !
                            //onActivityResult() callback in activity will be called with result
                            //of user action
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Log.e(GPSProvider.TAG, "LOCATION SETTINGS CHANGE UNAVAILABLE.. but mandatory..");
                        //parentActivity.finish();
                        //TODO decide what to do ? toast plus finish ?
                        break;
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

        //preferred rate in milliseconds. location updates may be faster than this rate if another
        // app is receiving updates at a faster rate, or slower than this rate,
        // or there may be no updates at all (if the device has no connectivity)
        locationRequest.setInterval(5000);

        //fastest rate in milliseconds at which app can handle location updates.
        // needed because Google Play services location APIs send out updates at the fastest rate
        // that any app has requested with setInterval(). If this rate is faster than what can be
        // handled, problems with UI flicker or data overflow.
        // hence set an upper limit to the update rate
        locationRequest.setFastestInterval(5000);

        //request the most precise location possible. The location services
        // are more likely to use GPS to determine the location.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(3); // 3 meters

        //PRIORITY_HIGH_ACCURACY, combined with the ACCESS_FINE_LOCATION permission setting,
        // and a fast update interval of 5 seconds, causes the fused location provider to return
        // location updates that are accurate to within a few feet.
        // This approach is appropriate for mapping apps that display the location in real time.
    }

    /**
     * Notify (call the {@link ch.epfl.sweng.calamar.GPSProvider.Observer#update(Location) update}
     * method of) the registered observers
     * @param location the new location
     */
    private void notifyObservers(Location location) {
        for(GPSProvider.Observer observer : observers) {
            observer.update(location);
        }
    }

    /**
     * Checks location settings and starts the location updates if no issue found. <br>
     * Registered observers will get new location periodically through their
     * {@link ch.epfl.sweng.calamar.GPSProvider.Observer#update(Location) update} method
     * */
    public void startLocationUpdates() {
        checkLocationSettings();
    }

    /**
     * Stops the location updates. <br>
     *     If you only want to unsubscribe, please call {@link #removeObserver(Observer)}
     */
    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    /**
     * @return the last received location <br>(<b>WARNING</b>, can be null if {@link #startLocationUpdates()}
     * hasn't been called, or can be stale if {@link #stopLocationUpdates()} has been called and ...)
     */
    public Location getLastLocation() {
        //TODO mybe check lastupdatetime and return null if too old
        return lastLocation;
    }

    /**
     * FusedLocationApi callback method
     * @param location the new location
     */
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        lastLocation = location;
        lastUpdateTime = new Date();
        notifyObservers(location);
    }

    /**
     * Google api callback methods TODO see remark above, I think should be handled by mainActivity at app startup
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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
            Log.e(TAG, "google API client failed to connect: no automatic resolution, error = "
                    + connectionResult.getErrorCode());
            //TODO finish ?
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "google API client connected");
        //TODO
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        googleApiClient.connect();
    }

    /**
     * Register a new observer to observable GPSProvider
     * @param observer the new observer
     */
    public void addObserver(GPSProvider.Observer observer) {
        this.observers.add(observer);
    }

    /**
     * Unsubscribe an observer from GPSProvider observable
     * @param observer the observer to remove
     * @return true if observer was present and correctly removed
     */
    public boolean removeObserver(GPSProvider.Observer observer) {
        return this.observers.remove(observer);
    }

    /**
     * @param parentActivity
     * @return the GPSProvider singleton's instance
     */
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

    /**
     * Defines observer to GPSProvider ...
     */
    abstract static class Observer {
        abstract public void update(Location  newLocation);
    }
}

