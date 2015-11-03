package ch.epfl.sweng.calamar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatUsersListActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String EXTRA_CORRESPONDENT_NAME = "ch.epfl.sweng.calamar.CORRESPONDENT_NAME";
    public final static String EXTRA_CORRESPONDENT_ID = "ch.epfl.sweng.calamar.CORRESPONDENT_ID";

    private ListView contactsView;
    private List<User> contacts;
    private ChatUsersListAdapter adapter;
    private TextView actualUserTextView;

    public static User actualUser = new User(1,"Alice");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        contacts = new ArrayList<>();
        getContacts();

        //TODO I don't think it is necessary to remind the user who he is (okay for development)
        actualUserTextView = (TextView) findViewById(R.id.actualUserName);
        actualUserTextView.setText("Actual user : " + actualUser.getName());

        contactsView = (ListView) findViewById(R.id.contactsList);
        contactsView.setSelector(R.drawable.list_selector);
        adapter = new ChatUsersListAdapter(this,contacts);
        contactsView.setAdapter(adapter);
        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent conversation = new Intent(ChatUsersListActivity.this, ChatActivity.class);
                //Assuming in same order
                User user = contacts.get(position);
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
        newContact.setView(input);

        newContact.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                User newUser = new User(10,input.getText().toString());
                adapter.add(newUser);
                contacts.add(newUser);
                adapter.notifyDataSetChanged();
                //TODO : Add in memory
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
        //TODO : Store contact ? -- Easy once persist_data is merged
        contacts.add(new User(2,"Bob"));
        contacts.add(new User(3,"Carol"));
        contacts.add(new User(4,"Denis"));
        contacts.add(new User(5,"Eve"));
    }

}
