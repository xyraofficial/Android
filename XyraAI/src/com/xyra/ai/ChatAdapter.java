package com.xyra.ai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends BaseAdapter {
    
    private static final int TYPE_USER = 0;
    private static final int TYPE_AI = 1;
    
    private Context context;
    private List<Message> messages;
    private LayoutInflater inflater;
    private SimpleDateFormat timeFormat;
    
    public ChatAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<Message>();
        this.inflater = LayoutInflater.from(context);
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @Override
    public int getCount() {
        return messages.size();
    }
    
    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = messages.get(position);
        int viewType = getItemViewType(position);
        
        ViewHolder holder;
        
        if (convertView == null) {
            holder = new ViewHolder();
            
            if (viewType == TYPE_USER) {
                convertView = inflater.inflate(R.layout.item_message_user, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.item_message_ai, parent, false);
            }
            
            holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        if (viewType == TYPE_AI) {
            CharSequence formattedText = MarkdownParser.parse(message.getContent());
            holder.tvMessage.setText(formattedText);
        } else {
            holder.tvMessage.setText(message.getContent());
        }
        
        holder.tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        
        return convertView;
    }
    
    public void addMessage(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }
    
    public void updateLastMessage(String content) {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            messages.get(lastIndex).setContent(content);
            notifyDataSetChanged();
        }
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    public void setMessages(List<Message> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }
    
    static class ViewHolder {
        TextView tvMessage;
        TextView tvTime;
    }
}
