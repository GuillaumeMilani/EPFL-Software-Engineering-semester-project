package ch.epfl.sweng.calamar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
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

    private CalamarApplication app;

    private static final String SERVER_BASE_URL = "http://calamar.japan-impact.ch";

    private AlertDialog lastAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        app = ((CalamarApplication) getApplication()).getInstance();

        contacts = new ArrayList<>();
        getContacts();

        actualUserTextView = (TextView) findViewById(R.id.actualUserName);
        setActualUser();

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

    /**
     * Return the actual user of the app.
     */
    private void setActualUser(){
        //TODO : Remove when you use a real device.
        app.setCurrentUserID(11);
        app.setCurrentUserName("calamaremulator@gmail.com");

        if(app.getCurrentUserID() == -1){
            String name = null;
            //Get google account email
            AccountManager manager = AccountManager.get(this);
            Account[] list = manager.getAccountsByType("com.google");
            if(list.length > 0){
                name = list[0].name;
            }
            new createNewUserTask(name,this).execute();
        }
        actualUserTextView.setText("Actual user : " + app.getCurrentUserName());
    }

    private void getContacts(){
        contacts.addAll(app.getDB().getAllRecipients());
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
                new retrieveUserTask(input.getText().toString(), ChatUsersListActivity.this).execute();
            }
        });

        newContact.setNegativeButton(getString(R.string.add_new_contact_negative_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        lastAlert = newContact.show();
    }


    /**
     +     * Async task for sending a message.
     +     *
     +     */
    private class createNewUserTask extends AsyncTask<Void, Void, Integer> {
        private String name = null;
        private Context context;

        public createNewUserTask(String name,Context context){
            this.name = name;
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Void... v) {
            try {
                //Get the device id.
                return DatabaseClientLocator.getDatabaseClient().newUser(name, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));//"aaaaaaaaaaaaaaaa",354436053190805
            } catch (DatabaseClientException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer id) {
            if(id != null) {
                app.setCurrentUserID(id);
                app.setCurrentUserName(name);
                actualUserTextView.setText("Actual user : " + name);
                AlertDialog.Builder newUser = new AlertDialog.Builder(context);
                newUser.setTitle("Account correctly created : User : " + name + ", id : " + id);
                newUser.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //OK
                    }
                });
                lastAlert = newUser.show();
            } else {
                AlertDialog.Builder newUser = new AlertDialog.Builder(context);
                newUser.setTitle("Your account creation has failed, check your internet connection.");
                newUser.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new createNewUserTask(name, context).execute();
                    }
                });
                lastAlert = newUser.show();
            }
        }
    }


    /**
     * Async task for retrieving a new user.
     */
    private class retrieveUserTask extends AsyncTask<Void, Void, User> {

        private String name = null;
        private Context context;

        public retrieveUserTask(String name,Context context) {
            this.name = name;
            this.context = context;
        }

        @Override
        protected User doInBackground(Void... v) {
            try {
                return DatabaseClientLocator.getDatabaseClient().retrieveUserFromName(name);
            } catch (DatabaseClientException e) {
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
                newUserAlert.setTitle("Impossible to add the contact");
                newUserAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //OK
                    }
                });
                newUserAlert.show();
            }
        }
    }


    public AlertDialog getLastDialog(){
        return lastAlert;
    }



}
