package ch.epfl.sweng.calamar.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.calamar.BaseActivity;
import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

// TODO: Clean up code and organize methods
public class ChatFragment extends android.support.v4.app.Fragment {

    public final static String EXTRA_CORRESPONDENT_NAME = "ch.epfl.sweng.calamar.CORRESPONDENT_NAME";
    public final static String EXTRA_CORRESPONDENT_ID = "ch.epfl.sweng.calamar.CORRESPONDENT_ID";
    private static final String TAG = ChatFragment.class.getSimpleName();

    private ListView contactsView;
    private List<Recipient> contacts;
    private ChatUsersListAdapter adapter;
    private TextView actualUserTextView;

    private CalamarApplication app;

    private Dialog newContactAlertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_chat_users_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        app = CalamarApplication.getInstance();

        contacts = new ArrayList<>();
        getContacts();

        actualUserTextView = (TextView) getView().findViewById(R.id.actualUserName);
      //  setActualUser();

        contactsView = (ListView) getView().findViewById(R.id.contactsList);
        contactsView.setSelector(R.drawable.list_selector);
        adapter = new ChatUsersListAdapter(getActivity(), contacts);
        contactsView.setAdapter(adapter);
        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent conversation = new Intent(getActivity(), ChatActivity.class);
                //Assuming in same order
                Recipient user = contacts.get(position);
                conversation.putExtra(EXTRA_CORRESPONDENT_NAME, user.getName());

                conversation.putExtra(EXTRA_CORRESPONDENT_ID, user.getID());
                startActivity(conversation);
            }
        });
        contactsView.setSelection(0);

        (getView().findViewById(R.id.newContact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewContact();
            }
        });

        // Create BroadCastReceiver
        getContext().registerReceiver(new ChatBroadcastReceiver(),new IntentFilter("ch.epfl.sweng.UPDATE_INTENT"));

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Called by button OnClickListener
     */
    public void addContact() {
        EditText input = (EditText) newContactAlertDialog.findViewById(R.id.newContactInput);
        newContactAlertDialog.dismiss();
        new retrieveUserTask(input.getText().toString(), (BaseActivity) getActivity()).execute();
    }

    /**
     * Called by button OnClickListener
     */
    public void cancelNewContact() {
        newContactAlertDialog.dismiss();
    }

    /**
     * prints the actual user of the app on textview.
     * @deprecated to be removed
     */
    public void setActualUser() {
        if (!app.getCurrentUserName().equals("")) {
            actualUserTextView.setText("Actual user : " + app.getCurrentUserName());
        } else {
            // TODO ok ???
           // getActivity().finish();
        }
    }

    private void getContacts() {
        contacts.addAll(app.getDatabaseHandler().getAllRecipients());
        contacts.remove(app.getCurrentUser());
    }

    private void addNewContact() {
        newContactAlertDialog = new Dialog(getActivity());

        newContactAlertDialog.setContentView(R.layout.create_new_contact);
        newContactAlertDialog.setTitle(getString(R.string.add_new_contact_title));
        newContactAlertDialog.findViewById(R.id.newContactAddButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });

        newContactAlertDialog.findViewById(R.id.newContactCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNewContact();
            }
        });
        newContactAlertDialog.show();
    }

    /**
     * Add the user in the contact list
     */
    private void addUserInContact(User user)
    {
        adapter.add(user);
        contacts.add(user);
        adapter.notifyDataSetChanged();
        //Add in memory
        app.getDatabaseHandler().addRecipient(user);
    }

    /**
     * Async task for retrieving a new user.
     */
    private class retrieveUserTask extends AsyncTask<Void, Void, User> {

        private String name = null;
        private final BaseActivity context;

        public retrieveUserTask(String name, BaseActivity context) {
            this.name = name;
            this.context = context;
        }

        @Override
        protected User doInBackground(Void... v) {
            try {
                return DatabaseClientLocator.getDatabaseClient().findUserByName(name);
            } catch (DatabaseClientException e) {
                Log.e(ChatFragment.TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(User newUser) {
            if (newUser != null) {
                //add the user in the contact list
                addUserInContact(newUser);
            } else {
                if(isAdded()) {
                    context.displayErrorMessage(getString(R.string.add_new_contact_impossible), false);
                }
            }
        }
    }

    /**
     *  Receive Broadcast Message and update chat accordingly
     */
    public class ChatBroadcastReceiver extends BroadcastReceiver {

        public final static String BROADCAST_EXTRA_USER = "user";
        public final static String BROADCAST_EXTRA_ID = "id";

        @Override
        public void onReceive(Context context, Intent intent) {
            // retrieve the user data
            User user = new User(Integer.valueOf(intent.getStringExtra(BROADCAST_EXTRA_ID)),
                                intent.getStringExtra(BROADCAST_EXTRA_USER));
            //add the user in the contact list
            addUserInContact(user);
        }
    }

    public ChatFragment() {
        // Required empty public constructor
    }


}
