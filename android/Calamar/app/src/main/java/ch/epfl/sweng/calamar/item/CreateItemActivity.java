package ch.epfl.sweng.calamar.item;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
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
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contactsName);
        contactsSpinner.setAdapter(spinnerAdapter);
        file = null;
        currentLocation=null;
    }

    private void startFilePicker() {
        //TODO filepicker activity
    }

    private void locationChecked(){
        GPSProvider.getInstance().startLocationUpdates(this);
        currentLocation=GPSProvider.getInstance().getLastLocation();
        GPSProvider.getInstance().stopLocationUpdates();
    }

    private void privateChecked(){

    }

    private void timeChecked(){

    }

    private void createAndSend() {
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
            //toSendBuilder.setText(message)
        } else {
            if (toSendBuilder.getClass() == SimpleTextItem.Builder.class) {
                //Toast : must have a file and/or message
            }
        }
        if (privateCheck.isChecked()) {
            Recipient to = contacts.get(contactsSpinner.getSelectedItemPosition());
            toSendBuilder.setTo(to);
        } else {
            //TODO Public?
            toSendBuilder.setTo(null);
        }
        if (locationCheck.isChecked()){
            toSendBuilder.addCondition(new PositionCondition(currentLocation));
        }
        //TODO add time
        Item toSend = toSendBuilder.build();
    }

}
