package ch.epfl.sweng.calamar.utils;


import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.SQLiteDatabaseHandler;
import ch.epfl.sweng.calamar.chat.ChatActivity;
import ch.epfl.sweng.calamar.chat.ChatAdapter;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.item.FileItem;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.recipient.User;

@RunWith(AndroidJUnit4.class)
public class StorageManagerTest extends ActivityInstrumentationTestCase2<ChatActivity> {

    private final StorageManager storageManager = StorageManager.getInstance();
    private final SQLiteDatabaseHandler dbHandler = SQLiteDatabaseHandler.getInstance();
    private final User testUser = new User(1, "Alice");
    private final User testRecipient = new User(2, "Bob");
    private Condition tc = Condition.trueCondition();
    private Condition fc = Condition.falseCondition();
    private Calendar calendar;

    private static final String ROOT_FOLDER_NAME = "Calamar/";
    private static final String IMAGE_FOLDER_NAME = ROOT_FOLDER_NAME + "Images/";
    private static final String FILE_FOLDER_NAME = ROOT_FOLDER_NAME + "Others/";
    private static final String FILENAME = "FILE_";
    private static final String IMAGENAME = "IMG_";
    private static final String NAME_SUFFIX = "_CAL";
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    @Rule
    public final ActivityTestRule<ChatActivity> mActivityRule = new ActivityTestRule<>(
            ChatActivity.class);

    private CalamarApplication app;

    public StorageManagerTest() {
        super(ChatActivity.class);
    }

    @Before
    public void setUp() {
        app = CalamarApplication.getInstance();
        app.resetPreferences();
        storageManager.deleteAllItemsWithDatabase();
        dbHandler.deleteAllRecipients();
    }

