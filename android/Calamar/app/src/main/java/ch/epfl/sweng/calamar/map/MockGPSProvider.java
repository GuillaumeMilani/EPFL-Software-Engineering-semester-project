package ch.epfl.sweng.calamar.map;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ch.epfl.sweng.calamar.CalamarApplication;


/**
 * Created by LPI on 13.11.2015.
 */
@SuppressWarnings("FinalStaticMethod")
public final class MockGPSProvider implements LocationListener
{
    private static volatile MockGPSProvider instance = null;

    // location
    private Location lastLocation;
    private Date lastUpdateTime;
    private LocationRequest locationRequest;

    private final GoogleApiClient googleApiClient;

    private final Set<GPSProvider.Observer> observers = new HashSet<>();

    /**
     * @return the GPSProvider singleton's instance
     */
    public final static MockGPSProvider getInstance() {
        // avoid call to synchronized if already instantiated
        if (MockGPSProvider.instance == null) {
            // avoid multiple instantiations by different threads
            synchronized(GPSProvider.class) {
                if (MockGPSProvider.instance == null) {
                    MockGPSProvider.instance = new MockGPSProvider();
                }
            }
        }
        return MockGPSProvider.instance;
    }

    public void startLocationUpdates(Activity parentActivity) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    /**
     * @return the last received location <br>(<b>WARNING</b>, can be null if {@link #startLocationUpdates(Activity)}
     * hasn't been called, or can be stale if {@link #stopLocationUpdates()} has been called and ...)
     */
    public Location getLastLocation() {
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
        // location must have certain fields sets ...
        checkLocation(location);
        // note : client must be connected.........
        LocationServices.FusedLocationApi.setMockMode(googleApiClient, true);
        LocationServices.FusedLocationApi.setMockLocation(googleApiClient, location);
    }

    private void checkLocation(Location location) {
        //throw new IllegalArgumentException("");
    }

    private MockGPSProvider() {
        // get the GoogleApi client,
        // creation plus play services availability checks are done in MainActivity at app startup
        googleApiClient = CalamarApplication.getInstance().getGoogleApiClient();
        // creates the location request
        createLocationRequest();
    }



    /**
     * Creates location request
     * */
    private void createLocationRequest() {
        // TODO tweak constants
        locationRequest = new LocationRequest();

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
}

