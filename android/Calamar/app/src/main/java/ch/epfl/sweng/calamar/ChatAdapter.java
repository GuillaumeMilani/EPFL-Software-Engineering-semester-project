package ch.epfl.sweng.calamar;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.calamar.SimpleTextItem;

//TODO : Support other item types

public class ChatAdapter extends BaseAdapter {

    private final List<SimpleTextItem> messages;
    private final Activity context;

    public ChatAdapter(Activity context, List<Item> messages) {
        assert (context != null && messages != null);
        this.context = context;
        this.messages = new ArrayList<>();

        //Add the items
        for(Item i : messages){
            addItem(i);
        }
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public SimpleTextItem getItem(final int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        SimpleTextItem item = getItem(position);
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = li.inflate(R.layout.list_chat_messages, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //TODO : how to check outgoing or not?
        //boolean outgoing = item.getFrom().id==myId
        boolean outgoing = true;
        setAlignment(holder, outgoing);
        holder.textMessage.setText(item.getMessage());
        holder.textTime.setText(item.getDate().toString());
        return convertView;
    }

    /**
     * Add a message to the adapter
     * @param message the message to be added
     */
    public void add(Item message) {
        addItem(message);
    }

    /**
     * Add a list of messages to the adapter
     * @param messages the list of messages
     */
    public void add(List<Item> messages) {
        for(Item i : messages) {
            addItem(i);
        }
    }

    private void addItem(Item i){
        //TODO : Identify type (with an enum in item ? )
        if(i.getClass() == SimpleTextItem.class){
            this.messages.add((SimpleTextItem)i);
        }
    }

    /**
     * Set the alignment of the messages, depending on if the message is outgoing or ingoing.
     * @param holder The ViewHolder containing the necessary attributes
     * @param outgoing True if the message is sent by the user, false if it is received.
     */
    private void setAlignment(ViewHolder holder, boolean outgoing) {
        if (outgoing) {
            holder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams rLayoutParams = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            holder.content.setLayoutParams(rLayoutParams);
            layoutParams = (LinearLayout.LayoutParams) holder.textMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.textMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.textTime.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.textTime.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams rLayoutParams =
                    (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(rLayoutParams);
            layoutParams = (LinearLayout.LayoutParams) holder.textMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.textMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.textTime.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.textTime.setLayoutParams(layoutParams);
        }
    }

    /**
     * Creates a ViewHolder containing the text of the message, the time, and two LinearLayout
     * (one for the whole message, and one for the text, contained in a "bubble")
     * @param v The view holding those values
     * @return The newly created ViewHolder
     */
    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.textMessage = (TextView) v.findViewById(R.id.textMessage);
        holder.textTime = (TextView) v.findViewById(R.id.textTime);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBG);
        return holder;

    }

    /**
     * Avoids using findViewById too much (more efficient and readable)
     */
    private static class ViewHolder {
        public TextView textMessage;
        public TextView textTime;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }

}
