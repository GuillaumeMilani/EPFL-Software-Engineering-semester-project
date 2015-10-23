package ch.epfl.sweng.calamar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ChatUsersListActivity extends AppCompatActivity {

    public final static String EXTRA_CORRESPONDENT_NAME = "ch.epfl.sweng.calamar.CORRESPONDENT_NAME";
    public final static String EXTRA_CORRESPONDENT_ID = "ch.epfl.sweng.calamar.CORRESPONDENT_ID";

    private ListView contactsView;
    private List<User> contacts;
    private ChatUsersListAdapter adapter;

    public static User actualUser = new User(1,"Alice");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_list);

        contacts = new ArrayList<>();
        getContacts();

        contactsView = (ListView) findViewById(R.id.contactsList);
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

    private void getContacts(){
        //TODO : Store contact ?
        contacts.add(new User(2,"Bob"));
        contacts.add(new User(3,"Carol"));
        contacts.add(new User(4,"Denis"));
        contacts.add(new User(5,"Eve"));
    }

}
