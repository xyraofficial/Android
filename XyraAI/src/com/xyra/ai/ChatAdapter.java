package com.xyra.ai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
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
    private MessageRenderer messageRenderer;
    
    public ChatAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<Message>();
        this.inflater = LayoutInflater.from(context);
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.messageRenderer = new MessageRenderer(context);
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
        
        if (viewType == TYPE_USER) {
            return getUserView(message, convertView, parent);
        } else {
            return getAIView(message, convertView, parent);
        }
    }
    
    private View getUserView(Message message, View convertView, ViewGroup parent) {
        UserViewHolder holder;
        
        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof UserViewHolder)) {
            convertView = inflater.inflate(R.layout.item_message_user, parent, false);
            holder = new UserViewHolder();
            holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            convertView.setTag(holder);
        } else {
            holder = (UserViewHolder) convertView.getTag();
        }
        
        holder.tvMessage.setText(message.getContent());
        holder.tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        
        return convertView;
    }
    
    private View getAIView(Message message, View convertView, ViewGroup parent) {
        AIViewHolder holder;
        
        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof AIViewHolder)) {
            convertView = inflater.inflate(R.layout.item_message_ai, parent, false);
            holder = new AIViewHolder();
            holder.messageContainer = (LinearLayout) convertView.findViewById(R.id.messageContainer);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            convertView.setTag(holder);
        } else {
            holder = (AIViewHolder) convertView.getTag();
        }
        
        String content = message.getContent();
        
        if (content.equals("Thinking...") || content.equals("Sedang berpikir...")) {
            holder.messageContainer.removeAllViews();
            TextView tvThinking = new TextView(context);
            tvThinking.setText(content);
            tvThinking.setTextColor(0xFFFCD34D);
            tvThinking.setTextSize(15);
            holder.messageContainer.addView(tvThinking);
        } else {
            messageRenderer.renderMessage(content, holder.messageContainer);
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
    
    static class UserViewHolder {
        TextView tvMessage;
        TextView tvTime;
    }
    
    static class AIViewHolder {
        LinearLayout messageContainer;
        TextView tvTime;
    }
}
