package ch.epfl.sweng.calamar;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.calamar.chat.ChatFragment;
import ch.epfl.sweng.calamar.map.GPSProvider;
import ch.epfl.sweng.calamar.map.MapFragment;

import static android.app.PendingIntent.getActivity;

/**
 * Created by Guillaume on 12.11.2015.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // Tabs related stuff
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    //TODO Test if i can use the location architecture to check the google play services
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // activity request codes
    private static final int ERROR_RESOLUTION_REQUEST = 1001;

    // google api related stuff
    private boolean resolvingError;


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapFragment(), "Map");
        adapter.addFragment(new ChatFragment(), "Chat");
        viewPager.setAdapter(adapter);
    }

    // *********************************************************************************************
    // GOOGLE API CLIENT CALLBACKS METHODS
    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "google API client connected");
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        CalamarApplication.getInstance().getGoogleApiClient().connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            resolvingError = true;
            Log.e(TAG, "google API client failed to connect: automatic resolution started, error = "
                    + connectionResult.getErrorCode());

            try {
                connectionResult.startResolutionForResult(this, ERROR_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                CalamarApplication.getInstance().getGoogleApiClient().connect();
            }
        } else {
            resolvingError = true;
            Log.e(TAG, "google API client failed to connect: no automatic resolution, error = "
                    + connectionResult.getErrorCode());

            // show error dialog
            showGoogleApiErrorDialog(connectionResult.getErrorCode());
        }
    }
    // *********************************************************************************************

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ERROR_RESOLUTION_REQUEST:
                resolvingError = false;
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Make sure the app is not already connected or attempting to connect
                        GoogleApiClient googleApiClient =
                                CalamarApplication.getInstance().getGoogleApiClient();
                        if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                            googleApiClient.connect();
                        }
                        break;
                    default:
                        Log.e(MainActivity.TAG, "google API client definitely can't connect...");
                        finish();//TODO maybe refine ?
                }
                break;

            case GPSProvider.CHECK_SETTINGS_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(MapFragment.TAG, "user correctly set location settings");

                        // reiterate the process
                        GPSProvider gpsProvider = GPSProvider.getInstance();
                        gpsProvider.startLocationUpdates(this);
                        //TODO WTF why is the whole process executed twice ?????????????????????????
                        // (double check....)

                        //TODO activate/deactivate UI
                        break;
                    default:
                        Log.e(MapFragment.TAG, "user declined offer to set location settings");
                        // finish();//TODO activate/deactivate UI...
                        // what to do ?
                }
                break;

            default:
                throw new IllegalStateException("onActivityResult : unknown request ! ");
        }
    }


    // *********************************************************************************************
    // ACTIVITY LIFECYCLE CALLBACKS
    // https://developer.android.com/training/basics/activity-lifecycle/starting.html
    // even better :
    // https://stackoverflow.com/questions/12203651/why-is-onresume-called-when-an-activity-starts
    //
    // every time screen is rotated, activity is destroyed/recreated :
    // https://stackoverflow.com/questions/7618703/activity-lifecycle-oncreate-called-on-every-re-orientation
    // maybe prevent this ...
    //
    //TODO check activity lifecycle and pertinent action to make when entering new states
    // regarding connection / disconnection of googleapiclient, start stop GPSProvider updates
    // etc...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Calamar");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        buildGoogleApiClient();  // will connect in onResume(), errors are handled in onConnectionFailed()
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!resolvingError) {
            CalamarApplication.getInstance().getGoogleApiClient().connect();
            // if errors, such as no google play apk, onConnectionFailed will handle the errors
        }
    }

    @Override
    protected void onStop() {
        CalamarApplication.getInstance().getGoogleApiClient().disconnect();
        super.onStop();
    }
    // *********************************************************************************************


    /**
     * This shows a Dialog provided by Google Play services that's appropriate for the given error.
     * The dialog may simply provide a message explaining the error, but it may also provide an action
     * to launch an activity that can resolve the error
     * (such as when the user needs to install a newer version of Google Play services).
     *
     * @param errorCode , the error code returned by onConnectionFailed
     */
    private void showGoogleApiErrorDialog(int errorCode) {
        // retrieve dialog for errorCode, if user cancel finish activity,
        // we cannot do much more...google play apk must be present
        Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode,
                MainActivity.ERROR_RESOLUTION_REQUEST, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.e(MainActivity.TAG, "error dialog cancelled");
                        //works even if dialog cancelled without clicking any button
                        finish();//TODO maybe refine..and create a method to handle this kind of actions
                    }
                });
        // reset resolvingError to false when dialog dismissed
        errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                resolvingError = false;
            }
        });
        // show dialog
        errorDialog.show();
    }

    /**
     * Creates google api client object
     *
     * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi.html">
     * FusedLocationProviderAPI</a>
     */
    private synchronized void buildGoogleApiClient() {
        CalamarApplication.getInstance().setGoogleApiClient(
                new GoogleApiClient.Builder(CalamarApplication.getInstance())
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this).build());
        // TODO check issue #59
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
    /**
     * Verifies google play services availability on the device
     */
    //keeped just in case.., not used now, I go the other way by connecting and then eventually
    //handle errors in onConnectionFailed
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailabilitySingleton = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailabilitySingleton.isGooglePlayServicesAvailable(CalamarApplication.getInstance());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailabilitySingleton.isUserResolvableError(resultCode)) {
                apiAvailabilitySingleton.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();

            } else {
                Log.e(TAG, "This device is not supported. play services unavailable " +
                        "and automatic error resolution failed. error code : " + resultCode);
                //show dialog using geterrordialog on singleton
                finish();
            }
            return false;
        }
        return true;
    }
}
