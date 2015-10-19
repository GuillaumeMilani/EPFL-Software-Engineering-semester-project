package ch.epfl.sweng.calamar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.SimpleTextItem;

//TODO Support other item types

/**
 * This activity manages the chat between two users (or in a group)
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText messageText;
    private Button sendButton;
    private Button refreshButton;
    private List<Item> messagesHistory;
    private ListView messagesContainer;
    private ChatAdapter adapter;

    private ItemClient client;

    private User actualUser = new User(2,"Bob");
    private User correspondent;

    private Date lastRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        correspondent = new User(1,"Alice");
        lastRefresh = new Date(0);

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
        try {
            messagesHistory = client.getAllItems(actualUser,lastRefresh);
            adapter.add(messagesHistory);
            //adapter.add(new SimpleTextItem(1,actualUser,correspondent,new Date(),"blabla"));
            adapter.notifyDataSetChanged();
            messagesContainer.setSelection(messagesContainer.getCount() - 1);
        } catch (ItemClientException e) {
            //TODO : Toast
            e.printStackTrace();
        }
    }

    /**
     * Sends a new message
     */
    private void send() {
        EditText editText = (EditText)findViewById(R.id.messageEdit);
        String message = editText.getText().toString();
        //TODO : Determine id of the message ?
        Item textMessage = new SimpleTextItem(1,actualUser,correspondent,new Date(),message);
        try {
            client.send(textMessage);
        } catch (ItemClientException e) {
            //TODO: Toast
            e.printStackTrace();
        }
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
