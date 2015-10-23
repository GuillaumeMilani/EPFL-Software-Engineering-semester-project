package ch.epfl.sweng.calamar;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class CalamarApplication extends Application {

    //TODO There is debate on using a Singleton or not
    //TODO We could use SharedPreferences to store an hashed userID and userName

    private static CalamarApplication application;
    private SQLiteDatabaseHandler db;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private final String CALAMAR_PREFERENCES = "ch.epfl.sweng.calamar";
    private final String LAST_USERS_REFRESH = "lastUsersRefresh";
    private final String LAST_ITEMS_REFRESH = "lastItemsRefresh";

    /**
     * Returns the current instance of the application.
     * @return A singleton
     */
    public CalamarApplication getInstance(){
        return application;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        application=this;
        db=new SQLiteDatabaseHandler(this);
        sp =getSharedPreferences(CALAMAR_PREFERENCES, Context.MODE_PRIVATE);
        editor=sp.edit();
        //TODO remove once database is thoroughly tested (tests delete all entries)
        setLastItemsRefresh(new Date(0));
        setLastUsersRefresh(new Date(0));
    }

    /**
     * Get the database containing the recipients and the items.
     * @return the database
     */
    public SQLiteDatabaseHandler getDB(){
        return db;
    }

    /**
     * Set the last time the application refreshed the users.
     * @param lastDate The date.
     */
    public void setLastUsersRefresh(Date lastDate){
        editor.putLong(LAST_USERS_REFRESH,lastDate.getTime()).apply();
    }

    /**
     * Get the last time the application refreshed the users.
     * @return the long corresponding to the date.
     */
    public long getLastUsersRefresh(){
        return sp.getLong(LAST_USERS_REFRESH,0);
    }

    /**
     * Set the time the application last refreshed the items.
     * @param lastDate The date.
     */
    public void setLastItemsRefresh(Date lastDate){
        editor.putLong(LAST_ITEMS_REFRESH,lastDate.getTime()).apply();
    }

    /**
     * Returns the long corresponding to the last time the application refreshed the items.
     * @return the date as a long
     */
    public long getLastItemsRefresh(){
       return sp.getLong(LAST_ITEMS_REFRESH,0);
    }
}
