package ch.epfl.sweng.calamar.utils;

import android.app.AlertDialog;
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
import ch.epfl.sweng.calamar.SQLiteDatabaseHandler;
import ch.epfl.sweng.calamar.chat.ChatAdapter;
import ch.epfl.sweng.calamar.item.FileItem;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.Item;

/**
 * A Singleton managing storing and retrieving items on local storage.
 */
public class StorageManager {

    private static final Set<WritingTask> currentWritingTasks = new HashSet<>();

    //Will be used to requery server if writing of a file has failed and the file is no longer available.
    private static final Set<Integer> currentFilesID = new HashSet<>();

    private static final int RETRY_TIME = 10000;
    private static final int MAX_ITER = 20;

    private static final String ROOT_FOLDER_NAME = "Calamar";
    private static final String IMAGE_FOLDER_NAME = ROOT_FOLDER_NAME + "/Images";
    private static final String FILE_FOLDER_NAME = ROOT_FOLDER_NAME + "/Others";
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
     */
    public void retryFailedWriting() {
        //Ask server
    }

    /**
     * Adds the item to the database and possibly store it if it is a FileItem
     *
     * @param i The item to be stored
     */
    public void storeItem(Item i) {
        switch (i.getType()) {
            case SIMPLETEXTITEM:
                dbHandler.addItem(i);
                break;
            case IMAGEITEM:
                ImageItem image;
                if (!i.getFrom().equals(app.getCurrentUser())) {
                    if (i.getCondition().getValue()) {
                        image = (ImageItem) rePath(Compresser.compressDataForDatabase((ImageItem) i));
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
                        file = rePath(Compresser.compressDataForDatabase((FileItem) i));
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
                throw new IllegalArgumentException("Unknown Item type");
        }

    }

    /**
     * Adds the items to the database and possibly store them
     *
     * @param items The items to be stored
     */
    public void storeItems(List<Item> items) {
        for (Item i : items) {
            storeItem(i);
        }
    }

    /**
     * Updates the ChatAdapter with the potential full data when retrieved.
     *
     * @param i the item
     * @param v the ChatAdapter
     */
    public void updateChatWithItem(Item i, ChatAdapter v) {
        new ReadTask(v).execute(i);
    }

    /**
     * Updates the dialog with the full data of the item.
     *
     * @param i The item
     * @param d the dialog
     */
    public void updateDialogWithItem(Item i, AlertDialog d) {
        new ReadTask(d).execute(i);
    }

    /**
     * Updates the ChatAdapter with the full data of the items.
     *
     * @param items The list of items to update
     * @param a     the ChatAdapter
     */
    public void updateChatWithItems(List<Item> items, ChatAdapter a) {
        new ReadTask(a).execute(items.toArray(new Item[items.size()]));
    }

    private class ReadTask extends AsyncTask<Item, Item, Void> {
        private ChatAdapter a;
        private AlertDialog d;

        public ReadTask(ChatAdapter a) {
            this.a = a;
        }

        public ReadTask(AlertDialog d) {
            this.d = d;
        }

        @Override
        protected Void doInBackground(Item... params) {
            for (Item i : params) {
                if (i != null) {
                    switch (i.getType()) {
                        case SIMPLETEXTITEM:
                            break;
                        case FILEITEM:
                            String filePath = ((FileItem) i).getPath();
                            byte[] fileData = getData((FileItem) i);
                            if (fileData != null) {
                                FileItem.Builder fileBuilder = new FileItem.Builder();
                                fileBuilder.setID(i.getID());
                                fileBuilder.setFrom(i.getFrom());
                                fileBuilder.setTo(i.getTo());
                                fileBuilder.setDate(i.getDate());
                                fileBuilder.setCondition(i.getCondition());
                                fileBuilder.setPath(filePath);
                                fileBuilder.setData(fileData);
                                publishProgress(fileBuilder.build());
                            }
                            break;
                        case IMAGEITEM:
                            String imagePath = ((ImageItem) i).getPath();
                            byte[] imageData = getData((ImageItem) i);
                            if (imageData != null) {
                                ImageItem.Builder imageBuilder = new ImageItem.Builder();
                                imageBuilder.setID(i.getID());
                                imageBuilder.setFrom(i.getFrom());
                                imageBuilder.setTo(i.getTo());
                                imageBuilder.setDate(i.getDate());
                                imageBuilder.setCondition(i.getCondition());
                                imageBuilder.setPath(imagePath);
                                imageBuilder.setData(imageData);
                                publishProgress(imageBuilder.build());
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown item type");
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Item... i) {
            if (d != null) {
                d.setView(i[0].getView(app));
            } else if (a != null) {
                a.update(i[0]);
                a.notifyDataSetChanged();
            } else {
                //The adapter or dialog has been destroyed
                this.cancel(true);
            }
        }
    }

    private byte[] getData(FileItem f) {
        if (isExternalStorageReadable()) {
            switch (f.getType()) {
                case FILEITEM:
                    File file = new File(f.getPath());
                    if (!file.exists()) {
                        return null;
                    } else {
                        try {
                            return FileUtils.toByteArray(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                case IMAGEITEM:
                    File image = new File(f.getPath());
                    if (!image.exists()) {
                        return null;
                    } else {
                        try {
                            return FileUtils.toByteArray(image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                default:
                    throw new IllegalArgumentException("Expected FileItem");
            }
        } else {
            showStorageStateToast();
            return null;
        }
    }

    private void storeFile(FileItem f) {
        WritingTask task = new WritingTask(f, 0);
        currentWritingTasks.add(task);
        currentFilesID.add(f.getID());
        task.execute();
    }

    private void showStorageStateToast() {
        switch (Environment.getExternalStorageState()) {
            case Environment.MEDIA_UNMOUNTED:
                Toast.makeText(app, "Storage inaccessible, please mount" +
                        "the storage.", Toast.LENGTH_LONG).show();
            case Environment.MEDIA_SHARED:
                Toast.makeText(app, "Storage inaccessible, please " +
                        "disconnect your phone from any connected usb device.", Toast.LENGTH_LONG).show();
                break;
            case Environment.MEDIA_UNMOUNTABLE:
                Toast.makeText(app, "Storage inaccessible, storage is " +
                        "probably corrupted.", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(app, "Storage inaccessible, please check" +
                        "that your storage is usable and mounted.", Toast.LENGTH_LONG).show();
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

    private FileItem rePath(FileItem f) {
        switch (f.getType()) {
            case FILEITEM:
                File filePath = Environment.getExternalStoragePublicDirectory(FILE_FOLDER_NAME);
                return new FileItem(f.getID(), f.getFrom(), f.getTo(), f.getDate(), f.getCondition(), f.getData(), filePath.getAbsolutePath() + '/' + FILENAME + "" + formatDate() + NAME_SUFFIX + app.getTodayFileCount());
            case IMAGEITEM:
                File imagePath = Environment.getExternalStoragePublicDirectory(IMAGE_FOLDER_NAME);
                return new ImageItem(f.getID(), f.getFrom(), f.getTo(), f.getDate(), f.getCondition(), f.getData(), imagePath.getAbsolutePath() + '/' + IMAGENAME + "" + formatDate() + NAME_SUFFIX + app.getTodayImageCount());
            default:
                throw new IllegalArgumentException("Expected FileItem");
        }
    }

    private String formatDate() {
        return calendar.get(Calendar.DAY_OF_MONTH) + "" + calendar.get(Calendar.MONTH) + "" + calendar.get(Calendar.YEAR);
    }

    /**
     * An AsyncTask whose task is to write a FileItem to the storage. It will retry for ~30 min (20 times) before giving up
     */
    private class WritingTask extends AsyncTask<Void, Void, Boolean> {

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
            if (isExternalStorageWritable()) {
                switch (f.getType()) {
                    case FILEITEM:
                        File filePath = Environment.getExternalStoragePublicDirectory(FILE_FOLDER_NAME);
                        if (!filePath.exists()) {
                            if (filePath.mkdirs()) {
                                try {
                                    writeFile(f, filePath);
                                    return true;
                                } catch (IOException e) {
                                    Toast.makeText(app, "Couldn't write file "
                                            + f.getName() + " to storage.", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            } else {
                                Toast.makeText(app, "Couldn't create directory", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                        try {
                            writeFile(f, filePath);
                            return true;
                        } catch (IOException e) {
                            Toast.makeText(app, "Couldn't write file "
                                    + f.getName() + " to storage.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(app, "Couldn't write image "
                                            + f.getName() + " to storage.", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            } else {
                                Toast.makeText(app, "Couldn't create directory", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        } else {
                            try {
                                writeFile(f, imagePath);
                                return true;
                            } catch (IOException e) {
                                Toast.makeText(app, "Couldn't write image "
                                        + f.getName() + " to storage.", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }

                    default:
                        throw new IllegalArgumentException("Expected FileItem");
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean b) {
            if (!b) {
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
                currentFilesID.remove(f.getID());
                currentWritingTasks.remove(this);
            }
        }
    }
}
