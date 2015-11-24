package ch.epfl.sweng.calamar;


import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.item.CreateItemActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(JUnit4.class)
public class CreateItemActivityTest extends ActivityInstrumentationTestCase2<CreateItemActivity> {

    private final static String HELLO_ALICE = "Hello Alice !";
    private final static String ALICE = "Alice";

    @Rule
    public final ActivityTestRule<CreateItemActivity> mActivityRule = new ActivityTestRule<>(
            CreateItemActivity.class);

    public CreateItemActivityTest() {
        super(CreateItemActivity.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        DatabaseClientLocator.setDatabaseClient(new ConstantDatabaseClient());
        CalamarApplication.getInstance().getDatabaseHandler().deleteAllItems();
    }

    @Test
    public void testPrivateCheckedToggleTogglesSpinnerVisibility() {
        onView(withId(R.id.contactSpinner)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.privateCheck)).perform(click());
        onView(withId(R.id.contactSpinner)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.privateCheck)).perform(click());
        onView(withId(R.id.contactSpinner)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
    }

    @Test
    public void testTimeCheckedToggleTogglesRadioVisibility() {
        onView(withId(R.id.timeGroup)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.timeCheck)).perform(click());
        onView(withId(R.id.timeGroup)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.timeCheck)).perform(click());
        onView(withId(R.id.timeGroup)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
    }

    @Test
    public void testCanWriteInTextField() {
        onView(withId(R.id.createItemActivity_messageText)).perform(typeText(HELLO_ALICE));
        onView(withId(R.id.createItemActivity_messageText)).check(matches(withText(HELLO_ALICE)));
    }


}
