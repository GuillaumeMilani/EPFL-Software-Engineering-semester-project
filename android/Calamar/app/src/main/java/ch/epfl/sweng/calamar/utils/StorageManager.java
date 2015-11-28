package ch.epfl.sweng.calamar.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.SQLiteDatabaseHandler;
import ch.epfl.sweng.calamar.item.FileItem;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.Item;

/**
 * A Singleton managing storing, retrieving and deleting items on local storage.
 */
//TODO Make toasts on UI thread, RuntimeException otherwise
public class StorageManager {

    private static final Set<WritingTask> currentWritingTasks = new HashSet<>();

    //Will be used to requery server if writing of a file has failed and the file is no longer available.
    private static final Set<Integer> currentFilesID = new HashSet<>();

    private static final int RETRY_TIME = 10000;
    private static final int MAX_ITER = 20;

    private static final String ROOT_FOLDER_NAME = "Calamar/";
    private static final String IMAGE_FOLDER_NAME = ROOT_FOLDER_NAME + "Images/";
    private static final String FILE_FOLDER_NAME = ROOT_FOLDER_NAME + "Others/";
    private static final String FILENAME = "FILE_";
    private static final String IMAGENAME = "IMG_";
    private static final String NAME_SUFFIX = "_CAL";

    private static StorageManager instance;
    private final SQLiteDatabaseHandler dbHandler;
    private final Handler handler;
    private final CalamarApplication app;
    private final Calendar calendar;

    /**
     * Returns the only instance of this class
     *
     * @return the singleton
     */
    public static StorageManager getInstance() {
        if (instance == null) {
            instance = new StorageManager();
        }
        return instance;
    }

    /**
     * Private constructor
     */
    private StorageManager() {
        app = CalamarApplication.getInstance();
        dbHandler = app.getDatabaseHandler();
        handler = new Handler();
        calendar = Calendar.getInstance();
    }

    /**
     * Stops all writing tasks (to free up memory mainly)
     */
    public void endWritingTasks(int level) {
        for (WritingTask w : currentWritingTasks) {
            if (w.getIteration() >= level) {
                w.cancel(true);
            }
        }
        currentWritingTasks.clear();
    }

    /**
     * Retry failed FileItem writings once more memory is available
     * TODO does nothing at the moment
     */
    public void retryFailedWriting() {
        //Ask server
    }

    /**
     * Adds the item to the database and possibly store it if it is a FileItem
     * Also updates the item with his new path
     *
     * @param i The item to be stored
     */
    public void storeItem(Item i, StorageCallbacks caller) {
        switch (i.getType()) {
            case SIMPLETEXTITEM:
                dbHandler.addItem(i);
                break;
            case IMAGEITEM:
                ImageItem image;
                if (!i.getFrom().equals(app.getCurrentUser())) {
                    if (i.getCondition().getValue()) {
                        ImageItem repathedImage = (ImageItem) rePath((ImageItem) i);
                        if (caller != null) {
                            caller.onItemRetrieved(repathedImage);
                        }
                        image = (ImageItem) Compresser.compressDataForDatabase(repathedImage);
                        app.increaseImageCount();
                        dbHandler.addItem(image);
                        storeFile((ImageItem) i);
                    } else {
                        dbHandler.addItem(i);
                    }
                } else {
                    dbHandler.addItem(Compresser.compressDataForDatabase((ImageItem) i));
                }
                break;
            case FILEITEM:
                FileItem file;
                if (!i.getFrom().equals(app.getCurrentUser())) {
                    if (i.getCondition().getValue()) {
                        FileItem repathedFile = rePath((FileItem) i);
                        if (caller != null) {
                            caller.onItemRetrieved(repathedFile);
                        }
                        file = Compresser.compressDataForDatabase(repathedFile);
                        app.increaseFileCount();
                        dbHandler.addItem(file);
                        storeFile((FileItem) i);
                    } else {
                        dbHandler.addItem(i);
                    }
                } else {
                    dbHandler.addItem(Compresser.compressDataForDatabase((FileItem) i));
                }
                break;
            default:
                throw new IllegalArgumentException(app.getString(R.string.unknown_item_type));
        }

    }

    /**
     * Adds the items to the database and possibly store them
     *
     * @param items The items to be stored
     */
    public void storeItems(List<Item> items, StorageCallbacks caller) {
        for (Item i : items) {
            storeItem(i, caller);
        }
    }

