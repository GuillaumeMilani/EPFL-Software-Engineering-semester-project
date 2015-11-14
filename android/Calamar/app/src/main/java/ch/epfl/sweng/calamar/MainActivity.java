package ch.epfl.sweng.calamar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import ch.epfl.sweng.calamar.chat.ChatActivity;
import ch.epfl.sweng.calamar.map.MapsActivity;

public class MainActivity extends BaseActivity {

    //TODO check activity lifecycle and pertinent action to make when entering new states
    // regarding connection / disconnection of googleapiclient, start stop GPSProvider updates
    // etc...

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // retrieve UI element
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

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts a child activity
     */
    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(CalamarApplication.getInstance(), cls);
        startActivity(intent);
    }


}
