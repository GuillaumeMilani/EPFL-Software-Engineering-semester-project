package ch.epfl.sweng.calamar;

import android.app.Application;

public class CalamarApplication extends Application {

    //TODO There is debate on using a Singleton or not
    //TODO We could use SharedPreferences to store an hashed userID and userName

    private static CalamarApplication application;
    private SQLiteDatabaseHandler db;

    public CalamarApplication getInstance(){
        return application;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        application=this;
        db=new SQLiteDatabaseHandler(this);
    }

    public SQLiteDatabaseHandler getDB(){
        return db;
    }
}
