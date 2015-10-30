package ch.epfl.sweng.calamar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//TODO Support other item types

/**
 * This activity manages the chat between two users (or in a group)
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editText;
    private Button sendButton;
    private Button refreshButton;
    private List<Item> messagesHistory;
    private ListView messagesContainer;
    private ChatAdapter adapter;

    private NetworkDatabaseClient client;

    public static User actualUser = new User(-1,"Unknown");
    private User correspondent;

    private Date lastRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        lastRefresh = new Date(0);
        setActualUser();

        correspondent = new User(2,"Bob");

        client = new NetworkDatabaseClient("http://calamar.japan-impact.ch",new DefaultNetworkProvider());

        editText = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);
        refreshButton = (Button) findViewById(R.id.refreshButton);

        messagesHistory = new ArrayList<>();
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        adapter = new ChatAdapter(this, messagesHistory);
        messagesContainer.setAdapter(adapter);

        TextView recipient = (TextView) findViewById(R.id.recipientLabel);
        //TODO Change Recipient depending on User ID
        recipient.setText("Someone");

        refreshButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        //refresh();
    }


    /**
     * Return the actual user of the app.
     */
    private void setActualUser(){
        // if 0, create a new user !
        if(lastRefresh.getTime() == 0){
            String name = "No Email";
            //Get google account email
            AccountManager manager = AccountManager.get(this);
            Account[] list = manager.getAccountsByType("com.google");
            if(list.length > 0){
                name = list[0].name;
            }
            new createNewUserTask(name).execute(client);
       } else {
           //TODO : Go in the bdd get the user.
           // actualUser = ...
       }
    }

    /**
     * Gets all messages and display them
     */
    private void refresh() {
         new refreshTask(actualUser).execute(client);
    }

    /**
     * Sends a new message
     */
    private void send() {
        String message = editText.getText().toString();
        Item textMessage = new SimpleTextItem(1,actualUser,correspondent,new Date(),message);
        adapter.add(textMessage);
        adapter.notifyDataSetChanged();
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
        editText.setText("");
        new sendItemTask(textMessage).execute(client);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.chatSendButton) {
            send();
        } else if (v.getId() == R.id.refreshButton) {
            refresh();
        } else {
            throw new IllegalArgumentException("Got an unexpected view Id in Onclick");
        }
    }



    /**
     * Async task for sending a message.
     *
     */
    private class sendItemTask extends AsyncTask<ItemClient, Void, Void> {

        private Item textMessage;
        public sendItemTask(Item textMessage){
            this.textMessage = textMessage;
        }

        @Override
        protected Void doInBackground(ItemClient... itemClients) {
            try {
                //TODO : Determine id of the message ?

                itemClients[0].send(textMessage);
                return null;
                //return itemClients[0].send(textMessage);
            } catch (ItemClientException e) {
                //TODO : TOAST
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Async task for sending a message.
     *
     */
    private class refreshTask extends AsyncTask<ItemClient, Void, List<Item>> {

        private Recipient recipient;

        public refreshTask(Recipient recipient){
            this.recipient = recipient;
        }

        @Override
        protected List<Item> doInBackground(ItemClient... itemClients) {
            try {
                return itemClients[0].getAllItems(recipient,lastRefresh);
            } catch (ItemClientException e) {
                //TODO : TOAST
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            if(items != null) {
                adapter.add(items);
                adapter.notifyDataSetChanged();
                messagesContainer.setSelection(messagesContainer.getCount() - 1);
                lastRefresh = new Date();
            }
        }

    }

    /**
     * Async task for sending a message.
     *
     */
    private class createNewUserTask extends AsyncTask<ItemClient, Void, Integer> {

        private String name = "No name";

        public createNewUserTask(String name){
            this.name = name;
        }

        @Override
        protected Integer doInBackground(ItemClient... itemClients) {
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
                actualUser = new User(id,name);
                //TODO : Store in ddb
            } else {
                //TODO : TOAST
            }
        }
    }
}
