package ch.epfl.sweng.calamar.chat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

public class ChatUsersListActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String EXTRA_CORRESPONDENT_NAME = "ch.epfl.sweng.calamar.CORRESPONDENT_NAME";
    public final static String EXTRA_CORRESPONDENT_ID = "ch.epfl.sweng.calamar.CORRESPONDENT_ID";

    private ListView contactsView;
    private List<Recipient> contacts;
    private ChatUsersListAdapter adapter;
    private TextView actualUserTextView;

    private CalamarApplication app;

    private Dialog newContactAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        app = CalamarApplication.getInstance();

        contacts = new ArrayList<>();
        getContacts();

        actualUserTextView = (TextView) findViewById(R.id.actualUserName);
        setActualUser();

        contactsView = (ListView) findViewById(R.id.contactsList);
        contactsView.setSelector(R.drawable.list_selector);
        adapter = new ChatUsersListAdapter(this, contacts);
        contactsView.setAdapter(adapter);
        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent conversation = new Intent(ChatUsersListActivity.this, ChatActivity.class);
                //Assuming in same order
                Recipient user = contacts.get(position);
                conversation.putExtra(EXTRA_CORRESPONDENT_NAME, user.getName());

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
     * Called by create_new_contact layout
     *
     * @param v
     */
    public void addContact(View v) {
        EditText input = (EditText) newContactAlertDialog.findViewById(R.id.newContactInput);
        newContactAlertDialog.dismiss();
        new retrieveUserTask(input.getText().toString(), ChatUsersListActivity.this).execute();
    }

    /**
     * Called by create_new_contact layout
     *
     * @param v
     */
    public void cancelNewContact(View v) {
        newContactAlertDialog.dismiss();
    }

    /**
     * Return the actual user of the app.
     */
    private void setActualUser() {
        //TODO : Remove when you use a real device.
        app.setCurrentUserID(11);
        app.setCurrentUserName("calamaremulator@gmail.com");

        if (app.getCurrentUserID() == -1) {
            String name = null;
            //Get google account email
            AccountManager manager = AccountManager.get(this);
            Account[] list = manager.getAccountsByType("com.google");
            if (list.length > 0) {
                name = list[0].name;
            }
            new createNewUserTask(name, this).execute();
        }
        actualUserTextView.setText("Actual user : " + app.getCurrentUserName());
    }

    private void getContacts() {
        contacts.addAll(app.getDB().getAllRecipients());
    }

    private void addNewContact() {
        newContactAlertDialog = new Dialog(this);

        newContactAlertDialog.setContentView(R.layout.create_new_contact);
        newContactAlertDialog.setTitle(getString(R.string.add_new_contact_title));

        newContactAlertDialog.show();
    }

    /**
     * Async task for sending a message.
     */
    private class createNewUserTask extends AsyncTask<Void, Void, Integer> {
        private String name = null;
        private final Context context;

        public createNewUserTask(String name, Context context) {
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
            if (id != null) {
                app.setCurrentUserID(id);
                app.setCurrentUserName(name);
                actualUserTextView.setText("Actual user : " + name);
                AlertDialog.Builder newUser = new AlertDialog.Builder(context);
                newUser.setTitle(getString(R.string.new_account_creation_success) + name);
                newUser.setPositiveButton(R.string.alert_dialog_default_positive_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //OK
                    }
                });
                newUser.show();
            } else {
                AlertDialog.Builder newUser = new AlertDialog.Builder(context);
                newUser.setTitle(R.string.new_account_creation_fail);
                newUser.setPositiveButton(R.string.new_account_creation_retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new createNewUserTask(name, context).execute();
                    }
                });
                newUser.show();
            }
        }
    }


    /**
     * Async task for retrieving a new user.
     */
    private class retrieveUserTask extends AsyncTask<Void, Void, User> {

        private String name = null;
        private final Context context;

        public retrieveUserTask(String name, Context context) {
            this.name = name;
            this.context = context;
        }

        @Override
        protected User doInBackground(Void... v) {
            try {
                return DatabaseClientLocator.getDatabaseClient().findUserByName(name);
            } catch (DatabaseClientException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User newUser) {
            if (newUser != null) {
                adapter.add(newUser);
                contacts.add(newUser);
                adapter.notifyDataSetChanged();
                //Add in memory
                app.getDB().addRecipient(newUser);
            } else {
                AlertDialog.Builder newUserAlert = new AlertDialog.Builder(context);
                newUserAlert.setTitle(R.string.add_new_contact_impossible);
                newUserAlert.setPositiveButton(R.string.alert_dialog_default_positive_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //OK
                    }
                });
                newUserAlert.show();
            }
        }
    }

}
