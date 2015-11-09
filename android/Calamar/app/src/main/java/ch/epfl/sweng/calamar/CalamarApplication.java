package ch.epfl.sweng.calamar;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class CalamarApplication extends Application {

    //TODO There is debate on using a Singleton or not

    private static CalamarApplication application;
    private SQLiteDatabaseHandler db;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private static final String CALAMAR_PREFERENCES = "ch.epfl.sweng.calamar";
    private static final String LAST_USERS_REFRESH_SP = "lastUsersRefresh";
    private static final String LAST_ITEMS_REFRESH_SP = "lastItemsRefresh";
    private static final String CURRENT_USER_ID_SP = "currentUserID";
    private static final String CURRENT_USER_NAME_SP = "currentUserName";

    /**
     * Returns the current instance of the application.
     *
     * @return A singleton
     */
    public static CalamarApplication getInstance() {
        if (application==null){
            application=new CalamarApplication();
        }
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        sp = getSharedPreferences(CALAMAR_PREFERENCES, Context.MODE_PRIVATE);
        editor = sp.edit();
        setCurrentUserID(1);
        setCurrentUserName("Bob");
        db = SQLiteDatabaseHandler.getInstance();

        //TODO remove once database is thoroughly tested (tests delete all entries)
        //setLastItemsRefresh(new Date(0));
        //setLastUsersRefresh(new Date(0));
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
     * @return the user
     */
    public User getCurrentUser(){
        return new User(getCurrentUserID(),getCurrentUserName());
    }

    /**
     * Resets the Username to an empty String.
     */
    public void resetUsername(){
        setCurrentUserName("");
    }

    /**
     * Resets the user ID to -1
     */
    public void resetUserID(){
        setCurrentUserID(-1);
    }

    /**
     * Resets the lastItemsRefresh to 0
     */
    public void resetLastItemsRefresh(){
        setLastItemsRefresh(new Date(0));
    }

    /**
     * Resets the lastUsersRefresh to 0
     */
    public void resetLastUsersRefresh(){
        setLastUsersRefresh(new Date(0));
    }

    /**
     * Resets everything to its default value {@see resetUserID}{@see resetUsername}{@see resetLastItemsRefresh}{@see resetLastUsersRefresh}
     */
    public void resetPreferences(){
        resetUserID();
        resetUsername();
        resetLastItemsRefresh();
        resetLastUsersRefresh();
    }
}
