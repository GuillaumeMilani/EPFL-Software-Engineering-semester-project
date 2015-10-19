package ch.epfl.sweng.calamar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.epfl.sweng.calamar.SimpleTextItem;

//TODO Support other item types

/**
 * This activity manages the chat between two users (or in a group)
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText messageText;
    private Button sendButton;
    private Button refreshButton;
    private ArrayList<SimpleTextItem> messagesHistory;
    private ListView messagesContainer;
    private ChatAdapter adapter;

    private ItemClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageText = (EditText) findViewById(R.id.messageEdit);
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

        refresh();
    }

    /**
     * Gets all messages and display them
     */
    private void refresh() {
        //TODO only get new messages without clearing
        messagesHistory = new ArrayList<>(client.getAllSimpleTextItems());
        adapter.add(messagesHistory);
        adapter.notifyDataSetChanged();
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    /**
     * Sends a new message
     */
    private void send() {
        //TODO do something with messageText
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

}
