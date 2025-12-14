package com.xyra.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<JSONObject> users;
    private UserActionListener listener;
    
    public interface UserActionListener {
        void onBanUser(JSONObject user, boolean ban);
        void onDeleteUser(JSONObject user);
    }
    
    public UserAdapter(Context context, ArrayList<JSONObject> users, UserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }
    
    @Override
    public int getCount() {
        return users.size();
    }
    
    @Override
    public Object getItem(int position) {
        return users.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        }
        
        final JSONObject user = users.get(position);
        
        ImageView ivAvatar = convertView.findViewById(R.id.ivAvatar);
        TextView tvEmail = convertView.findViewById(R.id.tvEmail);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        Button btnBan = convertView.findViewById(R.id.btnBan);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);
        
        String email = user.optString("email", "Unknown");
        boolean isBanned = user.optBoolean("banned", false);
        
        tvEmail.setText(email);
        
        if (isBanned) {
            tvStatus.setText("Banned");
            tvStatus.setBackgroundResource(R.drawable.bg_status_banned);
            btnBan.setText("Unban");
            btnBan.setBackgroundResource(R.drawable.bg_button_warning);
        } else {
            tvStatus.setText("Active");
            tvStatus.setBackgroundResource(R.drawable.bg_status_active);
            btnBan.setText("Ban");
            btnBan.setBackgroundResource(R.drawable.bg_button_secondary);
        }
        
        btnBan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onBanUser(user, !user.optBoolean("banned", false));
                }
            }
        });
        
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteUser(user);
                }
            }
        });
        
        return convertView;
    }
}
