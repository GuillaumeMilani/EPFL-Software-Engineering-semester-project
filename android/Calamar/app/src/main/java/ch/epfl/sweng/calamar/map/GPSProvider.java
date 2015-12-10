package ch.epfl.sweng.calamar.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import ch.epfl.sweng.calamar.BaseActivity;
import ch.epfl.sweng.calamar.BuildConfig;
import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;


/**
 * Created by LPI on 06.11.2015.
 */
public final class GPSProvider implements LocationListener {
    private static volatile GPSProvider instance = null;
    private static final String TAG = GPSProvider.class.getSimpleName();
    public static final int CHECK_SETTINGS_REQUEST = 1002;


    // location
    private Location lastLocation;
    private Date lastUpdateTime;
    private final LocationRequest locationRequest = createLocationRequest();

    private final GoogleApiClient googleApiClient;
    private boolean isStarted = false;
    private boolean isResolvingSettings = false;


    // TODO, seems to be one possible soluce to avoid (or maybe luck....)
    // https://stackoverflow.com/questions/24201356/avoiding-concurrent-modification-in-observer-pattern
    // but ?? maybe "just" Collections.synchronizedSet can do the job
    private final Set<Observer> observers = new CopyOnWriteArraySet<>();

    /**
     * @return the GPSProvider singleton's instance
     */
    public static GPSProvider getInstance() {
        // avoid call to synchronized if already instantiated
        if (GPSProvider.instance == null) {
            // avoid multiple instantiations by different threads
            synchronized (GPSProvider.class) {
                if (GPSProvider.instance == null) {
                    GPSProvider.instance = new GPSProvider();
                }
            }
        }
        return GPSProvider.instance;
    }

    /**
     * Starts the location updates. <br>
     * Registered observers will get new location periodically through their
     * {@link GPSProvider.Observer#update(Location) update} method. <br><br>
     * <p/>
     * WARNING: caller should call checkSettingsAndLaunchIfOK instead
     *
     * @throws IllegalStateException when google api client not connected
     */
    public void startLocationUpdates() {
        if (!isStarted()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, GPSProvider.instance);
            isStarted = true;
            isResolvingSettings = false;
            Log.i(TAG, CalamarApplication.getInstance().getString(R.string.gps_request_started));
        }
    }

    /**
     * Stops the location updates. <br>
     * If you only want to unsubscribe, please call {@link #removeObserver(Observer)}
     */
    public void stopLocationUpdates() {
        if (isStarted()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
            isStarted = false;
            Log.i(TAG, CalamarApplication.getInstance().getString(R.string.gps_request_stopped));
        }
    }

    /**
     * FusedLocationApi callback method
     *
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
     *
     * @param observer the new observer
     */
    public void addObserver(GPSProvider.Observer observer) {
        this.observers.add(observer);
    }

    /**
     * Unsubscribe an observer from GPSProvider observable
     *
     * @param observer the observer to remove
     * @return true if observer was present and correctly removed
     */
    public boolean removeObserver(GPSProvider.Observer observer) {
        return this.observers.remove(observer);
    }

    /**
     * Sets a mock location for debugging and testing
     *
     * @param location The location to be set
     * @throws RuntimeException If the app is not in debug mode
     */
    public void setMockLocation(Location location) {
        if (BuildConfig.DEBUG) {
            notifyObservers(location);
        } else {
            throw new RuntimeException(CalamarApplication.getInstance().getString(R.string.set_mock_release_exception));
        }
    }

    /**
     * @return True if the GPSProvider has been started
     */
    public boolean isStarted() {
        return isStarted;
    }

    public boolean isResolvingSettings() {
        return isResolvingSettings;
    }

    private GPSProvider() {
        // get the GoogleApi client,
        // creation plus play services availability checks are done in MainActivity at app startup
        googleApiClient = CalamarApplication.getInstance().getGoogleApiClient();
    }

    /**
     * Verifies location services availability on the device
     * if OK, location updates are requested, <br>
     * is NOT called by startLocationUpdates(). <br>
     * does nothing if already started
     */
    public void checkSettingsAndLaunchIfOK(final BaseActivity parentActivity) {
        if (!isStarted() && !isResolvingSettings()) {
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
                            Log.i(GPSProvider.TAG, CalamarApplication.getInstance().getString(R.string.location_settings_ok));
                            //  start location requests
                            startLocationUpdates();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog, onActivityResult() callback in activity will be called
                                // with result of user action
                                isResolvingSettings = true;
                                Log.e(GPSProvider.TAG, CalamarApplication.getInstance().getString(R.string.unsatisfied_location_settings));
                                status.startResolutionForResult(parentActivity, CHECK_SETTINGS_REQUEST);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            if (!parentActivity.isFinishing()) {
                                parentActivity.displayErrorMessage(
                                        parentActivity.getString(R.string.location_settings_change_unavailable_mandatory), true);
                            }
                            break;
                    }
                }
            });
        }
    }

    /**
     * Creates location request
     */
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

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(3); // 3 meters


        // PRIORITY_HIGH_ACCURACY, combined with the ACCESS_FINE_LOCATION permission setting,
        // and a fast update interval of 5 seconds, causes the fused location provider to return
        // location updates that are accurate to within a few feet.
        // This approach is appropriate for mapping apps that display the location in real time.

        return locationRequest;
    }

    /**
     * Notify (call the {@link GPSProvider.Observer#update(Location) update}
     * method of) the registered observers
     *
     * @param location the new location
     */
    private void notifyObservers(Location location) {
        for (GPSProvider.Observer observer : observers) {
            observer.update(location);
        }
    }

    public void displayErrorMessage(final BaseActivity context) {
        // TODO refactor to remove code duplication with baseact.displaydialog
        Log.e(GPSProvider.TAG, "we need gps !!");
        if (!context.isFinishing()) {//&& !isPaused()) {
            final AlertDialog.Builder errorDialog = new AlertDialog.Builder(context);
            errorDialog.setTitle(context.getString(R.string.unsatisfied_location_settings));
            errorDialog.setPositiveButton(R.string.alert_dialog_default_positive_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //OK
                    isResolvingSettings = false;
                    context.finish();
                }
            });
            errorDialog.show();
        }
    }

    /**
     * Defines observer to GPSProvider ...
     */
    public abstract static class Observer {
        abstract public void update(Location newLocation);
    }
}

