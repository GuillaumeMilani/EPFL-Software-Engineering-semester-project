package ch.epfl.sweng.calamar;


import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.calamar.chat.ChatFragment;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.map.MapFragment;

/**
 * Created by Guillaume on 12.11.2015.
 */

public class MainActivity extends BaseActivity {

    // Tabs related stuff
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    // activity request codes
    private static final int ACCOUNT_CHOOSEN = 3001;

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    // *********************************************************************************************


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACCOUNT_CHOOSEN :
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // get account name
                        String  accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                        Log.i(TAG, accountName);
                        CalamarApplication.getInstance().setCurrentUserName(accountName);

                        afterAccountAuthentication();
                        break;
                    default:
                        Log.e(MainActivity.TAG,"Didn't choose an account");
                        finish();
                }

                break;
            default:
                throw new IllegalStateException("onActivityResult : unknown request ! ");
        }
    }

    //TODO check activity lifecycle and pertinent action to make when entering new states
    // regarding connection / disconnection of googleapiclient, start stop GPSProvider updates
    // etc...

    // *********************************************************************************************
    // ACTIVITY LIFECYCLE CALLBACKS
    // https://developer.android.com/training/basics/activity-lifecycle/starting.html
    // even better :
    // https://stackoverflow.com/questions/12203651/why-is-onresume-called-when-an-activity-starts
    //
    // every time screen is rotated, activity is destroyed/recreated :
    // https://stackoverflow.com/questions/7618703/activity-lifecycle-oncreate-called-on-every-re-orientation
    // maybe prevent this ...
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Calamar");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //choose account dialog
        Intent accountIntent = AccountManager.newChooseAccountIntent(null, null,
                new String[] {"com.google"}, true, null, null,
                null, null);
        startActivityForResult(accountIntent,ACCOUNT_CHOOSEN);

    }
    // *********************************************************************************************

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapFragment(), "Map");
        adapter.addFragment(new ChatFragment(), "Chat");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    /**
     * Called after the account was authenticated
     */
    private void afterAccountAuthentication()
    {
        new createNewUserTask(CalamarApplication.getInstance().getCurrentUserName(),this).execute();


        try {
            ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
            ChatFragment chatFragment = (ChatFragment) adapter.getItem(1);;
            chatFragment.setActualUser();
        }
        catch (Exception e)
        {
            Log.e(TAG,"Couldn't update the chat");
        }

    }

    /**
     * Async task for sending a message.
     */
    private class createNewUserTask extends AsyncTask<Void, Void, Integer> {
        private String name = null;
        private Context context;

        public createNewUserTask(String name, Context context) {
            this.name = name;
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Void... v) {
            try {
                //Get the device id.
                return DatabaseClientLocator.getDatabaseClient().newUser(name, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));//"aaaaaaaaaaaaaaaa",354436053190805
            } catch (DatabaseClientException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer id) {
            if (id != null) {
                CalamarApplication.getInstance().setCurrentUserID(id);
                // Show toast
                Context context = getApplicationContext();
                CharSequence text = "Connected as " + name;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                AlertDialog.Builder newUser = new AlertDialog.Builder(context);
                newUser.setTitle(R.string.new_account_creation_fail);
                newUser.setPositiveButton(R.string.new_account_creation_retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new createNewUserTask(name, context).execute();
                    }
                });
                newUser.show();
            }
        }
    }

}
