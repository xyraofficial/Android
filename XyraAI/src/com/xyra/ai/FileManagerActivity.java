package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileManagerActivity extends Activity {
    
    private static final String PREFS_NAME = "XyraAIFiles";
    
    private ImageButton btnBack;
    private ImageButton btnNewFolder;
    private ImageButton btnNewFile;
    private ImageButton btnMore;
    private TextView tvTitle;
    private TextView tvRootPath;
    private ListView fileList;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private LinearLayout breadcrumbs;
    
    private FileManagerAdapter adapter;
    private List<FolderItem> folders;
    private List<FileItem> files;
    private FolderItem currentFolder;
    private List<FolderItem> folderStack;
    
    private SharedPreferences prefs;
    private SupabaseService supabaseService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_file_manager);
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        supabaseService = new SupabaseService(this);
        
        initViews();
        setupAdapter();
        setupClickListeners();
        loadRootFolder();
    }
    
    private void initViews() {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnNewFolder = (ImageButton) findViewById(R.id.btnNewFolder);
        btnNewFile = (ImageButton) findViewById(R.id.btnNewFile);
        btnMore = (ImageButton) findViewById(R.id.btnMore);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvRootPath = (TextView) findViewById(R.id.tvRootPath);
        fileList = (ListView) findViewById(R.id.fileList);
        emptyState = (LinearLayout) findViewById(R.id.emptyState);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        breadcrumbs = (LinearLayout) findViewById(R.id.breadcrumbs);
        
        folders = new ArrayList<>();
        files = new ArrayList<>();
        folderStack = new ArrayList<>();
    }
    
    private void setupAdapter() {
        adapter = new FileManagerAdapter(this);
        fileList.setAdapter(adapter);
        
        adapter.setOnItemClickListener(new FileManagerAdapter.OnItemClickListener() {
            @Override
            public void onFolderClick(FolderItem folder) {
                openFolder(folder);
            }
            
            @Override
            public void onFileClick(FileItem file) {
                openFile(file);
            }
            
            @Override
            public void onItemLongClick(Object item, View view) {
                showContextMenu(item, view);
            }
        });
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        btnNewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateFolderDialog();
            }
        });
        
        btnNewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateFileDialog();
            }
        });
        
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreMenu(v);
            }
        });
        
        tvRootPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRoot();
            }
        });
    }
    
    private void loadRootFolder() {
        currentFolder = new FolderItem("root", null, "My Files");
        folderStack.clear();
        folderStack.add(currentFolder);
        
        loadLocalFiles();
        updateUI();
    }
    
    private void loadLocalFiles() {
        String foldersJson = prefs.getString("folders_" + currentFolder.getId(), "");
        String filesJson = prefs.getString("files_" + currentFolder.getId(), "");
        
        folders.clear();
        files.clear();
        
        if (!TextUtils.isEmpty(foldersJson)) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(foldersJson);
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.getJSONObject(i);
                    FolderItem folder = new FolderItem(
                        obj.getString("id"),
                        obj.optString("parentId", null),
                        obj.getString("name")
                    );
                    folders.add(folder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (!TextUtils.isEmpty(filesJson)) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(filesJson);
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.getJSONObject(i);
                    FileItem file = new FileItem(
                        obj.getString("id"),
                        obj.optString("folderId", null),
                        obj.getString("name"),
                        obj.optString("extension", ""),
                        obj.optString("content", "")
                    );
                    files.add(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        adapter.setItems(folders, files);
    }
    
    private void saveLocalFiles() {
        try {
            org.json.JSONArray foldersArr = new org.json.JSONArray();
            for (FolderItem folder : folders) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("id", folder.getId());
                obj.put("parentId", folder.getParentId());
                obj.put("name", folder.getName());
                foldersArr.put(obj);
            }
            
            org.json.JSONArray filesArr = new org.json.JSONArray();
            for (FileItem file : files) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("id", file.getId());
                obj.put("folderId", file.getFolderId());
                obj.put("name", file.getName());
                obj.put("extension", file.getExtension());
                obj.put("content", file.getContent());
                filesArr.put(obj);
            }
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("folders_" + currentFolder.getId(), foldersArr.toString());
            editor.putString("files_" + currentFolder.getId(), filesArr.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateUI() {
        if (folders.isEmpty() && files.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            fileList.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            fileList.setVisibility(View.VISIBLE);
        }
        
        updateBreadcrumbs();
    }
    
    private void updateBreadcrumbs() {
        StringBuilder path = new StringBuilder("/");
        for (int i = 1; i < folderStack.size(); i++) {
            path.append(folderStack.get(i).getName()).append("/");
        }
        tvRootPath.setText(path.toString());
    }
    
    private void openFolder(FolderItem folder) {
        folderStack.add(folder);
        currentFolder = folder;
        loadLocalFiles();
        updateUI();
    }
    
    private void openFile(FileItem file) {
        Intent intent = new Intent(this, CodeEditorActivity.class);
        intent.putExtra("file_id", file.getId());
        intent.putExtra("file_name", file.getFullName());
        intent.putExtra("file_content", file.getContent());
        intent.putExtra("file_language", file.getLanguage());
        intent.putExtra("folder_id", currentFolder.getId());
        startActivityForResult(intent, 100);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String fileId = data.getStringExtra("file_id");
            String newContent = data.getStringExtra("file_content");
            
            for (FileItem file : files) {
                if (file.getId().equals(fileId)) {
                    file.setContent(newContent);
                    break;
                }
            }
            saveLocalFiles();
            adapter.setItems(folders, files);
        }
    }
    
    private void showContextMenu(Object item, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Rename");
        popup.getMenu().add("Delete");
        
        final Object finalItem = item;
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem menuItem) {
                String title = menuItem.getTitle().toString();
                if ("Rename".equals(title)) {
                    showRenameDialog(finalItem);
                } else if ("Delete".equals(title)) {
                    showDeleteConfirmation(finalItem);
                }
                return true;
            }
        });
        popup.show();
    }
    
    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Folder");
        
        final EditText input = new EditText(this);
        input.setHint("Folder name");
        builder.setView(input);
        
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();
                if (!TextUtils.isEmpty(name)) {
                    createFolder(name);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCreateFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New File");
        
        final EditText input = new EditText(this);
        input.setHint("filename.py");
        builder.setView(input);
        
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fullName = input.getText().toString().trim();
                if (!TextUtils.isEmpty(fullName)) {
                    createFile(fullName);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void createFolder(String name) {
        FolderItem folder = new FolderItem(
            UUID.randomUUID().toString(),
            currentFolder.getId(),
            name
        );
        folders.add(folder);
        saveLocalFiles();
        adapter.setItems(folders, files);
        updateUI();
        Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show();
    }
    
    private void createFile(String fullName) {
        String name = fullName;
        String extension = "";
        
        int dotIndex = fullName.lastIndexOf('.');
        if (dotIndex > 0) {
            name = fullName.substring(0, dotIndex);
            extension = fullName.substring(dotIndex + 1);
        }
        
        FileItem file = new FileItem(
            UUID.randomUUID().toString(),
            currentFolder.getId(),
            name,
            extension,
            ""
        );
        files.add(file);
        saveLocalFiles();
        adapter.setItems(folders, files);
        updateUI();
        
        openFile(file);
    }
    
    private void showRenameDialog(final Object item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename");
        
        final EditText input = new EditText(this);
        if (item instanceof FolderItem) {
            input.setText(((FolderItem) item).getName());
        } else if (item instanceof FileItem) {
            input.setText(((FileItem) item).getFullName());
        }
        builder.setView(input);
        
        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (!TextUtils.isEmpty(newName)) {
                    renameItem(item, newName);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void renameItem(Object item, String newName) {
        if (item instanceof FolderItem) {
            ((FolderItem) item).setName(newName);
        } else if (item instanceof FileItem) {
            FileItem file = (FileItem) item;
            int dotIndex = newName.lastIndexOf('.');
            if (dotIndex > 0) {
                file.setName(newName.substring(0, dotIndex));
                file.setExtension(newName.substring(dotIndex + 1));
            } else {
                file.setName(newName);
            }
        }
        saveLocalFiles();
        adapter.setItems(folders, files);
        Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
    }
    
    private void showDeleteConfirmation(final Object item) {
        String name = "";
        if (item instanceof FolderItem) {
            name = ((FolderItem) item).getName();
        } else if (item instanceof FileItem) {
            name = ((FileItem) item).getFullName();
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete \"" + name + "\"?")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteItem(item);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteItem(Object item) {
        if (item instanceof FolderItem) {
            folders.remove(item);
        } else if (item instanceof FileItem) {
            files.remove(item);
        }
        saveLocalFiles();
        adapter.setItems(folders, files);
        updateUI();
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
    }
    
    private void showMoreMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Refresh");
        popup.getMenu().add("Sort by name");
        popup.getMenu().add("Sort by date");
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                String title = item.getTitle().toString();
                if ("Refresh".equals(title)) {
                    loadLocalFiles();
                    updateUI();
                }
                return true;
            }
        });
        popup.show();
    }
    
    private void navigateToRoot() {
        folderStack.clear();
        loadRootFolder();
    }
    
    @Override
    public void onBackPressed() {
        if (folderStack.size() > 1) {
            folderStack.remove(folderStack.size() - 1);
            currentFolder = folderStack.get(folderStack.size() - 1);
            loadLocalFiles();
            updateUI();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (supabaseService != null) {
            supabaseService.shutdown();
        }
    }
}
