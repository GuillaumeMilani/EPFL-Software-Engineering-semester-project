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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.item.CreateItemActivity;
import ch.epfl.sweng.calamar.push.RegistrationIntentService;

public abstract class BaseActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private CalamarApplication app;

    // LogCat tag
    private static final String TAG = BaseActivity.class.getSimpleName();

    // activity request codes
    private static final int ERROR_RESOLUTION_REQUEST = 1001;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1002;
    protected static final int ACCOUNT_CHOOSEN = 3001;


    // google api related stuff
    private boolean resolvingError;

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
        app = CalamarApplication.getInstance();
        if (!app.isGoogleApiClientCreated()) {
            buildGoogleApiClient();
        }  // will connect in onResume(), errors are handled in onConnectionFailed()
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.onActivityResumed(this);

        GoogleApiClient googleApiClient = app.getGoogleApiClient();
        if (!resolvingError &&
                !googleApiClient.isConnected() &&
                !googleApiClient.isConnecting()) {
            googleApiClient.connect();
            // if errors, such as no google play apk, onConnectionFailed will handle the errors
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        GoogleApiClient googleApiClient = app.getGoogleApiClient();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }
    // *********************************************************************************************

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
                connectionResult.startResolutionForResult(this, ERROR_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                app.getGoogleApiClient().connect();
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
                                app.getGoogleApiClient();
                        if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                            googleApiClient.connect();
                        }
                        break;
                    default:
                        Log.e(TAG, "google API client definitely can't connect...");
                        finish();//TODO maybe refine ?
                }
                break;
            //TODO code 1002 http://www.androiddesignpatterns.com/2013/01/google-play-services-setup.html
            case REQUEST_CODE_PICK_ACCOUNT: // TODO ?? is this still useful ??
                /*
                if (resultCode == RESULT_OK) {
                    String accountName = data.getStringExtra(
                            AccountManager.KEY_ACCOUNT_NAME);
                    AccountUtils.setAccountName(this, accountName);
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "This application requires a Google account.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                */
                break;
            case ACCOUNT_CHOOSEN:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // get account name
                        String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                        CalamarApplication.getInstance().setCurrentUserName(accountName);
                        afterAccountAuthentication();
                        break;
                    default:
                        Log.e(BaseActivity.TAG, "Didn't choose an account");
                        finish();
                }
                break;
            default:
                throw new IllegalStateException("onActivityResult : unknown request ! ");
        }
    }


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
                ERROR_RESOLUTION_REQUEST, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.e(TAG, "error dialog cancelled");
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
    }

    /**
     * Called after the account was authenticated
     */
    private void afterAccountAuthentication() {
        new createNewUserTask(CalamarApplication.getInstance().getCurrentUserName(), this).execute();
        // The user need to be authenticated before registration
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }


    /**
     * Launches an activity allowing the user to create an item
     *
     * @param v The + button
     */
    public void createItem(View v) {
        Intent intent = new Intent(this, CreateItemActivity.class);
        startActivity(intent);
    }


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
                return DatabaseClientLocator.getDatabaseClient().newUser(name,
                        Settings.Secure.getString(getContentResolver(),
                                Settings.Secure.ANDROID_ID));//"aaaaaaaaaaaaaaaa",354436053190805
            } catch (DatabaseClientException e) {
                e.printStackTrace();
                // TODO make it works like the other asynctask,
                // and make use of context safe
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer id) {
            if (id != null) {
                CalamarApplication.getInstance().setCurrentUserID(id);
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
