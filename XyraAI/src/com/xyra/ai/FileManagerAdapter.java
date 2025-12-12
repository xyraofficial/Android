package com.xyra.ai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FileManagerAdapter extends BaseAdapter {
    
    public interface OnItemClickListener {
        void onFolderClick(FolderItem folder);
        void onFileClick(FileItem file);
        void onItemLongClick(Object item, View view);
    }
    
    private Context context;
    private List<Object> items;
    private OnItemClickListener listener;
    private int indentLevel = 0;
    
    public FileManagerAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setItems(List<FolderItem> folders, List<FileItem> files) {
        items.clear();
        if (folders != null) items.addAll(folders);
        if (files != null) items.addAll(files);
        notifyDataSetChanged();
    }
    
    public void buildTree(FolderItem rootFolder, int level) {
        if (rootFolder == null) return;
        
        if (level == 0) {
            items.clear();
        }
        
        for (FolderItem folder : rootFolder.getSubFolders()) {
            folder.setPath(String.valueOf(level));
            items.add(folder);
            if (folder.isExpanded()) {
                buildTree(folder, level + 1);
            }
        }
        
        for (FileItem file : rootFolder.getFiles()) {
            items.add(file);
        }
        
        if (level == 0) {
            notifyDataSetChanged();
        }
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_file_manager, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.ivIcon);
            holder.name = convertView.findViewById(R.id.tvName);
            holder.info = convertView.findViewById(R.id.tvInfo);
            holder.container = convertView.findViewById(R.id.itemContainer);
            holder.expandIcon = convertView.findViewById(R.id.ivExpand);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Object item = items.get(position);
        
        if (item instanceof FolderItem) {
            FolderItem folder = (FolderItem) item;
            holder.name.setText(folder.getName());
            holder.icon.setImageResource(R.drawable.ic_folder);
            holder.info.setText(folder.getTotalItems() + " items");
            holder.expandIcon.setVisibility(View.VISIBLE);
            holder.expandIcon.setRotation(folder.isExpanded() ? 90 : 0);
            
            int level = 0;
            try {
                level = Integer.parseInt(folder.getPath());
            } catch (Exception e) {}
            holder.container.setPadding(level * 32 + 16, 8, 16, 8);
            
            holder.container.setOnClickListener(v -> {
                if (listener != null) listener.onFolderClick(folder);
            });
        } else if (item instanceof FileItem) {
            FileItem file = (FileItem) item;
            holder.name.setText(file.getFullName());
            holder.icon.setImageResource(getFileIcon(file.getExtension()));
            holder.info.setText(formatSize(file.getSize()));
            holder.expandIcon.setVisibility(View.GONE);
            holder.container.setPadding(48, 8, 16, 8);
            
            holder.container.setOnClickListener(v -> {
                if (listener != null) listener.onFileClick(file);
            });
        }
        
        final Object finalItem = item;
        holder.container.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(finalItem, v);
            return true;
        });
        
        return convertView;
    }
    
    private int getFileIcon(String extension) {
        if (extension == null) return R.drawable.ic_file;
        
        switch (extension.toLowerCase()) {
            case "py":
                return R.drawable.ic_file_python;
            case "js":
            case "ts":
                return R.drawable.ic_file_js;
            case "java":
            case "kt":
                return R.drawable.ic_file_java;
            case "html":
                return R.drawable.ic_file_html;
            case "css":
                return R.drawable.ic_file_css;
            case "json":
                return R.drawable.ic_file_json;
            case "dart":
                return R.drawable.ic_file_dart;
            case "md":
                return R.drawable.ic_file_md;
            default:
                return R.drawable.ic_file;
        }
    }
    
    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
    
    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView info;
        LinearLayout container;
        ImageView expandIcon;
    }
}
