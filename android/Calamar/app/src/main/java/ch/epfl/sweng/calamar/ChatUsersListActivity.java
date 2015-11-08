package ch.epfl.sweng.calamar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    private ItemClient client;

    private CalamarApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        app = ((CalamarApplication) getApplication()).getInstance();

        client = ItemClientLocator.getItemClient();

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
            throw new IllegalArgumentException(getString(R.string.on_click_error));
        }
    }

    private void addNewContact(){
        AlertDialog.Builder newContact = new AlertDialog.Builder(this);

        newContact.setTitle(getString(R.string.add_new_contact_title));
        newContact.setMessage(getString(R.string.add_new_contact_message));

        final EditText input = new EditText(this);
        input.setHint(getString(R.string.add_new_contact_hint));
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input);
        newContact.setView(layout);

        newContact.setPositiveButton(getString(R.string.add_new_contact_positive_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new retrieveUserTask(input.getText().toString(), ChatUsersListActivity.this).execute(client);
            }
        });

        newContact.setNegativeButton(getString(R.string.add_new_contact_negative_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        newContact.show();
    }

    private void getContacts(){
        contacts.addAll(app.getDB().getAllRecipients());
    }

    /**
     * Async task for refreshing / getting new messages.
     */
    private class retrieveUserTask extends AsyncTask<ItemClient, Void, User> {

        private String name;
        private Context context;

        public retrieveUserTask(String name,Context context) {
            this.name = name;
            this.context = context;
        }

        @Override
        protected User doInBackground(ItemClient... itemClients) {
                try {
                    return itemClients[0].retrieveUserFromName(name);
                } catch (ItemClientException e) {
                    e.printStackTrace();
                    return null;
                }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                User newUser = new User(user.getID(), user.getName());
                adapter.add(newUser);
                contacts.add(newUser);
                adapter.notifyDataSetChanged();
                //Add in memory
                app.getDB().addRecipient(newUser);

            } else {
                AlertDialog.Builder newUserAlert = new AlertDialog.Builder(context);
                newUserAlert.setTitle(getString(R.string.error_add_contact));
                newUserAlert.setPositiveButton(getString(R.string.alert_dialog_default_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //OK
                    }
                });
                newUserAlert.show();
            }
        }
    }

}
