package ch.epfl.sweng.calamar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatUsersListActivity extends AppCompatActivity {

    public final static String EXTRA_CORRESPONDENT_NAME = "ch.epfl.sweng.calamar.CORRESPONDENT_NAME";
    public final static String EXTRA_CORRESPONDENT_ID = "ch.epfl.sweng.calamar.CORRESPONDENT_ID";

    private ListView contactsView;
    private List<User> contacts;
    private ChatUsersListAdapter adapter;
    private TextView actualUserTextView;

    private UserClient client;

    private CalamarApplication app;

    private static final String SERVER_BASE_URL = "http://calamar.japan-impact.ch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        app = ((CalamarApplication) getApplication()).getInstance();

        client = new NetworkDatabaseClient(SERVER_BASE_URL,new DefaultNetworkProvider());

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
                User user = contacts.get(position);
                conversation.putExtra(EXTRA_CORRESPONDENT_NAME,user.getName());
                conversation.putExtra(EXTRA_CORRESPONDENT_ID, user.getID());
                startActivity(conversation);
            }
        });
        contactsView.setSelection(0);
    }

    /**
     * Return the actual user of the app.
     */
    private void setActualUser(){
        // if 0, create a new user !
        if(app.getCurrentUserID() == -1){
            String name = "No Email";
            //Get google account email
            AccountManager manager = AccountManager.get(this);
            Account[] list = manager.getAccountsByType("com.google");
            if(list.length > 0){
                name = list[0].name;
            }
            new createNewUserTask(name).execute(client);
        }
        actualUserTextView.setText("Actual user : " + app.getCurrentUserName());
    }

    private void getContacts(){
        //TODO : Store contact ? -- Easy once persist_data is merged
        contacts.add(new User(2,"Bob"));
        contacts.add(new User(3,"Carol"));
        contacts.add(new User(4,"Denis"));
        contacts.add(new User(5,"Eve"));
    }

    /**
     * Async task for sending a message.
     *
     */
    private class createNewUserTask extends AsyncTask<UserClient, Void, Integer> {

        private String name = "No name";

        public createNewUserTask(String name){
            this.name = name;
        }

        @Override
        protected Integer doInBackground(UserClient... itemClients) {
            try {
                //Get the device id.
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                return client.newUser(name,telephonyManager.getDeviceId());
            } catch (ItemClientException e) {
                //TODO : TOAST
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
            } else {
                //TODO : Feedback
            }
        }
    }

}
