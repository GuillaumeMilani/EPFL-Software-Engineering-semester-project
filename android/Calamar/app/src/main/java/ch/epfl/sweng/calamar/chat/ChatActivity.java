package ch.epfl.sweng.calamar.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.BaseActivity;
import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.SQLiteDatabaseHandler;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.item.CreateItemActivity;
import ch.epfl.sweng.calamar.item.FileItem;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.StorageCallbacks;
import ch.epfl.sweng.calamar.utils.StorageManager;

/**
 * This activity manages the chat between two users (or in a group)
 */

public final class ChatActivity extends BaseActivity implements StorageCallbacks {

    private static final String TAG = ChatActivity.class.getSimpleName();


    private EditText editText;
    private Button sendButton;
    private ListView messagesContainer;
    private ChatAdapter adapter;

    private Recipient correspondent;

    private StorageManager storageManager;
    private SQLiteDatabaseHandler dbHandler;
    private CalamarApplication app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String correspondentName = intent.getStringExtra(ChatFragment.EXTRA_CORRESPONDENT_NAME);
        int correspondentID = intent.getIntExtra(ChatFragment.EXTRA_CORRESPONDENT_ID, Recipient.DEFAULT_ID);

        if (correspondentName == null) {
            correspondentName = getString(R.string.empty_string);
        }

        app = CalamarApplication.getInstance();
        correspondent = new User(correspondentID, correspondentName);

        sendButton = (Button) findViewById(R.id.chatSendButton);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextItem();
            }
        });

        editText = (EditText) findViewById(R.id.messageEdit);

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    sendButton.setEnabled(false);
                } else {
                    sendButton.setEnabled(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        final Button refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh(false);
            }
        });

        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        adapter = new ChatAdapter(this);
        messagesContainer.setAdapter(adapter);

        messagesContainer.setOnItemClickListener(new ItemClickWithStorageCallbackListener());

        TextView recipient = (TextView) findViewById(R.id.recipientLabel);
        recipient.setText(correspondent.getName());

        storageManager = app.getStorageManager();
        dbHandler = app.getDatabaseHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Save sent messages
        app.new ApplyPendingDatabaseOperationsTask().execute();
    }

    /**
     * Gets all messages and display them
     */
    private void refresh(boolean offline) {
        new RefreshTask(app.getCurrentUser(), offline, this).execute();
    }

    /**
     * Sends a new text message
     */
    private void sendTextItem() {
        String message = editText.getText().toString();
        Item textMessage = new SimpleTextItem(Item.DUMMY_ID, app.getCurrentUser(), correspondent, new Date(), message);
        adapter.notifyDataSetChanged();
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
        editText.setText(getString(R.string.empty_string));
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
                Log.e(ChatActivity.TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Item item) {
            if (item != null) {
                editText.setText("");
                adapter.add(item);
                messagesContainer.setSelection(messagesContainer.getCount() - 1);
                storageManager.storeItem(item, ChatActivity.this);
            } else {
                displayErrorMessage(getString(R.string.item_send_error), false);
            }
        }
    }

    /**
     * Async task for refreshing / getting new messages.
     */
    private class RefreshTask extends AsyncTask<Void, Void, List<Item>> {

        private final Recipient recipient;
        private final boolean offline;
        private final Activity context;

        public RefreshTask(Recipient recipient, boolean offline, Activity context) {
            if (null == recipient || null == context) {
                throw new IllegalArgumentException(getString(R.string.refreshtask_null));
            }
            this.context = context;
            this.recipient = recipient;
            this.offline = offline;
        }

        @Override
        protected List<Item> doInBackground(Void... v) {
            if (offline) {
                return dbHandler.getItemsForContact(correspondent);
            } else {
                try {
                    return DatabaseClientLocator.getDatabaseClient().getAllItems(recipient, new Date(dbHandler.getLastItemTime()));
                } catch (DatabaseClientException e) {
                    Log.e(ChatActivity.TAG, e.getMessage());
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            if (items != null) {
                if (!items.isEmpty()) {
                    if (!offline) {
                        storageManager.storeItems(items, ChatActivity.this);
                        dbHandler.setLastItemTime(items.get(items.size() - 1).getDate().getTime());
                    }
                    adapter.add(items);
                    for (Item item : items) {
                        storageManager.getCompleteItem(item, ChatActivity.this);
                    }
                    messagesContainer.setSelection(messagesContainer.getCount() - 1);
                }
                Toast.makeText(context, getString(R.string.refresh_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                displayErrorMessage(getString(R.string.unable_to_refresh_message), false);
            }
        }

    }

    /**
     * Returns a copy of the messages history
     *
     * @return the messages history
     */
    public List<Item> getHistory() {
        return adapter.getHistory();
    }

    /**
     * Updates the item in the messages history
     *
     * @param item the item to be updated
     */
    @Override
    public void onItemRetrieved(Item item) {
        boolean notFound = true;
        for (int i = adapter.getCount() - 1; i >= 0 && notFound; --i) {
            if (item.getID() == adapter.getItem(i).getID()) {
                adapter.set(item, i);
                notFound = false;
            }
        }
    }

    /**
     * Does nothing, will never ask the StorageManager for only data.
     *
     * @param data the data
     */
    @Override
    public void onDataRetrieved(byte[] data) {
        //Does nothing
    }

    public void createItem(View v) {
        Intent intent = new Intent(this, CreateItemActivity.class);
        intent.putExtra(CreateItemActivity.CREATE_ITEM_RECIPIENT_EXTRA_ID, correspondent.getID());
        intent.putExtra(CreateItemActivity.CREATE_ITEM_RECIPIENT_EXTRA_NAME, correspondent.getName());
        startActivity(intent);
    }

    public void clearChat() {
        adapter.clear();
    }

    private class ItemClickWithStorageCallbackListener implements AdapterView.OnItemClickListener, StorageCallbacks {

        private AlertDialog dialog;
        private Item item;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            item = adapter.getItem(position);

            AlertDialog.Builder itemDescription = new AlertDialog.Builder(ChatActivity.this);
            itemDescription.setTitle(R.string.item_details_alertDialog_title);

            itemDescription.setPositiveButton(R.string.alert_dialog_default_positive_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //OK
                }
            });

            itemDescription.setView(item.getView(ChatActivity.this));

            //Theoretically not needed : storageManager.getCompleteItem(item,this);

            dialog = itemDescription.show();
        }

        @Override
        public void onItemRetrieved(Item i) {
            item = i;
            if (dialog != null) {
                dialog.setView(item.getView(ChatActivity.this));
            }
        }

        @Override
        public void onDataRetrieved(byte[] data) {
            switch (item.getType()) {
                case SIMPLETEXTITEM:
                    break;
                case FILEITEM:
                    item = new FileItem(item.getID(), item.getFrom(), item.getTo(), item.getDate(), item.getCondition(), data, ((FileItem) item).getPath());
                    break;
                case IMAGEITEM:
                    item = new ImageItem(item.getID(), item.getFrom(), item.getTo(), item.getDate(), item.getCondition(), data, ((ImageItem) item).getPath());
                    break;
                default:
                    throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.unexpected_item_type, item.getType().name()));
            }
            dialog.setView(item.getView(ChatActivity.this));
        }
    }
}