    /**
     * Deletes the item given as argument (deletes also in the database)
     *
     * @param item The item to delete
     */
    public void deleteItemWithDatabase(Item item) {
        switch (item.getType()) {
            case SIMPLETEXTITEM:
                dbHandler.deleteItem(item);
                break;
            case FILEITEM:
                new DeleteTask((FileItem) item).execute();
                dbHandler.deleteItem(item);
                break;
            case IMAGEITEM:
                new DeleteTask((ImageItem) item).execute();
                dbHandler.deleteItem(item);
                break;
            default:
                throw new IllegalArgumentException(app.getString(R.string.unknown_item_type));
        }
    }

    /**
     * Deletes an item without deleting it in the database
     *
     * @param item the item to delete
     */
    public void deleteItemWithoutDatabase(Item item) {
        switch (item.getType()) {
            case SIMPLETEXTITEM:
                break;
            case FILEITEM:
                new DeleteTask((FileItem) item).execute();
                break;
            case IMAGEITEM:
                new DeleteTask((ImageItem) item).execute();
                break;
            default:
                throw new IllegalArgumentException(app.getString(R.string.unknown_item_type));
        }
    }

    /**
     * Deletes the item whose ID is given (deletes also in the database)
     *
     * @param ID the id of the item to delete
     */
    public void deleteItemWithDatabase(int ID) {
        Item item = dbHandler.getItem(ID);
        deleteItemWithDatabase(item);
    }

    /**
     * Deletes the item whose ID is given, without deleting it in the database
     *
     * @param ID The id of the item to delete
     */
    public void deleteItemWithoutDatabase(int ID) {
        Item item = dbHandler.getItem(ID);
        deleteItemWithoutDatabase(item);
    }

    /**
     * Deletes all stored items (even in database)
     */
    public void deleteAllItemsWithDatabase() {
        List<Item> items = dbHandler.getAllItems();
        for (Item i : items) {
            deleteItemWithDatabase(i);
        }
        dbHandler.deleteAllItems();
    }

    /**
     * Deletes all stored items (without deleting anything in database)
     */
    public void deleteAllItemsWithoutDatabase() {
        List<Item> items = dbHandler.getAllItems();
        for (Item i : items) {
            deleteItemWithoutDatabase(i);
        }
    }

    /**
     * Deletes all items whose id is in the list (deletes also in the database)
     *
     * @param ids the ids of the items to delete
     */
    public void deleteItemsForIdsWithDatabase(List<Integer> ids) {
        List<Item> toDelete = dbHandler.getItems(ids);
        deleteItemsWithDatabase(toDelete);
    }

    /**
     * Deletes all items given in the list (deletes also in the database)
     *
     * @param items the items to delete
     */
    public void deleteItemsWithDatabase(List<Item> items) {
        for (Item i : items) {
            deleteItemWithDatabase(i);
        }
    }

    /**
     * Deletes all items whose id is in the list without deleting them in the database
     *
     * @param ids the ids of the items to delete
     */
    public void deleteItemsForIdsWithoutDatabase(List<Integer> ids) {
        List<Item> toDelete = dbHandler.getItems(ids);
        deleteItemsWithoutDatabase(toDelete);
    }

    /**
     * Deletes all items given in the list without deleting them in the database
     *
     * @param items the items to delete
     */
    public void deleteItemsWithoutDatabase(List<Item> items) {
        for (Item i : items) {
            deleteItemWithoutDatabase(i);
        }
    }

    /**
     * Task which deletes the FileItem given to the constructor. Tries only once.
     */
    private class DeleteTask extends AsyncTask<Void, Void, Void> {

        private final FileItem f;

