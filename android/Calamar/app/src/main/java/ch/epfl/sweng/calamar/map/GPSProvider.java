package ch.epfl.sweng.calamar.map;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.util.Log;

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
import java.util.concurrent.CopyOnWriteArraySet;

import ch.epfl.sweng.calamar.BuildConfig;
import ch.epfl.sweng.calamar.CalamarApplication;


/**
 * Created by LPI on 06.11.2015.
 */
@SuppressWarnings("FinalStaticMethod")
public final class GPSProvider implements LocationListener {
    private static volatile GPSProvider instance = null;
    private static final String TAG = GPSProvider.class.getSimpleName();
    public static final int CHECK_SETTINGS_REQUEST = 1002;


    // location
    private Location lastLocation;
    private Date lastUpdateTime;
    private final LocationRequest locationRequest = createLocationRequest();

    private final GoogleApiClient googleApiClient;

    // TODO, seems to be one possible soluce to avoid (or maybe luck....)
    // https://stackoverflow.com/questions/24201356/avoiding-concurrent-modification-in-observer-pattern
    // but ?? maybe "just" Collections.synchronizedSet can do the job
    private final Set<Observer> observers = new CopyOnWriteArraySet<>();

    /**
     * @return the GPSProvider singleton's instance
     */
    public final static GPSProvider getInstance() {
        // avoid call to synchronized if already instantiated
        if (GPSProvider.instance == null) {
            // avoid multiple instantiations by different threads
            synchronized(GPSProvider.class) {
                if (GPSProvider.instance == null) {
                    GPSProvider.instance = new GPSProvider();
                }
            }
        }
        return GPSProvider.instance;
    }

    /**
     * Checks location settings and starts the location updates if no issue found. <br>
     * Registered observers will get new location periodically through their
     * {@link GPSProvider.Observer#update(Location) update} method. <br><br>
     *
     * <b>Warning :</b> if settings are not OK, nothing done, caller <i>parentActivity</i>
     * should implement {@link Activity#onActivityResult(int, int, Intent)} to react to the user actions.
     * <br>
     *     request code : {@link #CHECK_SETTINGS_REQUEST}
     *     TODO describe result code and actions
     */
    public void startLocationUpdates(Activity parentActivity) {
        checkLocationSettings(parentActivity);
        // TODO...I don't manage to find a good solution....rethink
        // here if settings KO and user sets them OK, caller will recall this, and settings will be checked
        // again .........if I split them apart, in case of settings OK,
        // need to decide on a way to inform caller that settings ok (callback..)
    }

    /**
     * Stops the location updates. <br>
     *     If you only want to unsubscribe, please call {@link #removeObserver(Observer)}
     */
    public void stopLocationUpdates() {
        if(googleApiClient.isConnected()) { // else call cause illegalstateexception
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        } // TODO else ??? we need to think when we start / stop / connect / disconnect
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

    public void setMockLocation(Location location) {
        if(BuildConfig.DEBUG) {
            notifyObservers(location);
        } else {
            throw new RuntimeException("can't set mock Location in release version");
        }
    }

    private GPSProvider() {
        // get the GoogleApi client,
        // creation plus play services availability checks are done in MainActivity at app startup
        googleApiClient = CalamarApplication.getInstance().getGoogleApiClient();
    }

    /**
     * Verifies location services availability on the device, and initiate location updates if OK
     * if KO, attempt to resolve error by showing user a dialog. <br>
     * is called by startLocationUpdates()
     * */
    private void checkLocationSettings(final Activity parentActivity) {
        googleApiClient.connect();//if already connected does nothing

        // build location settings status requests
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        // checks the location settings using the client and request above
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request);

        // sets the callback for result
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //  final LocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests.
                        Log.i(GPSProvider.TAG, "Location settings OK");

                        //  start location requests
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                googleApiClient, locationRequest, GPSProvider.instance);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog and check the result in onActivityResult().
                            Log.e(GPSProvider.TAG, "Location settings not satisfied");
                            status.startResolutionForResult(parentActivity, CHECK_SETTINGS_REQUEST);

                            // onActivityResult() callback in activity will be called with result
                            // of user action

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
     * Creates location request
     * */
    private LocationRequest createLocationRequest() {
        // TODO tweak constants
        LocationRequest locationRequest = new LocationRequest();

        // preferred rate in milliseconds. location updates may be faster than this rate if another
        // app is receiving updates at a faster rate, or slower than this rate,
        // or there may be no updates at all (if the device has no connectivity)
        locationRequest.setInterval(5000);

        // fastest rate in milliseconds at which app can handle location updates.
        // needed because Google Play services location APIs send out updates at the fastest rate
        // that any app has requested with setInterval(). If this rate is faster than what can be
        // handled, problems with UI flicker or data overflow.
        // hence set an upper limit to the update rate
        locationRequest.setFastestInterval(5000);

        // request the most precise location possible. The location services
        // are more likely to use GPS to determine the location.
        if(!BuildConfig.DEBUG) {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setSmallestDisplacement(3); // 3 meters
        }


        // PRIORITY_HIGH_ACCURACY, combined with the ACCESS_FINE_LOCATION permission setting,
        // and a fast update interval of 5 seconds, causes the fused location provider to return
        // location updates that are accurate to within a few feet.
        // This approach is appropriate for mapping apps that display the location in real time.

        return locationRequest;
    }

    /**
     * Notify (call the {@link GPSProvider.Observer#update(Location) update}
     * method of) the registered observers
     * @param location the new location
     */
    private void notifyObservers(Location location) {
        for(GPSProvider.Observer observer : observers) {
            observer.update(location);
        }
    }

    /**
     * Defines observer to GPSProvider ...
     */
    public abstract static class Observer {
        abstract public void update(Location  newLocation);
    }
}

