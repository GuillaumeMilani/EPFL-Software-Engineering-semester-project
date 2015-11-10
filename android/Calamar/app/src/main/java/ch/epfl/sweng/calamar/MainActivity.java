package ch.epfl.sweng.calamar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // activity request codes
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int ERROR_RESOLUTION_REQUEST = 1001;

    // google api related stuff
    private boolean resolvingError;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient(); //will connect in onResume(), errors are handled in onConnectionFailed()

        //retrieve UI element
        Button showChatBtn = (Button) findViewById(R.id.showChatBtn);
        showChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ChatActivity.class);
            }
        });

        //TODO maybe disable until google api client connects succesfully
        Button showMapBtn = (Button) findViewById(R.id.showMapBtn);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MapsActivity.class);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!resolvingError) {
            CalamarApplication.getInstance().getGoogleApiClient().connect();
            //if errors, such as no google play apk, onConnectionFailed will handle the errors
        }
    }

    @Override
    protected void onStop() {
        CalamarApplication.getInstance().getGoogleApiClient().disconnect();
        super.onStop();
    }

    /**
     * Starts a child activity
     */
    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(CalamarApplication.getInstance(), cls);
        startActivity(intent);
    }

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //GOOGLE API CLIENT CALLBACKS METHODS
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
                //TODO callback
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                CalamarApplication.getInstance().getGoogleApiClient().connect();
            }
        } else {
            resolvingError = true;
            Log.e(TAG, "google API client failed to connect: no automatic resolution, error = "
                    + connectionResult.getErrorCode());

            //show error dialog
            showGoogleApiErrorDialog(connectionResult.getErrorCode());
            resolvingError = true;
        }
    }


    /**
     * This shows a Dialog provided by Google Play services that's appropriate for the given error.
     * The dialog may simply provide a message explaining the error, but it may also provide an action
     * to launch an activity that can resolve the error
     * (such as when the user needs to install a newer version of Google Play services).
     * @param errorCode , the error code returned by onConnectionFailed
     */
    private void showGoogleApiErrorDialog(int errorCode) {
        //retrieve dialog for errorCode, if user cancel finish activity,
        //we cannot do much more...google play apk must be present
        Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode,
                MainActivity.PLAY_SERVICES_RESOLUTION_REQUEST, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.e(MainActivity.TAG, "error dialog cancelled");
                        //works even if dialog cancelled without clicking any button
                        finish();//TODO maybe refine..
                    }
                });
        //reset resolvingError to false when dialog dismissed
        errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                resolvingError = false;
            }
        });
        //show dialog
        errorDialog.show();
    }

    /**
     * Creates google api client object
     * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi.html">
     *      FusedLocationProviderAPI</a>
     * */
    private synchronized void buildGoogleApiClient() {
        CalamarApplication.getInstance().setGoogleApiClient(
                new GoogleApiClient.Builder(CalamarApplication.getInstance())
                    .addApi(LocationServices.API)//TODO add service push TONY
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build());
    }

//    /**
//     * Verifies google play services availability on the device
//     */
//    //keeped just in case.., not used now, I go the other way by connecting and then eventually
//    //handle errors in onConnectionFailed
//    private boolean checkPlayServices() {
//        GoogleApiAvailability apiAvailabilitySingleton = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailabilitySingleton.isGooglePlayServicesAvailable(CalamarApplication.getInstance());
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailabilitySingleton.isUserResolvableError(resultCode)) {
//                apiAvailabilitySingleton.getErrorDialog(this, resultCode,
//                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
//
//            } else {
//                Log.e(TAG, "This device is not supported. play services unavailable " +
//                        "and automatic error resolution failed. error code : " + resultCode);
//                //show dialog using geterrordialog on singleton
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }
}
