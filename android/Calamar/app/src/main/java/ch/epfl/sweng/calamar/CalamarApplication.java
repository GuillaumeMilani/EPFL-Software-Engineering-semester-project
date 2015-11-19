package ch.epfl.sweng.calamar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.securepreferences.SecurePreferences;

import java.util.Date;

import ch.epfl.sweng.calamar.recipient.User;

public final class CalamarApplication extends Application implements Application.ActivityLifecycleCallbacks {

    //TODO Why volatile?
    private static volatile CalamarApplication instance;
    private SQLiteDatabaseHandler db;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private static final String LAST_USERS_REFRESH_SP = "lastUsersRefresh";
    private static final String LAST_ITEMS_REFRESH_SP = "lastItemsRefresh";
    private static final String CURRENT_USER_ID_SP = "currentUserID";
    private static final String CURRENT_USER_NAME_SP = "currentUserName";
    private static final String TAG = CalamarApplication.class.getSimpleName();

    // Google client to interact with Google API
    //https://developers.google.com/android/guides/api-client
    private GoogleApiClient googleApiClient = null;

    //Used to check and do stuff if the app is on background -- not foolproof, there is no real good way apparently
    private boolean onForeground = true;
    private int resumed = 0;
    private int paused = 0;
    private Handler handler;
    private final int WAITING_TIME = 500;

    /**
     * Returns the current instance of the application.
     *
     * @return A singleton
     */
    public static CalamarApplication getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Application is null");
        }
        return instance;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    //TODO Clean method once things are decided
    public void onCreate() {
        super.onCreate();
        User test = new User(1, "Bob");
        instance = this;
        sp = new SecurePreferences(this, test.getPassword(), "user_pref.xml");
        editor = sp.edit();
        db = SQLiteDatabaseHandler.getInstance();
        handler = new Handler();
        setLastItemsRefresh(new Date(0));
        setLastUsersRefresh(new Date(0));
    }

    /**
     * Get the database containing the recipients and the items.
     *
     * @return the database
     */
    public SQLiteDatabaseHandler getDB() {
        return db;
    }

    /**
     * Set the last time the application refreshed the users.
     *
     * @param lastDate The date.
     */
    public void setLastUsersRefresh(Date lastDate) {
        editor.putLong(LAST_USERS_REFRESH_SP, lastDate.getTime()).apply();
    }

    /**
     * Get the last time the application refreshed the users.
     *
     * @return the long corresponding to the date.
     */
    public long getLastUsersRefresh() {
        return sp.getLong(LAST_USERS_REFRESH_SP, 0);
    }

    /**
     * Set the time the application last refreshed the items.
     *
     * @param lastDate The date.
     */
    public void setLastItemsRefresh(Date lastDate) {
        editor.putLong(LAST_ITEMS_REFRESH_SP, lastDate.getTime()).apply();
    }

    /**
     * Returns the long corresponding to the last time the application refreshed the items.
     *
     * @return the date as a long
     */
    public long getLastItemsRefresh() {
        return sp.getLong(LAST_ITEMS_REFRESH_SP, 0);
    }

    /**
     * Sets the current user id.
     *
     * @param id the id the current user will have
     */
    public void setCurrentUserID(int id) {
        editor.putInt(CURRENT_USER_ID_SP, id).apply();
    }

    /**
     * Returns the ID of the current user
     *
     * @return the id, or -1 if it is not defined.
     */
    public int getCurrentUserID() {
        return sp.getInt(CURRENT_USER_ID_SP, -1);
    }

    /**
     * Sets the name of the current user
     *
     * @param name The new name of the user
     */
    public void setCurrentUserName(String name) {
        editor.putString(CURRENT_USER_NAME_SP, name).apply();
    }

    /**
     * Returns the name of the current user
     *
     * @return the name of the user
     */
    public String getCurrentUserName() {
        return sp.getString(CURRENT_USER_NAME_SP, "");
    }

    /**
     * Returns the current User
     *
     * @return the user
     */
    public User getCurrentUser() {
        return new User(getCurrentUserID(), getCurrentUserName());
    }

    /**
     * Resets the Username to an empty String.
     */
    public void resetUsername() {
        setCurrentUserName("");
    }

    /**
     * Resets the user ID to -1
     */
    public void resetUserID() {
        setCurrentUserID(-1);
    }

    /**
     * Resets the lastItemsRefresh to 0
     */
    public void resetLastItemsRefresh() {
        setLastItemsRefresh(new Date(0));
    }

    /**
     * Resets the lastUsersRefresh to 0
     */
    public void resetLastUsersRefresh() {
        setLastUsersRefresh(new Date(0));
    }

    /**
     * Resets everything to its default value {@see resetUserID}{@see resetUsername}{@see resetLastItemsRefresh}{@see resetLastUsersRefresh}
     */
    public void resetPreferences() {
        resetUserID();
        resetUsername();
        resetLastItemsRefresh();
        resetLastUsersRefresh();
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
//        //TODO ask guru, when unresolvable errors cause the main activity to finish (destroy activity)
//        //if user reopen, activity's oncreate is called again but because the app isn't killed,
//        // the following code will cause the app to crash (but we nearly don't care...enfin...)
//        if (this.googleApiClient != null) {
//            Log.e(CalamarApplication.TAG, "setGoogleApiClient : google api client is already created !");
//            throw new IllegalStateException("setGoogleApiClient : google api client is already created !");
//        }
        if (this.googleApiClient == null) {
            this.googleApiClient = googleApiClient;
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        if (null == googleApiClient) {
            Log.e(CalamarApplication.TAG, "getGoogleApiClient : google api client has not been created !");
            throw new IllegalStateException("getGoogleApiClient : google api client has not been created !");
        }
        return googleApiClient;
    }

    /**
     * Tells if the google API client is created
     *
     * @return true if it is, false otherwise
     */
    public boolean isGoogleApiClientCreated() {
        return googleApiClient != null;
    }


    //Used to check if app is in background (triggers database)
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isOnForeground()) {
                    //TODO db.applyPendingOperations() once improve_database is merged;
                }
            }
        }, WAITING_TIME);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isOnForeground()) {
                    //TODO same as above;
                    Toast.makeText(CalamarApplication.getInstance(), "OnForeground", Toast.LENGTH_SHORT).show();
                }
            }
        }, WAITING_TIME);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private boolean isOnForeground() {
        if (paused >= resumed && onForeground) {
            onForeground = false;
        } else if (resumed > paused && !onForeground) {
            onForeground = true;
        }
        return onForeground;
    }


}
