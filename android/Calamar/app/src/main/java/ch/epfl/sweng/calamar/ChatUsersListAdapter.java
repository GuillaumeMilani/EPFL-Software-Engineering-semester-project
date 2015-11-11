package ch.epfl.sweng.calamar;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatUsersListAdapter extends BaseAdapter {
    private final ArrayList<Recipient> users;
    private final Activity context;

    public ChatUsersListAdapter(Activity context, List<Recipient> users){
        this.users=new ArrayList<>(users);
        this.context=context;
    }
    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Recipient getItem(int position) {
        return users.get(position);
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

        holder.userName.setText(user.getName());
        return convertView;
    }

    public void add(User user){
        users.add(user);
    }

    /**
     * Creates a ViewHolder containing the name of the user, and one LinearLayout containing it.
     * @param v The view holding those values
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
}
