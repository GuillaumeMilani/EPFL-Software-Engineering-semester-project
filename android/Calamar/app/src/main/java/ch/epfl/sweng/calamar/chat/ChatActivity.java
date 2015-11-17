package ch.epfl.sweng.calamar.chat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.BaseActivity;
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
public class ChatActivity extends BaseActivity implements View.OnClickListener {
    private EditText editText;
    private Button sendButton;
    private Button refreshButton;
    private List<Item> messagesHistory;
    private ListView messagesContainer;
    private ChatAdapter adapter;

    private Recipient correspondent;

    private SQLiteDatabaseHandler databaseHandler;

    private CalamarApplication app;

    private final String TAG = ChatActivity.class.getSimpleName();

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

        databaseHandler = app.getDB();

        boolean offline = true;
        refresh(offline);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.chatSendButton) {
            sendTextItem();
        } else if (v.getId() == R.id.refreshButton) {
            refresh(false);
        } else {
            throw new IllegalArgumentException("Got an unexpected view Id in Onclick");
        }
    }

    /**
     * Gets all messages and display them
     */
    private void refresh(boolean offline) {
        new RefreshTask(app.getCurrentUser(), offline).execute();
    }

    /**
     * Sends a new text message
     */
    private void sendTextItem() {
        String message = editText.getText().toString();
        Item textMessage = new SimpleTextItem(1, app.getCurrentUser(), correspondent, new Date(), message);
        adapter.notifyDataSetChanged();
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
        editText.setText("");
        new SendItemTask(textMessage).execute();
    }


    /**
     * Async task for sending a message.
     */
    private class SendItemTask extends AsyncTask<Void, Void, Item> {

        private final Item item;

        public SendItemTask(Item item) {
            this.item = item;
        }

        @Override
        protected Item doInBackground(Void... v) {
            try {
                return DatabaseClientLocator.getDatabaseClient().send(item);
            } catch (DatabaseClientException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Item item) {
            if (item != null) {
                adapter.add(item);
                adapter.notifyDataSetChanged();
                messagesContainer.setSelection(messagesContainer.getCount() - 1);
                databaseHandler.addItem(item);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.item_send_error),
                        Toast.LENGTH_SHORT).show();
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
                new AddToDatabaseTask().execute(items.toArray(new Item[items.size()]));
                adapter.add(items);
                adapter.notifyDataSetChanged();
                messagesContainer.setSelection(messagesContainer.getCount() - 1);
                if (!offline) {
                    app.setLastItemsRefresh(new Date());
                }

                Toast.makeText(getApplicationContext(), R.string.chat_activity_refresh_message,
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), R.string.chat_activity_unable_to_refresh,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class AddToDatabaseTask extends AsyncTask<Item, Void, Void> {

        @Override
        protected Void doInBackground(Item... items) {
            List<Item> toAdd = Arrays.asList(items);
            databaseHandler.addItems(toAdd);
            return null;
        }
    }
}
