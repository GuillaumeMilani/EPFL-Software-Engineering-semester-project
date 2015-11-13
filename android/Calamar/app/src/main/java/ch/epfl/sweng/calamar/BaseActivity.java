package ch.epfl.sweng.calamar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    private CalamarApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        app=CalamarApplication.getInstance();
    }

    @Override
    protected void onResume(){
        super.onResume();
        app.onActivityResumed(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        app.onActivityPaused(this);
    }
}
