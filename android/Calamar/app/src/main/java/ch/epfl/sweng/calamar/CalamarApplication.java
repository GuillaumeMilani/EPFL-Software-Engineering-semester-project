package ch.epfl.sweng.calamar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.securepreferences.SecurePreferences;

import java.util.Calendar;
import java.util.Date;

import ch.epfl.sweng.calamar.push.RegistrationIntentService;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.StorageManager;

public final class CalamarApplication extends Application implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private static final String TAG = CalamarApplication.class.getSimpleName();

    private static final String LAST_USERS_REFRESH_SP = "lastUsersRefresh";
    private static final String LAST_ITEMS_REFRESH_SP = "lastItemsRefresh";
    private static final String CURRENT_USER_ID_SP = "currentUserID";
    private static final String CURRENT_USER_NAME_SP = "currentUserName";
    private static final String TODAY_IMAGE_COUNT_SP = "todayImageCount";
    private static final String TODAY_FILE_COUNT_SP = "todayFileCount";
    private static final String USER_PREF_NAME = "user_pref.xml";

    private static final int UPDATE_DB_TIME = 600000;

    //TODO Why volatile?
    private static volatile CalamarApplication instance;

    private final int WAITING_TIME = 500;

    private SQLiteDatabaseHandler dbHandler;
    private StorageManager storageManager;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Calendar calendar;
    private int day;

    // Google client to interact with Google API
    //https://developers.google.com/android/guides/api-client
    private GoogleApiClient googleApiClient = null;

    //Used to check and do stuff if the app is on background -- not foolproof,
    // there is no real good way to check that apparently, but seems to be working fine
    private boolean onForeground = true;
    private int resumed = 0;
    private int paused = 0;
    private Handler handler;

    /**
     * Returns the current instance of the application.
     *
     * @return A singleton
     */
    public static CalamarApplication getInstance() {
        if (instance == null) {
            //TODO No way to get string resource without instance I guess ?
            throw new IllegalStateException(instance.getString(R.string.application_null));
        }
        return instance;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    //TODO Clean method once things are decided / tested
    public void onCreate() {
        super.onCreate();
        User test = new User(1, "Bob");
        instance = this;
        sp = new SecurePreferences(this, test.getPassword(), USER_PREF_NAME);
        editor = sp.edit();
        handler = new Handler();
        dbHandler = SQLiteDatabaseHandler.getInstance();
        storageManager = StorageManager.getInstance();
        calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        loopDatabaseUpdate();
    }


    /**
     * Get the database handler containing the recipients and the items.
     *
     * @return the database handler
     */
    public SQLiteDatabaseHandler getDatabaseHandler() {
        return dbHandler;
    }

    /**
     * Get the StorageManager allowing to store and retrieve items
     *
     * @return the storage manager
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Set if the token was sent to server
     *
     * @param bool the bool to be set
     */
    public void setTokenSent(boolean bool) {
        editor.putBoolean(RegistrationIntentService.SENT_TOKEN_TO_SERVER, bool).apply();
    }

    /**
     * Returns if the registration token was sent to server
     *
     * @return the boolean
     */
    public boolean getTokenSent() {
        return sp.getBoolean(RegistrationIntentService.SENT_TOKEN_TO_SERVER, false);
    }

    /**
     * Set the last time the application refreshed the users.
     *
     * @param lastTime A long
     */
    public void setLastUsersRefresh(long lastTime) {
        editor.putLong(LAST_USERS_REFRESH_SP, lastTime).apply();
    }

    /**
     * Sets the last time the application refreshed the users.
     *
     * @param lastTime A date
     */
    public void setLastUsersRefresh(Date lastTime) {
        setLastUsersRefresh(lastTime.getTime());
    }

    /**
     * Get the last time the application refreshed the users.
     *
     * @return the date.
     */
    public Date getLastUsersRefresh() {
        return new Date(sp.getLong(LAST_USERS_REFRESH_SP, 0));
    }

    /**
     * Set the time the application last refreshed the items.
     *
     * @param lastTime The date.
     */
    public void setLastItemsRefresh(long lastTime) {
        editor.putLong(LAST_ITEMS_REFRESH_SP, lastTime).apply();
    }

    public void setLastItemsRefresh(Date date) {
        setLastItemsRefresh(date.getTime());
    }

    /**
     * Returns the date corresponding to the last time the application refreshed the items.
     *
     * @return the date
     */
    public Date getLastItemsRefresh() {
        return new Date(sp.getLong(LAST_ITEMS_REFRESH_SP, 0));
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
     * Sets the number of images stored today.
     *
     * @param count the number to be set
     */
    public void setImageCount(int count) {
        editor.putInt(TODAY_IMAGE_COUNT_SP, count);
    }

    /**
     * Sets the number of files stored today.
     *
     * @param count the number to be set
     */
    public void setFileCount(int count) {
        editor.putInt(TODAY_FILE_COUNT_SP, count);
    }

    /**
     * Increase today's image count by 1.
     */
    public void increaseImageCount() {
        editor.putInt(TODAY_IMAGE_COUNT_SP, getTodayFileCount() + 1).apply();
    }

    /**
     * Increase today's file count by 1.
     */
    public void increaseFileCount() {
        editor.putInt(TODAY_FILE_COUNT_SP, getTodayFileCount() + 1).apply();
    }

    /**
     * Returns the name of the current user
     *
     * @return the name of the user
     */
    public String getCurrentUserName() {
        return sp.getString(CURRENT_USER_NAME_SP, getString(R.string.empty_string));
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
     * Returns the number of ImageItem stored today.
     *
     * @return the number of ImageItem stored today
     */
    public int getTodayImageCount() {
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (curDay != day) {
            day = curDay;
            resetImageCount();
            resetFileCount();
            return 0;
        }
        return sp.getInt(TODAY_IMAGE_COUNT_SP, 0);
    }

    /**
     * Returns the number of FileItem stored today.
     *
     * @return the number of FileItem stored today
     */
    public int getTodayFileCount() {
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (curDay != day) {
            day = curDay;
            resetImageCount();
            resetFileCount();
            return 0;
        }
        return sp.getInt(TODAY_FILE_COUNT_SP, 0);
    }

    /**
     * Resets the Username to an empty String.
     */
    public void resetUsername() {
        setCurrentUserName(getString(R.string.empty_string));
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
        setLastItemsRefresh(0);
    }

    /**
     * Resets the lastUsersRefresh to 0
     */
    public void resetLastUsersRefresh() {
        setLastUsersRefresh(0);
    }

    /**
     * Resets the number of images stored today to 0.
     */
    public void resetImageCount() {
        setImageCount(0);
    }

    /**
     * Resets the number of files stored today to 0.
     */
    public void resetFileCount() {
        setFileCount(0);
    }

    /**
     * Resets everything to its default value {@see resetUserID}
     * {@see resetUsername}{@see resetLastItemsRefresh}
     * {@see resetLastUsersRefresh}{@see resetImageCount}{@see resetFileCount}
     */
    public void resetPreferences() {
        resetUserID();
        resetUsername();
        resetLastItemsRefresh();
        resetLastUsersRefresh();
        resetImageCount();
        resetFileCount();
    }

    /**
     * Sets the google api client
     *
     * @param googleApiClient the google api client to be set.
     */
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

    /**
     * Returns the Google Api Client
     *
     * @return the instance of the google api client
     * @throws IllegalStateException if the google api client has not been created yet.
     */
    public GoogleApiClient getGoogleApiClient() {
        if (null == googleApiClient) {
            Log.e(CalamarApplication.TAG, getString(R.string.get_google_client_not_created));
            throw new IllegalStateException(getString(R.string.get_google_client_not_created));
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
                    new ApplyPendingDatabaseOperationsTask().execute();
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
                    new ApplyPendingDatabaseOperationsTask().execute();
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

    /**
     * AsyncTask to apply the pending operations in the local database
     */
    public class ApplyPendingDatabaseOperationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... v) {
            if (dbHandler.areOperationsPending()) {
                dbHandler.applyPendingOperations();
            }
            Log.i("Database", "Applied operations");
            return null;
        }
    }

    /**
     * So that if there is a problem with the background check, we won't have to retrieve all items each time.
     */
    private void loopDatabaseUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new ApplyPendingDatabaseOperationsTask();
                loopDatabaseUpdate();
            }
        }, UPDATE_DB_TIME);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_BACKGROUND) {
            dbHandler.applyPendingOperations();
        } else if (level >= TRIM_MEMORY_MODERATE && level < TRIM_MEMORY_COMPLETE) {
            dbHandler.applyPendingOperations();
            storageManager.cancelWritingTasks(5);
        } else if (level <= TRIM_MEMORY_RUNNING_CRITICAL && level > TRIM_MEMORY_RUNNING_MODERATE) {
            dbHandler.applyPendingOperations();
            storageManager.cancelWritingTasks(1);
        } else if (level >= TRIM_MEMORY_COMPLETE) {
            dbHandler.applyPendingOperations();
            storageManager.cancelWritingTasks(1);
        } else if ((level > TRIM_MEMORY_RUNNING_CRITICAL && level < TRIM_MEMORY_MODERATE)
                || level < TRIM_MEMORY_RUNNING_MODERATE) {
            storageManager.retryFailedWriting();
        }
    }


}
