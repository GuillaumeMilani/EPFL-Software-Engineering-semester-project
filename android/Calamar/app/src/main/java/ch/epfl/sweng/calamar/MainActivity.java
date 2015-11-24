package ch.epfl.sweng.calamar;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.chat.ChatFragment;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.map.GPSProvider;
import ch.epfl.sweng.calamar.map.MapFragment;
import ch.epfl.sweng.calamar.push.RegistrationIntentService;

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
    private static final int ERROR_CLIENT_REQUEST = 1001;
    private static final int ERROR_REGISTRATION_REQUEST = 2001;
    private static final int ACCOUNT_CHOOSEN = 3001;

    // google api related stuff
    private boolean resolvingError;

    private CalamarApplication app;


    //TODO check activity lifecycle and pertinent action to make when entering new states
    // regarding connection / disconnection of googleapiclient, start stop GPSProvider updates
    // etc...

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
        app.getGoogleApiClient().connect();
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
                connectionResult.startResolutionForResult(this, ERROR_CLIENT_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                app.getGoogleApiClient().connect();
            }
        } else {
            resolvingError = true;
            Log.e(TAG, "google API client failed to connect: no automatic resolution, error = "
                    + connectionResult.getErrorCode());

            // show error dialog
            showGoogleApiErrorDialog(connectionResult.getErrorCode(), ERROR_CLIENT_REQUEST);
        }
    }
    // *********************************************************************************************

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ERROR_REGISTRATION_REQUEST :
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //launch intent again
                        Intent intent = new Intent(this, RegistrationIntentService.class);
                        startService(intent);
                        break;
                    default:
                        Log.e(MainActivity.TAG, "registration can't connect");
                        finish();
                }
            case ERROR_CLIENT_REQUEST:
                resolvingError = false;
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Make sure the app is not already connected or attempting to connect
                        GoogleApiClient googleApiClient =
                                app.getGoogleApiClient();
                        if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                            googleApiClient.connect();
                        }
                        break;
                    default:
                        Log.e(MainActivity.TAG, "google API client definitely can't connect...");
                        finish();//TODO maybe refine ?
                }
                break;
            case ACCOUNT_CHOOSEN :
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // get account name
                        String  accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                        Log.i(TAG, accountName);
                        app.setCurrentUserName(accountName);

                        afterAccountAuthentication();
                        break;
                    default:
                        Log.e(MainActivity.TAG,"Didn't choose an account");
                        finish();
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

        app = CalamarApplication.getInstance();

        // Layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Calamar");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        buildGoogleApiClient();  // will connect in onResume(), errors are handled in onConnectionFailed()

        //choose account dialog
        Intent accountIntent = AccountManager.newChooseAccountIntent(null, null,
                new String[] {"com.google"}, true, null, null,
                null, null);
        startActivityForResult(accountIntent,ACCOUNT_CHOOSEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!resolvingError) {
            app.getGoogleApiClient().connect();
            // if errors, such as no google play apk, onConnectionFailed will handle the errors
        }
    }

    @Override
    protected void onStop() {
        app.getGoogleApiClient().disconnect();
        super.onStop();
        app.getDatabaseHandler().closeDatabase();
    }

    @Override
    protected void onPause() {
        super.onPause();
        new applyPendingDatabaseOperationsTask();
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
    private void showGoogleApiErrorDialog(int errorCode,int errorType) {
        // retrieve dialog for errorCode, if user cancel finish activity,
        // we cannot do much more...google play apk must be present
        Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode,
                 errorType, new DialogInterface.OnCancelListener() {
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
        app.setGoogleApiClient(
                new GoogleApiClient.Builder(app)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this).build());
        // TODO check issue #59
    }

    /**
     * Verifies google play services availability on the device
     */
    //keeped just in case.., not used now, I go the other way by connecting and then eventually
    //handle errors in onConnectionFailed
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailabilitySingleton = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailabilitySingleton.isGooglePlayServicesAvailable(app);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailabilitySingleton.isUserResolvableError(resultCode)) {
                showGoogleApiErrorDialog(resultCode,ERROR_REGISTRATION_REQUEST);
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

    /**
     * Called after the account was authenticated
     */
    private void afterAccountAuthentication()
    {
       new createNewUserTask(app.getCurrentUserName(),this).execute();

        // token generation
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        try {
            ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
            ChatFragment chatFragment = (ChatFragment) adapter.getItem(1);;
            chatFragment.setActualUser();
        }
        catch (Exception e)
        {
            Log.e(TAG,"Couldn't update the chat");
        }

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

    private class applyPendingDatabaseOperationsTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... v) {
            app.getDatabaseHandler().applyPendingOperations();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            app.setLastUsersRefresh(new Date());
            app.setLastItemsRefresh(new Date());
        }
    }


    /**
     * Async task for sending a message.
     */
    private class createNewUserTask extends AsyncTask<Void, Void, Integer> {
        private String name = null;
        private Context context;

        public createNewUserTask(String name, Context context) {
            this.name = name;
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Void... v) {
            try {
                //Get the device id.
                return DatabaseClientLocator.getDatabaseClient().newUser(name, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));//"aaaaaaaaaaaaaaaa",354436053190805
            } catch (DatabaseClientException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer id) {
            if (id != null) {
                app.setCurrentUserID(id);
                // Show toast
                Context context = getApplicationContext();
                CharSequence text = "Connected as " + name;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                AlertDialog.Builder newUser = new AlertDialog.Builder(context);
                newUser.setTitle(R.string.new_account_creation_fail);
                newUser.setPositiveButton(R.string.new_account_creation_retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new createNewUserTask(name, context).execute();
                    }
                });
                newUser.show();
            }
        }
    }
}
