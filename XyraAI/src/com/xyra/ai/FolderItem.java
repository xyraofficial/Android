package com.xyra.ai;

import java.util.ArrayList;
import java.util.List;

public class FolderItem {
    private String id;
    private String parentId;
    private String name;
    private String path;
    private long createdAt;
    private long updatedAt;
    private boolean isExpanded;
    private List<FolderItem> subFolders;
    private List<FileItem> files;
    
    public FolderItem() {
        this.subFolders = new ArrayList<>();
        this.files = new ArrayList<>();
        this.isExpanded = false;
    }
    
    public FolderItem(String id, String parentId, String name) {
        this();
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
    
    public List<FolderItem> getSubFolders() { return subFolders; }
    public void setSubFolders(List<FolderItem> subFolders) { this.subFolders = subFolders; }
    
    public List<FileItem> getFiles() { return files; }
    public void setFiles(List<FileItem> files) { this.files = files; }
    
    public void addSubFolder(FolderItem folder) {
        if (subFolders == null) subFolders = new ArrayList<>();
        subFolders.add(folder);
    }
    
    public void addFile(FileItem file) {
        if (files == null) files = new ArrayList<>();
        files.add(file);
    }
    
    public int getTotalItems() {
        int count = 0;
        if (files != null) count += files.size();
        if (subFolders != null) count += subFolders.size();
        return count;
    }
}
