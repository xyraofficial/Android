package com.xyra.ai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends BaseAdapter {
    
    private Context context;
    private List<ChatHistory.ChatItem> items;
    private LayoutInflater inflater;
    private SimpleDateFormat dateFormat;
    private boolean deleteMode = false;
    private OnChatItemClickListener listener;
    
    public interface OnChatItemClickListener {
        void onChatClick(ChatHistory.ChatItem item);
        void onChatDelete(ChatHistory.ChatItem item);
    }
    
    public ChatHistoryAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<ChatHistory.ChatItem>();
        this.inflater = LayoutInflater.from(context);
        this.dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
    }
    
    public void setItems(List<ChatHistory.ChatItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;
        notifyDataSetChanged();
    }
    
    public boolean isDeleteMode() {
        return deleteMode;
    }
    
    public void setOnChatItemClickListener(OnChatItemClickListener listener) {
        this.listener = listener;
    }
    
    @Override
    public int getCount() {
        return items.size();
    }
    
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_chat_history, parent, false);
            holder = new ViewHolder();
            holder.tvPreview = (TextView) convertView.findViewById(R.id.tvChatPreview);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvChatTime);
            holder.btnDelete = (ImageButton) convertView.findViewById(R.id.btnDeleteChat);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        final ChatHistory.ChatItem item = items.get(position);
        
        holder.tvPreview.setText(item.preview);
        holder.tvTime.setText(dateFormat.format(new Date(item.timestamp)));
        
        if (deleteMode) {
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
        
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChatClick(item);
                }
            }
        });
        
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onChatDelete(item);
                }
                return true;
            }
        });
        
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChatDelete(item);
                }
            }
        });
        
        return convertView;
    }
    
    static class ViewHolder {
        TextView tvPreview;
        TextView tvTime;
        ImageButton btnDelete;
    }
}