        public DeleteTask(FileItem f) {
            this.f = f;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (isExternalStorageWritable()) {
                File file = new File(f.getPath());
                if (file.exists()) {
                    if (!file.delete()) {
                        //Toast.makeText(CalamarApplication.getInstance(), R.string.error_file_deletion, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                showStorageStateToast();
            }
            return null;
        }
    }

    /**
     * Returns the complete Item (with complete data) for a given Item
     *
     * @param i      The item
     * @param caller The Activity who called this method
     */
    public void getCompleteItem(Item i, StorageCallbacks caller) {
        if (i != null) {
            switch (i.getType()) {
                case SIMPLETEXTITEM:
                    caller.onItemRetrieved(i);
                case FILEITEM:
                    new ReadTask((FileItem) i, ((FileItem) i).getPath(), caller).execute();
                    break;
                case IMAGEITEM:
                    new ReadTask((ImageItem) i, ((ImageItem) i).getPath(), caller).execute();
                    break;
                default:
                    throw new IllegalArgumentException(app.getString(R.string.unknown_item_type));
            }
        }
    }

    /**
     * Returns the complete Item (with complete Data) for a given ID
     *
     * @param ID     The id of the item
     * @param caller The Activity who called this method
     */
    public void getCompleteItem(int ID, StorageCallbacks caller) {
        Item i = dbHandler.getItem(ID);
        getCompleteItem(i, caller);
    }

    /**
     * Returns the data of the file given by the path
     *
     * @param path   The path of the file
     * @param caller The Activity who called this method
     */
    public void getData(String path, StorageCallbacks caller) {
        new ReadTask(null, path, caller);
    }

    /**
     * Called when ReadTask has finished
     *
     * @param i      The FileItem (if null, callbacks OnDataRetrieved, else OnItemRetrieved)
     * @param data   The data retrieved
     * @param caller The Activity who asked for reading
     */
    private void onDataRetrieved(FileItem i, byte[] data, StorageCallbacks caller) {
        if (i == null) {
            caller.onDataRetrieved(data);
        } else {
            switch (i.getType()) {
                case FILEITEM:
                    caller.onItemRetrieved(new FileItem(i.getID(), i.getFrom(), i.getTo(), i.getDate(), i.getCondition(), data, i.getPath()));
                    break;
                case IMAGEITEM:
                    caller.onItemRetrieved(new ImageItem(i.getID(), i.getFrom(), i.getTo(), i.getDate(), i.getCondition(), data, i.getPath()));
                    break;
                default:
                    throw new IllegalArgumentException(app.getString(R.string.expected_fileitem));
            }
        }
    }

    /**
     * The task whose job is to retrieve data from storage and return it
     */
    protected class ReadTask extends AsyncTask<Void, Void, byte[]> {

        private final FileItem f;
        private final String path;
        private final StorageCallbacks caller;

        public ReadTask(FileItem f, String path, StorageCallbacks caller) {
            this.f = f;
            this.path = path;
            this.caller = caller;
        }

        @Override
        protected byte[] doInBackground(Void... params) {
            return getData(path);
        }

        @Override
        protected void onPostExecute(byte[] data) {
            onDataRetrieved(f, data, caller);
        }
    }

    /**
     * Returns a byte array representing the data of the FileItem
     *
     * @param f the FileItem whose data needs to be retrieved from storage
     * @return The data
     */
    private byte[] getData(FileItem f) {
        switch (f.getType()) {
            case FILEITEM:
                return getData(f.getPath());
            case IMAGEITEM:
                return getData(f.getPath());
            default:
                throw new IllegalArgumentException(app.getString(R.string.expected_fileitem));
        }
    }


    private byte[] getData(String path) {
        if (isExternalStorageReadable()) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    return FileUtils.toByteArray(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //showStorageStateToast();
        }
        return null;
    }

    /**
     * Store a FileItem in storage
     *
     * @param f the FileItem to store
     */
    private void storeFile(FileItem f) {
        if (f.getData().length != 0) {
            WritingTask task = new WritingTask(f, 0);
            currentWritingTasks.add(task);
            currentFilesID.add(f.getID());
            task.execute();
        }
    }

    /**
     * Shows different error toast depending on the state of the storage
     */
    private void showStorageStateToast() {
        switch (Environment.getExternalStorageState()) {
            case Environment.MEDIA_UNMOUNTED:
                Toast.makeText(app, R.string.error_media_unmounted, Toast.LENGTH_LONG).show();
            case Environment.MEDIA_SHARED:
                Toast.makeText(app, R.string.error_media_shared, Toast.LENGTH_LONG).show();
                break;
            case Environment.MEDIA_UNMOUNTABLE:
                Toast.makeText(app, R.string.error_media_unmountable, Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(app, R.string.error_media_generic, Toast.LENGTH_LONG).show();
                break;
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Writes a FileItem in the local storage
     *
     * @param f    The FileItem to store
     * @param path Where it will be stored
     * @throws IOException If there is a problem writing it
     */
    private void writeFile(FileItem f, File path) throws IOException {
        if (f.getData() != null) {
            OutputStream stream = null;
            try {
                stream = new BufferedOutputStream(new FileOutputStream(path));
                stream.write(Compresser.decompress(f.getData()));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
    }

    /**
     * Rename and changes the path of a FileItem which will be stored.
     *
     * @param f The FileItem to "repath"
     * @return The repathed FileItem
     */
    private FileItem rePath(FileItem f) {
        switch (f.getType()) {
            case FILEITEM:
                File filePath = Environment.getExternalStoragePublicDirectory(FILE_FOLDER_NAME);
                return new FileItem(f.getID(), f.getFrom(), f.getTo(), f.getDate(), f.getCondition(), f.getData(), filePath.getAbsolutePath() + '/' + FILENAME + "" + formatDate() + NAME_SUFFIX + app.getTodayFileCount());
            case IMAGEITEM:
                File imagePath = Environment.getExternalStoragePublicDirectory(IMAGE_FOLDER_NAME);
                return new ImageItem(f.getID(), f.getFrom(), f.getTo(), f.getDate(), f.getCondition(), f.getData(), imagePath.getAbsolutePath() + '/' + IMAGENAME + "" + formatDate() + NAME_SUFFIX + app.getTodayImageCount());
            default:
                throw new IllegalArgumentException(app.getString(R.string.expected_fileitem));
        }
    }

    /**
     * Creates a String using the current Date
     *
     * @return the String, used in rePath
     */
    private String formatDate() {
        return calendar.get(Calendar.DAY_OF_MONTH) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.YEAR);
    }

    /**
     * An AsyncTask whose task is to write a FileItem to the storage. It will retry for ~30 min (20 times) before giving up
     */
    protected class WritingTask extends AsyncTask<Void, Void, Boolean> {

        private int iterCount;
        private FileItem f;

        private WritingTask(FileItem f, int iterCount) {
            this.iterCount = iterCount;
            this.f = f;
        }

        public int getIteration() {
            return iterCount;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (isExternalStorageWritable() && !isCancelled()) {
                switch (f.getType()) {
                    case FILEITEM:
                        File filePath = Environment.getExternalStoragePublicDirectory(FILE_FOLDER_NAME);
                        if (!filePath.exists()) {
                            if (filePath.mkdirs()) {
                                try {
                                    writeFile(f, filePath);
                                    return true;
                                } catch (IOException e) {
                                    //Toast.makeText(app, app.getString(R.string.error_file_creation, f.getName()), Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            } else {
                                //Toast.makeText(app, R.string.error_directory_creation, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                        try {
                            writeFile(f, filePath);
                            return true;
                        } catch (IOException e) {
                            //Toast.makeText(app, app.getString(R.string.error_file_creation, f.getName()), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    case IMAGEITEM:
                        File imagePath = Environment.getExternalStoragePublicDirectory(IMAGE_FOLDER_NAME);
                        if (!imagePath.exists()) {
                            if (imagePath.mkdirs()) {
                                try {
                                    writeFile(f, imagePath);
                                    return true;
                                } catch (IOException e) {
                                    //Toast.makeText(app, app.getString(R.string.error_image_creation, f.getName()), Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            } else {
                                //Toast.makeText(app, R.string.error_directory_creation, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        } else {
                            try {
                                writeFile(f, imagePath);
                                return true;
                            } catch (IOException e) {
                                //Toast.makeText(app, app.getString(R.string.error_image_creation, f.getName()), Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }

                    default:
                        throw new IllegalArgumentException(app.getString(R.string.expected_fileitem));
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean b) {
            if (!b) {
                if (!isCancelled()) {
                    if (iterCount < MAX_ITER) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new WritingTask(f, iterCount + 1).execute();
                            }
                        }, RETRY_TIME * (iterCount + 1));
                    } else {
                        showStorageStateToast();
                    }
                } else {
                    currentWritingTasks.remove(this);
                }
            } else {
                currentFilesID.remove(f.getID());
                currentWritingTasks.remove(this);
            }
        }

        @Override
        protected void onCancelled() {
            currentWritingTasks.remove(this);
        }
    }
}
