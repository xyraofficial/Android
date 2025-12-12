package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CodeEditorActivity extends Activity {
    
    private ImageButton btnBack;
    private ImageButton btnUndo;
    private ImageButton btnRedo;
    private ImageButton btnSave;
    private ImageButton btnTheme;
    private ImageButton btnMore;
    private TextView tvFileName;
    private TextView tvLanguage;
    private TextView tvLineNumbers;
    private TextView tvCursorPosition;
    private TextView tvFileSize;
    private TextView tvModified;
    private EditText etCode;
    private ScrollView editorScrollView;
    private LinearLayout symbolBar;
    
    private CodeEditorEngine editorEngine;
    private String fileId;
    private String fileName;
    private String folderId;
    private String originalContent;
    private boolean isModified = false;
    
    private List<String> undoStack;
    private List<String> redoStack;
    private int undoPointer = -1;
    private boolean isUndoRedoing = false;
    
    private String currentThemeName = "Monokai";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_code_editor);
        
        editorEngine = new CodeEditorEngine();
        undoStack = new ArrayList<>();
        redoStack = new ArrayList<>();
        
        initViews();
        loadFileFromIntent();
        setupTextWatcher();
        setupClickListeners();
        setupSymbolBar();
        applyEditorTheme();
    }
    
    private void initViews() {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnUndo = (ImageButton) findViewById(R.id.btnUndo);
        btnRedo = (ImageButton) findViewById(R.id.btnRedo);
        btnSave = (ImageButton) findViewById(R.id.btnSave);
        btnTheme = (ImageButton) findViewById(R.id.btnTheme);
        btnMore = (ImageButton) findViewById(R.id.btnMore);
        tvFileName = (TextView) findViewById(R.id.tvFileName);
        tvLanguage = (TextView) findViewById(R.id.tvLanguage);
        tvLineNumbers = (TextView) findViewById(R.id.tvLineNumbers);
        tvCursorPosition = (TextView) findViewById(R.id.tvCursorPosition);
        tvFileSize = (TextView) findViewById(R.id.tvFileSize);
        tvModified = (TextView) findViewById(R.id.tvModified);
        etCode = (EditText) findViewById(R.id.etCode);
        editorScrollView = (ScrollView) findViewById(R.id.editorScrollView);
        symbolBar = (LinearLayout) findViewById(R.id.symbolBar);
    }
    
    private void loadFileFromIntent() {
        Intent intent = getIntent();
        fileId = intent.getStringExtra("file_id");
        fileName = intent.getStringExtra("file_name");
        folderId = intent.getStringExtra("folder_id");
        String content = intent.getStringExtra("file_content");
        String language = intent.getStringExtra("file_language");
        
        if (fileName == null) fileName = "untitled.txt";
        if (content == null) content = "";
        if (language == null) language = "text";
        
        originalContent = content;
        tvFileName.setText(fileName);
        tvLanguage.setText(capitalizeFirst(language));
        etCode.setText(content);
        
        editorEngine.setLanguage(language);
        
        undoStack.add(content);
        undoPointer = 0;
        
        updateLineNumbers();
        updateFileSize();
        updateModifiedStatus();
    }
    
    private void setupTextWatcher() {
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateLineNumbers();
                updateCursorPosition();
                updateFileSize();
                
                if (!isUndoRedoing) {
                    String text = s.toString();
                    if (undoPointer < undoStack.size() - 1) {
                        undoStack = new ArrayList<>(undoStack.subList(0, undoPointer + 1));
                    }
                    undoStack.add(text);
                    undoPointer = undoStack.size() - 1;
                    redoStack.clear();
                }
                
                isModified = !s.toString().equals(originalContent);
                updateModifiedStatus();
            }
        });
        
        etCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCursorPosition();
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
        
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undo();
            }
        });
        
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redo();
            }
        });
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile();
            }
        });
        
        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeMenu(v);
            }
        });
        
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreMenu(v);
            }
        });
    }
    
    private void setupSymbolBar() {
        int[] buttonIds = {
            R.id.btnTab, R.id.btnBracket1, R.id.btnBracket2, 
            R.id.btnBrace1, R.id.btnBrace2, R.id.btnSquare1, R.id.btnSquare2,
            R.id.btnQuote, R.id.btnSingleQuote, R.id.btnSemicolon,
            R.id.btnColon, R.id.btnEquals, R.id.btnLess, R.id.btnGreater,
            R.id.btnSlash, R.id.btnBackslash, R.id.btnHash, R.id.btnDollar, R.id.btnUnderscore
        };
        
        final String[] symbols = {
            "    ", "(", ")", "{", "}", "[", "]", "\"", "'", ";",
            ":", "=", "<", ">", "/", "\\", "#", "$", "_"
        };
        
        for (int i = 0; i < buttonIds.length; i++) {
            final int index = i;
            View btn = findViewById(buttonIds[i]);
            if (btn != null) {
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        insertSymbol(symbols[index]);
                    }
                });
            }
        }
    }
    
    private void insertSymbol(String symbol) {
        int start = etCode.getSelectionStart();
        int end = etCode.getSelectionEnd();
        Editable text = etCode.getText();
        text.replace(start, end, symbol);
        etCode.setSelection(start + symbol.length());
    }
    
    private void updateLineNumbers() {
        String code = etCode.getText().toString();
        String[] lines = code.split("\n", -1);
        StringBuilder lineNums = new StringBuilder();
        for (int i = 1; i <= lines.length; i++) {
            lineNums.append(i).append("\n");
        }
        tvLineNumbers.setText(lineNums.toString().trim());
    }
    
    private void updateCursorPosition() {
        int pos = etCode.getSelectionStart();
        String text = etCode.getText().toString();
        
        int line = 1;
        int col = 1;
        
        for (int i = 0; i < pos && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }
        
        tvCursorPosition.setText("Ln " + line + ", Col " + col);
    }
    
    private void updateFileSize() {
        int size = etCode.getText().toString().getBytes().length;
        String sizeText;
        if (size < 1024) {
            sizeText = size + " bytes";
        } else if (size < 1024 * 1024) {
            sizeText = String.format("%.1f KB", size / 1024.0);
        } else {
            sizeText = String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
        tvFileSize.setText(sizeText);
    }
    
    private void updateModifiedStatus() {
        tvModified.setText(isModified ? "Modified" : "Saved");
    }
    
    private void undo() {
        if (undoPointer > 0) {
            isUndoRedoing = true;
            undoPointer--;
            redoStack.add(etCode.getText().toString());
            etCode.setText(undoStack.get(undoPointer));
            etCode.setSelection(etCode.getText().length());
            isUndoRedoing = false;
        }
    }
    
    private void redo() {
        if (!redoStack.isEmpty()) {
            isUndoRedoing = true;
            String redoText = redoStack.remove(redoStack.size() - 1);
            undoPointer++;
            if (undoPointer >= undoStack.size()) {
                undoStack.add(redoText);
            }
            etCode.setText(redoText);
            etCode.setSelection(etCode.getText().length());
            isUndoRedoing = false;
        }
    }
    
    private void saveFile() {
        originalContent = etCode.getText().toString();
        isModified = false;
        updateModifiedStatus();
        
        Intent result = new Intent();
        result.putExtra("file_id", fileId);
        result.putExtra("file_content", originalContent);
        setResult(RESULT_OK, result);
        
        Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
    }
    
    private void showThemeMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Monokai");
        popup.getMenu().add("Dracula");
        popup.getMenu().add("One Dark");
        popup.getMenu().add("Light");
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                String themeName = item.getTitle().toString();
                setEditorTheme(themeName);
                return true;
            }
        });
        popup.show();
    }
    
    private void setEditorTheme(String themeName) {
        currentThemeName = themeName;
        
        CodeEditorEngine.Theme theme;
        switch (themeName) {
            case "Dracula":
                theme = CodeEditorEngine.Theme.DRACULA;
                break;
            case "One Dark":
                theme = CodeEditorEngine.Theme.ONE_DARK;
                break;
            case "Light":
                theme = CodeEditorEngine.Theme.LIGHT;
                break;
            default:
                theme = CodeEditorEngine.Theme.MONOKAI;
        }
        
        editorEngine.setTheme(theme);
        applyEditorTheme();
        Toast.makeText(this, "Theme: " + themeName, Toast.LENGTH_SHORT).show();
    }
    
    private void applyEditorTheme() {
        CodeEditorEngine.Theme theme = editorEngine.getTheme();
        
        View mainContent = findViewById(android.R.id.content);
        mainContent.setBackgroundColor(theme.background);
        
        etCode.setTextColor(theme.text);
        etCode.setBackgroundColor(theme.background);
        
        tvLineNumbers.setTextColor(theme.lineNumber);
        View lineNumbersContainer = findViewById(R.id.lineNumbersContainer);
        if (lineNumbersContainer != null) {
            lineNumbersContainer.setBackgroundColor(theme.lineNumberBg);
        }
        
        View statusBar = findViewById(R.id.statusBar);
        if (statusBar != null) {
            statusBar.setBackgroundColor(theme.lineNumberBg);
        }
        
        tvCursorPosition.setTextColor(theme.lineNumber);
        tvFileSize.setTextColor(theme.lineNumber);
        tvModified.setTextColor(theme.lineNumber);
    }
    
    private void showMoreMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Find & Replace");
        popup.getMenu().add("Go to Line");
        popup.getMenu().add("Word Wrap");
        popup.getMenu().add("Font Size");
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                String title = item.getTitle().toString();
                if ("Go to Line".equals(title)) {
                    showGoToLineDialog();
                } else if ("Find & Replace".equals(title)) {
                    showFindReplaceDialog();
                }
                return true;
            }
        });
        popup.show();
    }
    
    private void showGoToLineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Go to Line");
        
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Line number");
        builder.setView(input);
        
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int lineNum = Integer.parseInt(input.getText().toString());
                    goToLine(lineNum);
                } catch (NumberFormatException e) {
                    Toast.makeText(CodeEditorActivity.this, "Invalid line number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void goToLine(int lineNum) {
        String code = etCode.getText().toString();
        String[] lines = code.split("\n", -1);
        
        if (lineNum < 1 || lineNum > lines.length) {
            Toast.makeText(this, "Line out of range", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int position = 0;
        for (int i = 0; i < lineNum - 1 && i < lines.length; i++) {
            position += lines[i].length() + 1;
        }
        
        etCode.setSelection(Math.min(position, code.length()));
        etCode.requestFocus();
    }
    
    private void showFindReplaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Find & Replace");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        final EditText findInput = new EditText(this);
        findInput.setHint("Find");
        layout.addView(findInput);
        
        final EditText replaceInput = new EditText(this);
        replaceInput.setHint("Replace with");
        layout.addView(replaceInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Replace All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String find = findInput.getText().toString();
                String replace = replaceInput.getText().toString();
                if (!find.isEmpty()) {
                    String code = etCode.getText().toString();
                    String newCode = code.replace(find, replace);
                    etCode.setText(newCode);
                    Toast.makeText(CodeEditorActivity.this, "Replaced", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    @Override
    public void onBackPressed() {
        if (isModified) {
            new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("Do you want to save changes before leaving?")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveFile();
                        finish();
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton("Cancel", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}
