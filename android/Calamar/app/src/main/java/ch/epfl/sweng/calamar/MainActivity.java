package ch.epfl.sweng.calamar;

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
import ch.epfl.sweng.calamar.map.MapFragment;

public class MainActivity extends BaseActivity {

    // Tabs related stuff
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapFragment(), "Map");
        adapter.addFragment(new ChatFragment(), "Chat");
        viewPager.setAdapter(adapter);
    }


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
    //TODO check activity lifecycle and pertinent action to make when entering new states
    // regarding connection / disconnection of googleapiclient, start stop GPSProvider updates
    // etc...
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
    }

    // *********************************************************************************************

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
