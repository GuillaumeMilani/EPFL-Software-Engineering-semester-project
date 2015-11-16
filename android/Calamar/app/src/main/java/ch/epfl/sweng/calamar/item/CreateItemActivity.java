package ch.epfl.sweng.calamar.item;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.map.GPSProvider;
import ch.epfl.sweng.calamar.recipient.Recipient;

public class CreateItemActivity extends AppCompatActivity {

    private Spinner contactsSpinner;
    private CheckBox privateCheck;
    private CheckBox locationCheck;
    private EditText message;
    private File file;
    private List<Recipient> contacts;
    private List<String> contactsName;
    private Location currentLocation;
    private RadioGroup timeGroup;
    private CheckBox timeCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);
        privateCheck = (CheckBox) findViewById(R.id.privateCheck);
        locationCheck = (CheckBox) findViewById(R.id.locationCheck);
        message = (EditText) findViewById(R.id.createItemActivity_messageText);
        contacts = CalamarApplication.getInstance().getDB().getAllRecipients();
        contactsName = new ArrayList<>();
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
        file = null;
        currentLocation = null;
    }

    //Button listeners ; Not a big fan of methods having to be public
    public void startFilePicker(View v) {
        //TODO filepicker activity
    }

    public void locationChecked(View v) {
        GPSProvider.getInstance().startLocationUpdates(this);
        currentLocation = GPSProvider.getInstance().getLastLocation();
        GPSProvider.getInstance().stopLocationUpdates();
    }

    public void privateChecked(View v) {
        if (privateCheck.isChecked()) {
            contactsSpinner.setVisibility(View.VISIBLE);
        } else {
            timeGroup.setVisibility(View.INVISIBLE);
        }
    }

    public void timeChecked(View v) {
        if (timeCheck.isChecked()) {
            timeGroup.setVisibility(View.VISIBLE);
        } else {
            timeGroup.setVisibility(View.INVISIBLE);
        }
    }

    public void createAndSend(View v) {
        Item.Builder toSendBuilder = null;
        if (file != null) {
            String name = file.getName();
            int extIndex = name.lastIndexOf('.');
            String ext = extIndex > 0 ? name.substring(extIndex + 1) : "";
            if (ext.equals("png") || ext.equals("jpg") || ext.equals("bmp")
                    || ext.equals("PNG") || ext.equals("JPG") || ext.equals("jpeg")
                    || ext.equals("JPEG")) {
                //TODO add image
            }
            //TODO add audio
        } else {
            toSendBuilder = new SimpleTextItem.Builder();
        }
        if (!message.getText().toString().equals("")) {
            ((SimpleTextItem.Builder) toSendBuilder).setMessage(message.getText().toString());
        } else {
            if (toSendBuilder.getClass() == SimpleTextItem.Builder.class) {
                //TODO Toast : must have a file or message or both
            }
        }
        if (privateCheck.isChecked()) {
            Recipient to = contacts.get(contactsSpinner.getSelectedItemPosition());
            toSendBuilder.setTo(to);
        } else {
            //TODO Public = null ? Not allowed by Item constructor at the moment
            toSendBuilder.setTo(null);
        }
        if (locationCheck.isChecked()) {
            toSendBuilder.setCondition(new PositionCondition(currentLocation));
        }
        toSendBuilder.setFrom(CalamarApplication.getInstance().getCurrentUser());
        toSendBuilder.setDate(new Date().getTime());
        //TODO add time
        Item toSend = toSendBuilder.build();
    }

}
