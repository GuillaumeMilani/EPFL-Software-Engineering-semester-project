package ch.epfl.sweng.calamar;

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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Button showMapBtn = null;
    Button showChatBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //retrieve UI element
        showChatBtn = (Button)findViewById(R.id.showChatBtn);
        showChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ChatActivity.class);
            }
        });

        showMapBtn = (Button)findViewById(R.id.showMapBtn);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mapSettingsAreOk()) {
                    startActivity(MapsActivity.class);
                } else {
                    //TODO deactivate something until resolved ?
                }
            }
        });
    }

    private boolean mapSettingsAreOk() {
        //TODO
        return false;
    }

    /**
     * Starts a child activity
     */
    public void startActivity(Class<?> cls) {
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
        //TODO
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        googleApiClient.connect();
    }

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

}
