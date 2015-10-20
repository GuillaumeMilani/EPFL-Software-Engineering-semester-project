package ch.epfl.sweng.calamar;

import android.app.Application;

public class CalamarApplication extends Application {

    private SQLiteDatabaseHandler db;

    public CalamarApplication getInstance(){
        return this;
    }

    private CalamarApplication(){};

    @Override
    public void onCreate(){
        super.onCreate();
        db=new SQLiteDatabaseHandler(this);
    }

    public SQLiteDatabaseHandler getDB(){
        return db;
    }
}
