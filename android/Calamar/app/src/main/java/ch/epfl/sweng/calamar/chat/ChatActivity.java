package ch.epfl.sweng.calamar.chat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.SQLiteDatabaseHandler;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

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

    private Recipient correspondent;

    private SQLiteDatabaseHandler databaseHandler;

    private CalamarApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String correspondentName = intent.getStringExtra(ChatFragment.EXTRA_CORRESPONDENT_NAME);
        int correspondentID = intent.getIntExtra(ChatFragment.EXTRA_CORRESPONDENT_ID, -1); // -1 = default value

        if (correspondentName == null) {
            correspondentName = "";
        }

        app = CalamarApplication.getInstance();
        correspondent = new User(correspondentID, correspondentName);

        editText = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);
        refreshButton = (Button) findViewById(R.id.refreshButton);

        messagesHistory = new ArrayList<>();
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        adapter = new ChatAdapter(this, messagesHistory);
        messagesContainer.setAdapter(adapter);

        TextView recipient = (TextView) findViewById(R.id.recipientLabel);
        recipient.setText(correspondent.getName());

        refreshButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        databaseHandler = app.getDatabaseHandler();

        boolean offline = true;
        refresh(offline);
    }


    /**
     * Gets all messages and display them
     */
    private void refresh(boolean offline) {
        new RefreshTask(app.getCurrentUser(), offline).execute();
    }

    /**
     * Sends a new message
     */
    private void send() {
        String message = editText.getText().toString();
        Item textMessage = new SimpleTextItem(1, app.getCurrentUser(), correspondent, new Date(), message);
        adapter.add(textMessage);
        adapter.notifyDataSetChanged();
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
        editText.setText("");
        new SendItemTask(textMessage).execute();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.chatSendButton) {
            send();
        } else if (v.getId() == R.id.refreshButton) {
            refresh(false);
        } else {
            throw new IllegalArgumentException("Got an unexpected view Id in Onclick");
        }
    }


    /**
     * Async task for sending a message.
     */
    private class SendItemTask extends AsyncTask<Void, Void, Void> {

        private final Item textMessage;

        public SendItemTask(Item textMessage) {
            this.textMessage = textMessage;
        }

        @Override
        protected Void doInBackground(Void... v) {
            try {
                //TODO : Determine id of the message ?
                DatabaseClientLocator.getDatabaseClient().send(textMessage);
                //TODO need id to put into database
                databaseHandler.addItem(textMessage);
                return null;
                //return itemClients[0].send(textMessage);
            } catch (DatabaseClientException e) {
                //TODO : TOAST
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Async task for refreshing / getting new messages.
     */
    private class RefreshTask extends AsyncTask<Void, Void, List<Item>> {

        private final Recipient recipient;
        private final boolean offline;

        public RefreshTask(Recipient recipient, boolean offline) {
            this.recipient = recipient;
            this.offline = offline;
        }

        @Override
        protected List<Item> doInBackground(Void... v) {
            if (offline) {
                return databaseHandler.getItemsForContact(correspondent);
            } else {
                try {
                    return DatabaseClientLocator.getDatabaseClient().getAllItems(recipient, new Date(app.getLastItemsRefresh()));
                } catch (DatabaseClientException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            if (items != null) {
                databaseHandler.addItems(items);
                adapter.add(items);
                adapter.notifyDataSetChanged();
                messagesContainer.setSelection(messagesContainer.getCount() - 1);
                Toast.makeText(getApplicationContext(), R.string.chat_activity_refresh_message,
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), R.string.chat_activity_unable_to_refresh,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

}
