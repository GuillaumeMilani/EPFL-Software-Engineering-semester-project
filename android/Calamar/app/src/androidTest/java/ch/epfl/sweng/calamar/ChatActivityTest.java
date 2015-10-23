package ch.epfl.sweng.calamar;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ChatActivityTest {

    @Rule
    public ActivityTestRule<ChatActivity> mActivityRule = new ActivityTestRule<>(
            ChatActivity.class);


    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void basicTest() {
        ChatActivity activity = mActivityRule.getActivity();
    }
}
