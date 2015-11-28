package ch.epfl.sweng.calamar;


import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.calamar.chat.ChatFragment;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.map.MapFragment;

/**
 * Created by Guillaume on 12.11.2015.
 */

public class MainActivity extends BaseActivity {

    // Tabs related stuff
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    // *********************************************************************************************


    public static final String TABKEY = MainActivity.class.getCanonicalName() +  ":TABID";
    public enum TabID {

        MAP(CalamarApplication.getInstance().getString(R.string.label_map_tab)),
        CHAT(CalamarApplication.getInstance().getString(R.string.label_chat_tab));

        private final String label;

        TabID(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    };

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
        setupViewPager(viewPager, getIntent());

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // get front tab from intent or set it to Map (default)
        int tabId = getIntent().getIntExtra(MainActivity.TABKEY, TabID.MAP.ordinal());
        viewPager.setCurrentItem(tabId);
        //choose account dialog
        // TODO if possible get rid of this deprecated call, see javadoc
        Intent accountIntent = AccountManager.newChooseAccountIntent(null, null,
                new String[] {"com.google"}, true, null, null,
                null, null);
        startActivityForResult(accountIntent, BaseActivity.ACCOUNT_CHOOSEN);
    }
    // *********************************************************************************************

    private void setupViewPager(ViewPager viewPager, Intent intent) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // initial map position, either default or set from intent (activity started from item's
        // detail view)
        double latitude = intent.getDoubleExtra(MapFragment.LATITUDEKEY, MapFragment.DEFAULTLATITUDE);
        double longitude = intent.getDoubleExtra(MapFragment.LONGITUDEKEY, MapFragment.DEFAULTLONGITUDE);

        Bundle args = new Bundle();
        args.putDouble(MapFragment.LATITUDEKEY, latitude);
        args.putDouble(MapFragment.LONGITUDEKEY, longitude);
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(args);

        // BEWARE OF THE ORDER, must be consistent with TabID
        adapter.addFragment(mapFragment, TabID.MAP.toString());
        adapter.addFragment(new ChatFragment(), TabID.CHAT.toString());
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
}
