package ch.epfl.sweng.calamar;

import android.app.Application;

public class CalamarApplication extends Application {

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
