package ch.epfl.sweng.calamar.item;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.calamar.BaseActivity;
import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.map.GPSProvider;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

public class CreateItemActivity extends BaseActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final String RECIPIENT_EXTRA_ID = "ID";
    private static final String RECIPIENT_EXTRA_NAME = "Name";

    private static final String TAG = CreateItemActivity.class.getSimpleName();

    private Set<String> imageExt;
    private Spinner contactsSpinner;
    private CheckBox privateCheck;
    private CheckBox locationCheck;
    private EditText message;
    private File file;
    private List<Recipient> contacts;
    private Location currentLocation;
    private RadioGroup timeGroup;
    private CheckBox timeCheck;
    private Button browseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);

        privateCheck = (CheckBox) findViewById(R.id.privateCheck);
        locationCheck = (CheckBox) findViewById(R.id.locationCheck);
        message = (EditText) findViewById(R.id.createItemActivity_messageText);

        contacts = CalamarApplication.getInstance().getDatabaseHandler().getAllRecipients();
        List<String> contactsName = new ArrayList<>();
        for (Recipient r : contacts) {
            contactsName.add(r.getName());
        }
        contactsSpinner = (Spinner) findViewById(R.id.contactSpinner);
        contactsSpinner.setVisibility(View.INVISIBLE);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contactsName);
        contactsSpinner.setAdapter(spinnerAdapter);

        timeCheck = (CheckBox) findViewById(R.id.timeCheck);
        timeGroup = (RadioGroup) findViewById(R.id.timeGroup);
        timeGroup.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        final int id = intent.getIntExtra(RECIPIENT_EXTRA_ID, -1);
        if (id != -1) {
            final String name = intent.getStringExtra(RECIPIENT_EXTRA_NAME);
            contactsSpinner.setVisibility(View.VISIBLE);
            privateCheck.setChecked(true);
            contactsSpinner.setSelection(contacts.indexOf(new User(id, name)));
        }
        browseButton = (Button) findViewById(R.id.selectFileButton);

        final String[] imgExt = {"png", "PNG", "jpg", "jpeg", "JPG", "JPEG", "bmp", "BMP"};
        imageExt = new HashSet<>(Arrays.asList(imgExt));
        file = null;
        currentLocation = null;
    }

    //Button listeners ; Not a big fan of methods having to be public
    public void startFilePicker(View v) {
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(target, getString(R.string.choose_file));
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.select_file_title)), PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri fileUri = data.getData();
                    String path = FileUtils.getPath(this, fileUri);
                    if (path != null) {
                        file = new File(path);
                        if (file.getName().length() > 15) {
                            String text = file.getName().substring(0, 15) + "...";
                            browseButton.setText(text);
                        } else {
                            browseButton.setText(file.getName());
                        }
                    } else {
                        Toast.makeText(this, R.string.select_local_file, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public void locationChecked(View v) {
        // TODO timing exception, client may not be connected,
        // similar problem can arise at multiple place, we need
        // to design kind of protocol between the callbacks, to wait/disable UI/ re iterate action
        // ...etc ...or any other clever idea....
        GPSProvider.getInstance().startLocationUpdates(this);
        currentLocation = GPSProvider.getInstance().getLastLocation();
        GPSProvider.getInstance().stopLocationUpdates();
    }

    public void privateChecked(View v) {
        if (privateCheck.isChecked()) {
            contactsSpinner.setVisibility(View.VISIBLE);
        } else {
            contactsSpinner.setVisibility(View.INVISIBLE);
        }
    }

    public void timeChecked(View v) {
        if (timeCheck.isChecked()) {
            timeGroup.setVisibility(View.VISIBLE);
        } else {
            timeGroup.setVisibility(View.INVISIBLE);
        }
    }

    public void createAndSend(View v) throws IOException {
        Item.Builder toSendBuilder;
        if (file != null) {
            String name = file.getName();
            int extIndex = name.lastIndexOf('.');
            String ext = extIndex > 0 ? name.substring(extIndex + 1) : "";
            if (imageExt.contains(ext)) {
                toSendBuilder = new ImageItem.Builder().setFile(file);
            } else {
                toSendBuilder = new FileItem.Builder().setFile(file);
            }
        } else {
            toSendBuilder = new SimpleTextItem.Builder();
        }
        if (message.getText().toString().equals("") && toSendBuilder.getClass() == SimpleTextItem.Builder.class) {

            Toast.makeText(getApplicationContext(), getString(R.string.item_create_invalid),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (privateCheck.isChecked()) {
            // TODO clean this..........
            int contactPosition = contactsSpinner.getSelectedItemPosition();
            if (contactPosition != -1) {
                Recipient to = contacts.get(contactsSpinner.getSelectedItemPosition());
                toSendBuilder.setTo(to);
            } else {
                toSendBuilder.setTo(new User(User.PUBLIC_ID, User.PUBLIC_NAME));
            }
        } else {
            toSendBuilder.setTo(new User(User.PUBLIC_ID, User.PUBLIC_NAME));
        }
        if (locationCheck.isChecked()) {
            toSendBuilder.setCondition(new PositionCondition(currentLocation));
        }
        toSendBuilder.setFrom(CalamarApplication.getInstance().getCurrentUser());
        toSendBuilder.setDate(new Date().getTime());
        toSendBuilder.setMessage(message.getText().toString());
        Item toSend = toSendBuilder.build();
        new SendItemTask(toSend).execute();
        this.finish();
    }

    /**
     * Async task for sending the created item.
     */
    private class SendItemTask extends AsyncTask<Void, Void, Item> {

        private final Item item;

        public SendItemTask(Item item) {
            this.item = item;
        }

        @Override
        protected Item doInBackground(Void... v) {
            try {
                return DatabaseClientLocator.getDatabaseClient().send(item);
            } catch (DatabaseClientException e) {
                Log.e(CreateItemActivity.TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Item item) {
            if (item != null) {
                CalamarApplication.getInstance().getDatabaseHandler().addItem(item);
                Toast.makeText(getApplicationContext(), getString(R.string.item_sent_successful), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.item_send_error),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}
