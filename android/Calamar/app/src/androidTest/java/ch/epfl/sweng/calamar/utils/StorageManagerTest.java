package ch.epfl.sweng.calamar.utils;


import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void testWritingTaskLoopsIfRequiredAndCancelWorks() throws Throwable {
        byte[] data = {0x33};
        final FileItem item = new FileItem(0, testUser, testRecipient, new Date(), tc, data, "WriteThis");
        CountDownLatch latch = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item, null);
            }
        });
        assertEquals(storageManager.getCurrentWritingTasks().size(), 1);
        assertEquals(storageManager.getCurrentFilesID().size(), 1);
        assertTrue(storageManager.getCurrentFilesID().contains(item.getID()));
        storageManager.cancelWritingTasks(0);
        latch.await(5, TimeUnit.SECONDS);
        assertEquals(storageManager.getCurrentWritingTasks().size(), 0);
        assertEquals(storageManager.getCurrentFilesID().size(), 1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item, null);
            }
        });
        StorageManager.WritingTask wt = new ArrayList<>(storageManager.getCurrentWritingTasks()).get(0);
        while (wt.getIteration() < 2) {
            storageManager.cancelWritingTasks(2);
            assertFalse(wt.isCancelled());
            assertEquals(storageManager.getCurrentFilesID().size(), 1);
            List<StorageManager.WritingTask> tasks = new ArrayList<>(storageManager.getCurrentWritingTasks());
            if (tasks.isEmpty()) {
                assertEquals(wt.getIteration(), 1);
                assertEquals(wt.getStatus(), AsyncTask.Status.FINISHED);
                break;
            } else {
                wt = new ArrayList<>(storageManager.getCurrentWritingTasks()).get(0);
            }
        }
        storageManager.cancelWritingTasks(2);
        latch.await(5, TimeUnit.SECONDS);
        assertEquals(storageManager.getCurrentFilesID().size(), 1);
        assertEquals(storageManager.getCurrentWritingTasks().size(), 0);
    }

    @Test
    public void testDeleteTask() throws Throwable {
        File f1 = temp.newFile();
        byte[] data = {0x33};
        writeFile(f1, data);
        final FileItem item = new FileItem(0, testUser, testRecipient, new Date(), tc, null, f1.getAbsolutePath());
        assertTrue(Arrays.equals(FileUtils.toByteArray(f1), data));
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.deleteItemWithoutDatabase(item);
            }
        });
        synchronized (this) {
            wait(1000);
        }
        assertFalse(f1.exists());
    }


    @Test
    public void testFilesAreRenamed() throws Throwable {
        calendar = Calendar.getInstance();
        File f1 = null;
        File f2 = null;
        File f3 = null;
        f1 = temp.newFile();
        f2 = temp.newFile();
        f3 = temp.newFile();
        final File filePath = Environment.getExternalStoragePublicDirectory(FILE_FOLDER_NAME);
        final File imagePath = Environment.getExternalStoragePublicDirectory(IMAGE_FOLDER_NAME);
        final ChatActivity activity = mActivityRule.getActivity();
        final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), tc, null, f1.getAbsolutePath());
        final ImageItem item2 = new ImageItem(1, testUser, testRecipient, new Date(), tc, null, f2.getAbsolutePath());
        final FileItem item3 = new FileItem(2, testUser, testRecipient, new Date(), tc, null, f3.getAbsolutePath());
        final ListView list = (ListView) activity.findViewById(R.id.messagesContainer);
        final ChatAdapter adapter = (ChatAdapter) list.getAdapter();
        adapter.add(item);
        adapter.add(item2);
        adapter.add(item3);
        final List<Item> retrievedFirst = activity.getHistory();
        assertEquals(retrievedFirst.size(), 3);
        assertEquals(app.getTodayImageCount(), 0);
        assertEquals(app.getTodayFileCount(), 0);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item, activity);
            }
        });
        assertEquals(app.getTodayFileCount(), 0);
        assertEquals(app.getTodayImageCount(), 1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item3, activity);
            }
        });
        assertEquals(app.getTodayFileCount(), 1);
        assertEquals(app.getTodayImageCount(), 1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item2, activity);
            }
        });
        assertEquals(app.getTodayFileCount(), 1);
        assertEquals(app.getTodayImageCount(), 2);
        List<Item> retrieved = activity.getHistory();
        assertEquals(retrieved.size(), 3);
        assertEquals(((ImageItem) retrieved.get(0)).getPath(), imagePath.toString() + '/' + IMAGENAME + formatDate() + NAME_SUFFIX + '0');
        assertEquals(((ImageItem) retrieved.get(1)).getPath(), imagePath.toString() + '/' + IMAGENAME + formatDate() + NAME_SUFFIX + '1');
        assertEquals(((FileItem) retrieved.get(2)).getPath(), filePath.toString() + '/' + FILENAME + formatDate() + NAME_SUFFIX + '0');
        assertFalse(retrieved.get(0).equals(retrievedFirst.get(0)));
        assertFalse(retrieved.get(1).equals(retrievedFirst.get(1)));
        assertFalse(retrieved.get(2).equals(retrievedFirst.get(2)));
    }

    @Test
    public void testDoesntWriteIfLocked() throws Throwable {
        File f1 = null;
        File f2 = null;
        try {
            f1 = temp.newFile();
            f2 = temp.newFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final ChatActivity activity = mActivityRule.getActivity();
        final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), fc, null, f1.getAbsolutePath());
        final FileItem item2 = new FileItem(1, testUser, testRecipient, new Date(), fc, null, f2.getAbsolutePath());
        final ListView list = (ListView) activity.findViewById(R.id.messagesContainer);
        final ChatAdapter adapter = (ChatAdapter) list.getAdapter();
        adapter.add(item);
        adapter.add(item2);
        final List<Item> retrievedFirst = activity.getHistory();
        assertEquals(retrievedFirst.size(), 2);
        assertEquals(app.getTodayImageCount(), 0);
        assertEquals(app.getTodayFileCount(), 0);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item, activity);
            }
        });
        assertEquals(app.getTodayFileCount(), 0);
        assertEquals(app.getTodayImageCount(), 0);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item2, activity);
            }
        });
        assertEquals(app.getTodayFileCount(), 0);
        assertEquals(app.getTodayImageCount(), 0);
        List<Item> retrieved = activity.getHistory();
        assertEquals(retrieved.size(), 2);
        assertEquals(retrieved.get(0), retrievedFirst.get(0));
        assertEquals(retrieved.get(1), retrievedFirst.get(1));
    }

    @Test
    public void testDataIsRetrieved() throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
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
        final ChatActivity activity = mActivityRule.getActivity();
        byte[] data = {0x55, 0x43};
        byte[] compressedData = Compresser.compress(data);
        writeFile(f1, data);
        writeFile(f2, data);
        final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), tc, data, f1.getAbsolutePath());
        final FileItem item2 = new FileItem(1, testUser, testRecipient, new Date(), tc, data, f2.getAbsolutePath());
        final ArrayList<Item> toAdd = new ArrayList<>(2);
        toAdd.add(item);
        toAdd.add(item2);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItems(toAdd, activity);
            }
        });
        final ImageItem itemEmpty = new ImageItem(item.getID(), item.getFrom(), item.getTo(), item.getDate(), item.getCondition(), null, item.getPath());
        final FileItem item2Empty = new FileItem(item2.getID(), item2.getFrom(), item2.getTo(), item2.getDate(), item2.getCondition(), null, item2.getPath());
        final ListView list = (ListView) activity.findViewById(R.id.messagesContainer);
        final ChatAdapter adapter = (ChatAdapter) list.getAdapter();
        adapter.add(itemEmpty);
        adapter.add(item2Empty);
        final List<Item> retrievedFirst = activity.getHistory();
        assertEquals(retrievedFirst.size(), 2);
        assertEquals(app.getTodayImageCount(), 0);
        assertEquals(app.getTodayFileCount(), 0);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.new ReadTask(itemEmpty, itemEmpty.getPath(), activity) {
                    @Override
                    protected void onPostExecute(byte[] data) {
                        super.onPostExecute(data);
                        latch.countDown();
                    }
                }.execute();
            }
        });
        latch.await(10, TimeUnit.SECONDS);
        final CountDownLatch latch2 = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.new ReadTask(item2Empty, item2Empty.getPath(), activity) {
                    @Override
                    protected void onPostExecute(byte[] data) {
                        super.onPostExecute(data);
                        latch2.countDown();
                    }
                }.execute();
            }
        });
        latch2.await(10, TimeUnit.SECONDS);
        List<Item> retrieved = activity.getHistory();
        assertEquals(retrieved.size(), 2);
        assertTrue(Arrays.toString(((FileItem) retrieved.get(0)).getData()), Arrays.equals(((ImageItem) retrieved.get(0)).getData(), compressedData));
        assertTrue(Arrays.equals(((FileItem) retrieved.get(1)).getData(), compressedData));
        assertFalse(retrieved.get(0).equals(retrievedFirst.get(0)));
        assertFalse(retrieved.get(1).equals(retrievedFirst.get(1)));

    }


    @Test
    public void testOperationsAreRepercutedInDatabase() throws Throwable {
        calendar = Calendar.getInstance();
        File f1 = null;
        File f2 = null;
        File f3 = null;
        f1 = temp.newFile();
        f2 = temp.newFile();
        f3 = temp.newFile();
        final File filePath = Environment.getExternalStoragePublicDirectory(FILE_FOLDER_NAME);
        final File imagePath = Environment.getExternalStoragePublicDirectory(IMAGE_FOLDER_NAME);
        final byte[] dummyData = {0x45, 0x44};
        Bitmap bitmap = getBitmapFromAsset("testImage.jpg");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();
        final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), tc, bitmapData, f1.getAbsolutePath());
        final ImageItem item2 = new ImageItem(1, testUser, testRecipient, new Date(), tc, null, f2.getAbsolutePath());
        final FileItem item3 = new FileItem(2, testUser, testRecipient, new Date(), tc, dummyData, f3.getAbsolutePath());
        final FileItem item4 = new FileItem(3, testRecipient, testUser, new Date(), fc, null, "blablabla");
        final ChatActivity activity = mActivityRule.getActivity();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item, activity);
                storageManager.storeItem(item2, activity);
                storageManager.storeItem(item3, activity);
                storageManager.storeItem(item4, activity);
            }
        });
        List<Item> allItems = dbHandler.getAllItems();
        final ImageItem itemAfter = new ImageItem(item.getID(), item.getFrom(), item.getTo(),
                item.getDate(), item.getCondition(), Compresser.getImageThumbnail(item), imagePath.toString() + '/' + IMAGENAME + formatDate() + NAME_SUFFIX + '0');
        final ImageItem item2After = new ImageItem(item2.getID(), item2.getFrom(), item2.getTo(),
                item2.getDate(), item2.getCondition(), null, imagePath.toString() + '/' + IMAGENAME + formatDate() + NAME_SUFFIX + '1');
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

    @Test
    public void testDoesntRenameOrStoreIfSent() throws Throwable {
        File f1 = null;
        File f2 = null;
        f1 = temp.newFile();
        f2 = temp.newFile();
        app.setCurrentUserID(testUser.getID());
        app.setCurrentUserName(testUser.getName());
        byte[] data = {0x55, 0x43};
        final ChatActivity activity = mActivityRule.getActivity();
        final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), tc, null, f1.getAbsolutePath());
        final FileItem item2 = new FileItem(1, testUser, testRecipient, new Date(), tc, data, f2.getAbsolutePath());
        final ListView list = (ListView) activity.findViewById(R.id.messagesContainer);
        final ChatAdapter adapter = (ChatAdapter) list.getAdapter();
        adapter.add(item);
        adapter.add(item2);
        final List<Item> retrievedFirst = activity.getHistory();
        assertEquals(retrievedFirst.size(), 2);
        assertEquals(app.getTodayImageCount(), 0);
        assertEquals(app.getTodayFileCount(), 0);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item, activity);
            }
        });
        assertEquals(app.getTodayFileCount(), 0);
        assertEquals(app.getTodayImageCount(), 0);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item2, activity);
            }
        });
        //Checks if file still has data
        assertEquals(activity.getHistory().get(1), retrievedFirst.get(1));
        assertEquals(app.getTodayFileCount(), 0);
        assertEquals(app.getTodayImageCount(), 0);
        List<Item> retrieved = activity.getHistory();
        assertEquals(retrieved.size(), 2);
        assertEquals(retrieved.get(0), retrievedFirst.get(0));
        assertEquals(retrieved.get(1), retrievedFirst.get(1));
    }

    @Test
    public void testStoresGetsAndDeletes() throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
        File f1 = null;
        File f2 = null;
        f1 = temp.newFile();
        f2 = temp.newFile();
        final ChatActivity activity = mActivityRule.getActivity();
        final ChatAdapter adapter = (ChatAdapter) ((ListView) activity.findViewById(R.id.messagesContainer)).getAdapter();
        byte[] data = {0x66, 0x34};
        writeFile(f1, data);
        writeFile(f2, data);
        app.setCurrentUserName(testUser.getName());
        app.setCurrentUserID(testUser.getID());
        final ImageItem item = new ImageItem(0, testUser, testRecipient, new Date(), tc, null, f1.getAbsolutePath());
        final FileItem item2 = new FileItem(1, testUser, testRecipient, new Date(), tc, null, f2.getAbsolutePath());
        final ImageItem itemFull = new ImageItem(item.getID(), item.getFrom(), item.getTo(), item.getDate(), item.getCondition(), data, item.getPath());
        final FileItem item2Full = new FileItem(item2.getID(), item2.getFrom(), item2.getTo(), item2.getDate(), item2.getCondition(), data, item2.getPath());
        adapter.add(item);
        assertEquals(activity.getHistory().get(0), item);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(item, activity);
            }
        });
        assertEquals(activity.getHistory().get(0), item);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.new ReadTask(item, item.getPath(), activity) {
                    @Override
                    protected void onPostExecute(byte[] data) {
                        super.onPostExecute(data);
                        latch.countDown();
                    }
                }.execute();
            }
        });
        latch.await(20, TimeUnit.SECONDS);
        assertEquals(activity.getHistory().get(0), itemFull);
        adapter.update(item);
        storageManager.deleteItemWithoutDatabase(itemFull);
        assertEquals(dbHandler.getItem(item.getID()), item);
        final CountDownLatch latch2 = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.new ReadTask(item, item.getPath(), activity) {
                    @Override
                    protected void onPostExecute(byte[] data) {
                        super.onPostExecute(data);
                        latch2.countDown();
                    }
                }.execute();
            }
        });
        latch2.await(10, TimeUnit.SECONDS);
        assertEquals(activity.getHistory().get(0), item);
        final List<Item> toAdd = new ArrayList<>();
        toAdd.add(itemFull);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItems(toAdd, activity);
            }
        });
        assertEquals(activity.getHistory().get(0), item);
        writeFile(f1, data);
        final CountDownLatch latch3 = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.new ReadTask(item, item.getPath(), activity) {
                    @Override
                    protected void onPostExecute(byte[] data) {
                        super.onPostExecute(data);
                        latch3.countDown();
                    }
                }.execute();
            }
        });
        latch3.await(10, TimeUnit.SECONDS);
        assertEquals(activity.getHistory().get(0), itemFull);
        storageManager.deleteItemWithDatabase(item.getID());
        assertEquals(dbHandler.getItem(item.getID()), null);
        adapter.update(item);
        final CountDownLatch latch4 = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.new ReadTask(item, item.getPath(), activity) {
                    @Override
                    protected void onPostExecute(byte[] data) {
                        super.onPostExecute(data);
                        latch4.countDown();
                    }
                }.execute();
            }
        });
        latch4.await(10, TimeUnit.SECONDS);
        assertEquals(activity.getHistory().get(0), item);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.storeItem(itemFull, activity);
            }
        });
        writeFile(f1, data);
        final CountDownLatch latch5 = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                storageManager.new ReadTask(item, item.getPath(), activity) {
                    @Override
                    protected void onPostExecute(byte[] data) {
                        super.onPostExecute(data);
                        latch5.countDown();
                    }
                }.execute();
            }
        });
        latch5.await(10, TimeUnit.SECONDS);
        assertEquals(activity.getHistory().get(0), itemFull);
    }


    private String formatDate() {
        return calendar.get(Calendar.DAY_OF_MONTH) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.YEAR);
    }

    private void writeFile(File f, byte[] data) throws IOException {
        if (data != null) {
            OutputStream stream = null;
            try {
                stream = new BufferedOutputStream(new FileOutputStream(f.getAbsolutePath()));
                stream.write(data);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
    }

    private Bitmap getBitmapFromAsset(String filePath) {
        AssetManager assetManager = getInstrumentation().getTargetContext().getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

}
