package ch.epfl.sweng.calamar;


import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Spinner;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.epfl.sweng.calamar.client.ConstantDatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.item.CreateItemActivity;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(JUnit4.class)
public class CreateItemActivityTest extends ActivityInstrumentationTestCase2<CreateItemActivity> {

    private final static String HELLO_ALICE = "Hello Alice !";
    private final static String ALICE = "Alice";

    private final Recipient BOB = new User(1, "bob");

    private CalamarApplication app;

    @Rule
    public final ActivityTestRule<CreateItemActivity> mActivityRule = new ActivityTestRule<>(
            CreateItemActivity.class);

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    public CreateItemActivityTest() {
        super(CreateItemActivity.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        DatabaseClientLocator.setDatabaseClient(new ConstantDatabaseClient());
        app = CalamarApplication.getInstance();
        app.getDatabaseHandler().deleteAllItems();
        mActivityRule.getActivity();
    }

    @Test
    public void titleOfActivityIsShown() {
        onView(withId(R.id.activityTitle)).check(matches(withText("Create a new item")));
    }

    @Test
    public void testProgressBarIsInvisibleAtStart() {
        onView(withId(R.id.locationProgressBar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.sendProgressBar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
    }

    @Test
    public void testBrowseAFileDisplayed() {
        onView(withId(R.id.selectFileText)).check(matches(withText("File :")));
        onView(withId(R.id.selectFileButton)).check(matches(withText("Browse…")));
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
    public void testImpossibleToCreateEmptyItem() {
        onView(withId(R.id.createButton)).perform(click());
        onView(withText("An item must either have a text, a file, or both.")).check(matches(ViewMatchers.isDisplayed()));
        onView(withText("OK")).perform(click());
    }

    @Test
    public void testCantSendPublicItemWithoutLocation() throws DatabaseClientException {
        onView(withId(R.id.createItemActivity_messageText)).perform(typeText(HELLO_ALICE));
        closeSoftKeyboard();
        onView(withId(R.id.createButton)).perform(click());
        onView(withText(CalamarApplication.getInstance().getString(R.string.public_without_condition))).check(matches(ViewMatchers.isDisplayed()));
    }

    /*
    @Test
    public void testTimeCheckedToggleTogglesRadioVisibility() {
        onView(withId(R.id.timeGroup)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        onView(withId(R.id.timeCheck)).perform(click());
        onView(withId(R.id.timeGroup)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.timeCheck)).perform(click());
        onView(withId(R.id.timeGroup)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
    } */

    @Test
    public void testCanWriteInTextField() {
        onView(withId(R.id.createItemActivity_messageText)).perform(typeText(HELLO_ALICE));
        onView(withId(R.id.createItemActivity_messageText)).check(matches(withText(HELLO_ALICE)));
    }

    @Ignore //Causes problems, hangs in FileChooserActivity
    public void testFilePickerIsLaunched() {
        onView(withId(R.id.createItemActivity_messageText)).check(matches(withText("")));
        onView(withId(R.id.selectFileButton)).perform(click());
        /*No activity on stage Resumed
        onView(withId(R.id.createItemActivity_messageText)).check(doesNotExist());
        onView(withId(R.id.selectFileButton)).check(doesNotExist());
        onView(withId(R.id.contactSpinner)).check(doesNotExist());*/
    }

    @Test
    public void testOnActivityResult() throws Throwable {
        final Intent goodIntent = new Intent();
        File file = new File(folder.getRoot().getAbsolutePath() + "/IMG1.png");
        Bitmap bitmap = getBitmapFromAsset("testImage.jpg");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        out.write(bitmapData);
        goodIntent.setData(Uri.fromFile(file));
        mActivityRule.getActivity().onActivityResult(1, Activity.RESULT_CANCELED, null); //Does nothing
        onView(withId(R.id.selectFileButton)).check(matches(withText("Browse…")));
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivityRule.getActivity().onActivityResult(1, Activity.RESULT_OK, goodIntent);
            }
        });
        onView(withId(R.id.selectFileButton)).check(matches(withText("IMG1.png")));
    }

    @Ignore //Works, but activity is killed, so espresso is not happy
    public void testSendFileItem() throws Throwable {
        testOnActivityResult();
        final Spinner contactsSpinner = (Spinner) mActivityRule.getActivity().findViewById(R.id.contactSpinner);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactsSpinner.setSelection(1);
            }
        });
        mActivityRule.getActivity();
        onView(withId(R.id.privateCheck)).perform(click());
        onView(withId(R.id.createButton)).perform(click());
        onView(withText("Item sent")).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.createButton)).check(doesNotExist());
    }

    private Bitmap getBitmapFromAsset(String filePath) throws IOException {
        AssetManager assetManager = CalamarApplication.getInstance().getAssets();
        InputStream istr;
        istr = assetManager.open(filePath);
        return BitmapFactory.decodeStream(istr);
    }


}