    @Test
    public void testFilesAreRenamed() throws IOException, InterruptedException {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                calendar = Calendar.getInstance();
                File f1 = null;
                File f2 = null;
                File f3 = null;
                try {
                    f1 = temp.newFile();
                    f2 = temp.newFile();
                    f3 = temp.newFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final File filePath = Environment.getExternalStoragePublicDirectory(FILE_FOLDER_NAME);
                final File imagePath = Environment.getExternalStoragePublicDirectory(IMAGE_FOLDER_NAME);
                final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), tc, null, f1.getAbsolutePath());
                final ImageItem item2 = new ImageItem(1, testUser, testRecipient, new Date(), tc, null, f2.getAbsolutePath());
                final FileItem item3 = new FileItem(2, testUser, testRecipient, new Date(), tc, null, f3.getAbsolutePath());
                final ListView list = (ListView) mActivityRule.getActivity().findViewById(R.id.messagesContainer);
                final ChatAdapter adapter = (ChatAdapter) list.getAdapter();
                adapter.add(item);
                adapter.add(item2);
                adapter.add(item3);
                final List<Item> retrievedFirst = mActivityRule.getActivity().getHistory();
                assertEquals(retrievedFirst.size(), 3);
                assertEquals(app.getTodayImageCount(), 0);
                assertEquals(app.getTodayFileCount(), 0);
                final ChatActivity activity = mActivityRule.getActivity();
                storageManager.storeItem(item, activity);
                assertEquals(app.getTodayFileCount(), 0);
                assertEquals(app.getTodayImageCount(), 1);
                storageManager.storeItem(item3, activity);
                assertEquals(app.getTodayFileCount(), 1);
                assertEquals(app.getTodayImageCount(), 1);
                storageManager.storeItem(item2, activity);
                assertEquals(app.getTodayFileCount(), 1);
                assertEquals(app.getTodayImageCount(), 2);
                storageManager.getCompleteItem(item, mActivityRule.getActivity());
                storageManager.getCompleteItem(item2, mActivityRule.getActivity());
                storageManager.getCompleteItem(item3, mActivityRule.getActivity());
                List<Item> retrieved = mActivityRule.getActivity().getHistory();
                assertEquals(retrieved.size(), 3);
                assertEquals(((ImageItem) retrieved.get(0)).getPath(), imagePath.toString() + '/' + IMAGENAME + formatDate() + NAME_SUFFIX + '0');
                assertEquals(((ImageItem) retrieved.get(1)).getPath(), imagePath.toString() + '/' + IMAGENAME + formatDate() + NAME_SUFFIX + '1');
                assertEquals(((FileItem) retrieved.get(2)).getPath(), filePath.toString() + '/' + FILENAME + formatDate() + NAME_SUFFIX + '0');
                assertFalse(retrieved.get(0).equals(retrievedFirst.get(0)));
                assertFalse(retrieved.get(1).equals(retrievedFirst.get(1)));
                assertFalse(retrieved.get(2).equals(retrievedFirst.get(2)));
            }
        });
    }

    @Test
    public void testDoesntWriteIfLocked() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                File f1 = null;
                File f2 = null;
                try {
                    f1 = temp.newFile();
                    f2 = temp.newFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), fc, null, f1.getAbsolutePath());
                final FileItem item2 = new FileItem(1, testUser, testRecipient, new Date(), fc, null, f2.getAbsolutePath());
                final ListView list = (ListView) mActivityRule.getActivity().findViewById(R.id.messagesContainer);
                final ChatAdapter adapter = (ChatAdapter) list.getAdapter();
                adapter.add(item);
                adapter.add(item2);
                final List<Item> retrievedFirst = mActivityRule.getActivity().getHistory();
                assertEquals(retrievedFirst.size(), 2);
                assertEquals(app.getTodayImageCount(), 0);
                assertEquals(app.getTodayFileCount(), 0);
                final ChatActivity activity = mActivityRule.getActivity();
                storageManager.storeItem(item, activity);
                assertEquals(app.getTodayFileCount(), 0);
                assertEquals(app.getTodayImageCount(), 0);
                storageManager.storeItem(item2, activity);
                assertEquals(app.getTodayFileCount(), 0);
                assertEquals(app.getTodayImageCount(), 0);
                storageManager.getCompleteItem(item, mActivityRule.getActivity());
                storageManager.getCompleteItem(item2, mActivityRule.getActivity());
                List<Item> retrieved = mActivityRule.getActivity().getHistory();
                assertEquals(retrieved.size(), 2);
                assertEquals(retrieved.get(0), retrievedFirst.get(0));
                assertEquals(retrieved.get(1), retrievedFirst.get(1));
            }
        });
    }

    @Test
    public void testDataIsRetrieved() {

    }

    @Test
    public void testOperationsAreRepercutedInDatabase() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                calendar = Calendar.getInstance();
                File f1 = null;
                File f2 = null;
                File f3 = null;
                try {
                    f1 = temp.newFile();
                    f2 = temp.newFile();
                    f3 = temp.newFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final File filePath = Environment.getExternalStoragePublicDirectory(FILE_FOLDER_NAME);
                final File imagePath = Environment.getExternalStoragePublicDirectory(IMAGE_FOLDER_NAME);
                final byte[] dummyData = {0x45, 0x44};
                final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), tc, null, f1.getAbsolutePath());
                final ImageItem item2 = new ImageItem(1, testUser, testRecipient, new Date(), tc, null, f2.getAbsolutePath());
                final FileItem item3 = new FileItem(2, testUser, testRecipient, new Date(), tc, dummyData, f3.getAbsolutePath());
                final FileItem item4 = new FileItem(3, testRecipient, testUser, new Date(), fc, null, "blablabla");
                final ChatActivity activity = mActivityRule.getActivity();
                storageManager.storeItem(item, activity);
                storageManager.storeItem(item2, activity);
                storageManager.storeItem(item3, activity);
                storageManager.storeItem(item4, activity);
                dbHandler.applyPendingOperations();
                List<Item> allItems = dbHandler.getAllItems();
                final ImageItem itemAfter = new ImageItem(item.getID(), item.getFrom(), item.getTo(),
                        item.getDate(), item.getCondition(), item.getData(), imagePath.toString() + '/' + IMAGENAME + formatDate() + NAME_SUFFIX + '0');
                final ImageItem item2After = new ImageItem(item2.getID(), item2.getFrom(), item2.getTo(),
                        item2.getDate(), item2.getCondition(), item2.getData(), imagePath.toString() + '/' + IMAGENAME + formatDate() + NAME_SUFFIX + '1');
                final FileItem item3After = new FileItem(item3.getID(), item3.getFrom(), item3.getTo(),
                        item3.getDate(), item3.getCondition(), null, filePath.toString() + '/' + FILENAME + formatDate() + NAME_SUFFIX + '0');
                final FileItem item4After = new FileItem(item4.getID(), item4.getFrom(), item4.getTo(),
                        item4.getDate(), item4.getCondition(), null, item4.getPath());
                assertEquals(allItems.size(), 4);
                assertEquals(allItems.get(0), itemAfter);
                assertEquals(allItems.get(1), item2After);
                assertEquals(allItems.get(2), item3After);
                assertEquals(allItems.get(3), item4After);
            }
        });
    }

    @Test
    public void testDoesntRenameOrStoreIfSent() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                File f1 = null;
                File f2 = null;
                try {
                    f1 = temp.newFile();
                    f2 = temp.newFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                app.setCurrentUserID(testUser.getID());
                app.setCurrentUserName(testUser.getName());
                byte[] data = {0x55, 0x43};
                final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), fc, null, f1.getAbsolutePath());
                final FileItem item2 = new FileItem(1, testUser, testRecipient, new Date(), fc, data, f2.getAbsolutePath());
                final ListView list = (ListView) mActivityRule.getActivity().findViewById(R.id.messagesContainer);
                final ChatAdapter adapter = (ChatAdapter) list.getAdapter();
                adapter.add(item);
                adapter.add(item2);
                final List<Item> retrievedFirst = mActivityRule.getActivity().getHistory();
                assertEquals(retrievedFirst.size(), 2);
                assertEquals(app.getTodayImageCount(), 0);
                assertEquals(app.getTodayFileCount(), 0);
                final ChatActivity activity = mActivityRule.getActivity();
                storageManager.storeItem(item, activity);
                assertEquals(app.getTodayFileCount(), 0);
                assertEquals(app.getTodayImageCount(), 0);
                storageManager.storeItem(item2, activity);
                //Checks if file still has data
                assertEquals(mActivityRule.getActivity().getHistory().get(1), retrievedFirst.get(1));
                assertEquals(app.getTodayFileCount(), 0);
                assertEquals(app.getTodayImageCount(), 0);
                storageManager.getCompleteItem(item, mActivityRule.getActivity());
                storageManager.getCompleteItem(item2, mActivityRule.getActivity());
                List<Item> retrieved = mActivityRule.getActivity().getHistory();
                assertEquals(retrieved.size(), 2);
                assertEquals(retrieved.get(0), retrievedFirst.get(0));
                assertEquals(retrieved.get(1), retrievedFirst.get(1));
            }
        });
    }

    private String formatDate() {
        return calendar.get(Calendar.DAY_OF_MONTH) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.YEAR);
    }
}
