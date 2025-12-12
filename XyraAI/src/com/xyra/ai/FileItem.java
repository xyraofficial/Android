package com.xyra.ai;

public class FileItem {
    private String id;
    private String folderId;
    private String name;
    private String extension;
    private String content;
    private String language;
    private long size;
    private long createdAt;
    private long updatedAt;
    private boolean isBinary;
    
    public FileItem() {}
    
    public FileItem(String id, String folderId, String name, String extension, String content) {
        this.id = id;
        this.folderId = folderId;
        this.name = name;
        this.extension = extension;
        this.content = content;
        this.language = detectLanguage(extension);
        this.size = content != null ? content.length() : 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isBinary = false;
    }
    
    public static String detectLanguage(String extension) {
        if (extension == null) return "text";
        switch (extension.toLowerCase()) {
            case "py": return "python";
            case "js": return "javascript";
            case "ts": return "typescript";
            case "java": return "java";
            case "kt": return "kotlin";
            case "dart": return "dart";
            case "html": return "html";
            case "css": return "css";
            case "json": return "json";
            case "xml": return "xml";
            case "md": return "markdown";
            case "sql": return "sql";
            case "php": return "php";
            case "rb": return "ruby";
            case "go": return "go";
            case "rs": return "rust";
            case "c": return "c";
            case "cpp": case "cc": case "cxx": return "cpp";
            case "h": case "hpp": return "cpp";
            case "swift": return "swift";
            case "sh": case "bash": return "bash";
            case "yaml": case "yml": return "yaml";
            default: return "text";
        }
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getExtension() { return extension; }
    public void setExtension(String extension) { 
        this.extension = extension;
        this.language = detectLanguage(extension);
    }
    
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content;
        this.size = content != null ? content.length() : 0;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isBinary() { return isBinary; }
    public void setBinary(boolean binary) { isBinary = binary; }
    
    public String getFullName() {
        if (extension != null && !extension.isEmpty()) {
            return name + "." + extension;
        }
        return name;
    }
}
