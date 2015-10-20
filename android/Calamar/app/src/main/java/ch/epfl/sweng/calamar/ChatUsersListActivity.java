package ch.epfl.sweng.calamar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatUsersListActivity extends AppCompatActivity {

    private ListView contactsView;
    private ArrayList<User> contacts;
    private ChatUsersListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        //TODO store contacts locally?
        contacts=new ArrayList<>(provider.getContacts(ChatActivity.actualUser));

        contactsView = (ListView) findViewById(R.id.contactsList);
        adapter=new ChatUsersListAdapter(this,contacts);
        contactsView.setAdapter(adapter);
        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent conversation = new Intent(ChatUsersListActivity.this, ChatActivity.class);
                //Assuming in same order
                User user = contacts.get(position);
                conversation.putExtra("userName",user.getName());
                conversation.putExtra("userID",user.getID());
            }
        });
        contactsView.setSelection(0);
    }

}
