package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PersonasActivity extends Activity {
    
    private ListView listPersonas;
    private ImageButton btnBack;
    private View btnAddPersona;
    
    private PersonaManager personaManager;
    private PersonaAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_personas);
        
        personaManager = new PersonaManager(this);
        
        initViews();
        setupListeners();
        loadPersonas();
    }
    
    private void initViews() {
        listPersonas = (ListView) findViewById(R.id.personaList);
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnAddPersona = findViewById(R.id.btnAddPersona);
    }
    
    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
        
        if (btnAddPersona != null) {
            btnAddPersona.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddPersonaDialog();
                }
            });
        }
        
        listPersonas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PersonaManager.Persona persona = adapter.getItem(position);
                personaManager.setActivePersona(persona.id);
                adapter.notifyDataSetChanged();
                Toast.makeText(PersonasActivity.this, "Persona aktif: " + persona.name, Toast.LENGTH_SHORT).show();
            }
        });
        
        listPersonas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PersonaManager.Persona persona = adapter.getItem(position);
                if (persona.isCustom) {
                    showDeleteDialog(persona);
                }
                return true;
            }
        });
    }
    
    private void loadPersonas() {
        List<PersonaManager.Persona> personas = personaManager.getAllPersonas();
        adapter = new PersonaAdapter(personas);
        listPersonas.setAdapter(adapter);
    }
    
    private void showAddPersonaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Persona Baru");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_persona, null);
        builder.setView(dialogView);
        
        final EditText etName = (EditText) dialogView.findViewById(R.id.etName);
        final EditText etIcon = (EditText) dialogView.findViewById(R.id.etIcon);
        final EditText etDescription = (EditText) dialogView.findViewById(R.id.etDescription);
        final EditText etSystemPrompt = (EditText) dialogView.findViewById(R.id.etSystemPrompt);
        
        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String icon = etIcon.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String systemPrompt = etSystemPrompt.getText().toString().trim();
                
                if (name.isEmpty()) {
                    Toast.makeText(PersonasActivity.this, "Nama harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (icon.isEmpty()) {
                    icon = "🤖";
                }
                
                personaManager.addCustomPersona(name, icon, description, systemPrompt);
                loadPersonas();
                Toast.makeText(PersonasActivity.this, "Persona ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Batal", null);
        builder.show();
    }
    
    private void showDeleteDialog(final PersonaManager.Persona persona) {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Persona")
            .setMessage("Hapus persona \"" + persona.name + "\"?")
            .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    personaManager.deletePersona(persona.id);
                    loadPersonas();
                    Toast.makeText(PersonasActivity.this, "Persona dihapus", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    private class PersonaAdapter extends BaseAdapter {
        private List<PersonaManager.Persona> personas;
        
        public PersonaAdapter(List<PersonaManager.Persona> personas) {
            this.personas = personas;
        }
        
        @Override
        public int getCount() {
            return personas.size();
        }
        
        @Override
        public PersonaManager.Persona getItem(int position) {
            return personas.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(PersonasActivity.this)
                    .inflate(R.layout.item_persona, parent, false);
            }
            
            PersonaManager.Persona persona = getItem(position);
            boolean isActive = persona.id.equals(personaManager.getActivePersonaId());
            
            TextView tvIcon = (TextView) convertView.findViewById(R.id.tvIcon);
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            TextView tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
            ImageView ivCheck = (ImageView) convertView.findViewById(R.id.ivCheck);
            
            tvIcon.setText(persona.icon);
            tvName.setText(persona.name);
            tvDescription.setText(persona.description);
            
            if (ivCheck != null) {
                ivCheck.setVisibility(isActive ? View.VISIBLE : View.GONE);
            }
            
            return convertView;
        }
    }
}
