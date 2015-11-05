package ch.epfl.sweng.calamar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatUsersListActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String EXTRA_CORRESPONDENT_NAME = "ch.epfl.sweng.calamar.CORRESPONDENT_NAME";
    public final static String EXTRA_CORRESPONDENT_ID = "ch.epfl.sweng.calamar.CORRESPONDENT_ID";

    private ListView contactsView;
    private List<Recipient> contacts;
    private ChatUsersListAdapter adapter;
    private TextView actualUserTextView;

    private CalamarApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        app = ((CalamarApplication) getApplication()).getInstance();

        contacts = new ArrayList<>();
        getContacts();

        //TODO I don't think it is necessary to remind the user who he is (okay for development)
        actualUserTextView = (TextView) findViewById(R.id.actualUserName);
        actualUserTextView.setText("Actual user : " + ((CalamarApplication) getApplication()).getInstance().getCurrentUserName());

        contactsView = (ListView) findViewById(R.id.contactsList);
        contactsView.setSelector(R.drawable.list_selector);
        adapter = new ChatUsersListAdapter(this,contacts);
        contactsView.setAdapter(adapter);
        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent conversation = new Intent(ChatUsersListActivity.this, ChatActivity.class);
                //Assuming in same order
                Recipient user = contacts.get(position);
                conversation.putExtra(EXTRA_CORRESPONDENT_NAME,user.getName());
                conversation.putExtra(EXTRA_CORRESPONDENT_ID, user.getID());
                startActivity(conversation);
            }
        });
        contactsView.setSelection(0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.newContact) {
            addNewContact();
        } else {
            throw new IllegalArgumentException("Got an unexpected view Id in Onclick");
        }
    }

    private void addNewContact(){
        AlertDialog.Builder newContact = new AlertDialog.Builder(this);

        newContact.setTitle("Add a new contact");
        newContact.setMessage("Enter the mail of the new contact");

        final EditText input = new EditText(this);
        input.setHint("Mail");
        final EditText idEdit = new EditText(this);
        idEdit.setHint("id");
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input);
        layout.addView(idEdit);
        idEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        newContact.setView(layout);

        newContact.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //TODO : set the good id
                User newUser = new User(Integer.parseInt(idEdit.getText().toString()), input.getText().toString());
                adapter.add(newUser);
                contacts.add(newUser);
                adapter.notifyDataSetChanged();
                //Add in memory
                app.getDB().addRecipient(newUser);
            }
        });

        newContact.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        newContact.show();
    }

    private void getContacts(){
        contacts.addAll(app.getDB().getAllRecipients());
    }

}
