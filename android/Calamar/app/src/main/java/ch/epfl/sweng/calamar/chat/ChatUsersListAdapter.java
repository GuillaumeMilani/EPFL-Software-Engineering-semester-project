package ch.epfl.sweng.calamar.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;

import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.recipient.Recipient;

public final class ChatUsersListAdapter extends BaseAdapter {
    private final HighlightMap<Recipient,Boolean> users;
    private final Activity context;

    public ChatUsersListAdapter(Activity context, List<Recipient> users) {
        this.users = new HighlightMap<>(users,false);
        this.context = context;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Recipient getItem(int position) {
        return users.getAtIndex(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Recipient user = getItem(position);
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = li.inflate(R.layout.list_contacts_chat, null);

            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(users.get(user)) {
            holder.userName.setTextColor(Color.GREEN);
        }
        else {
            holder.userName.setTextColor(Color.BLACK);
        }

        holder.userName.setText(user.getName());

        return convertView;
    }

    public void highlight(Recipient user,Boolean state)
    {
        users.put(user,state);
    }

    /**
     * Adds an user to the list with default highlight value to false
     *
     * @param user The User to add
     */
    public void add(Recipient user) {
        users.put(user,false);
    }

    /**
     * Adds an user to the list
     *
     * @param user The User to add
     * @param highlight The value if the user is highlight
     */
    public void add(Recipient user,Boolean highlight) {
        users.put(user, highlight);
    }

    /**
     * Creates a ViewHolder containing the name of the user, and one LinearLayout containing it.
     *
     * @param v The itemView holding those values
     * @return The newly created ViewHolder
     */
    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.userName = (TextView) v.findViewById(R.id.userName);
        return holder;

    }

    /**
     * Avoids using findViewById too much (more efficient and readable)
     */
    private static class ViewHolder {
        public TextView userName;
    }

    /**
     * LinkedMap to preserve the order, mendatory for a list
     * @param <K> Key
     * @param <V> Value which can be set by default
     */
    private class HighlightMap<K,V> extends LinkedHashMap<K,V>
    {
        public HighlightMap(List<K> users,V defaultValue) {
            for (K user : users) {
                this.put(user,defaultValue);
            }
        }

        public K getAtIndex(int index) {
            return (K) this.keySet().toArray()[index];
        }
    }
}
