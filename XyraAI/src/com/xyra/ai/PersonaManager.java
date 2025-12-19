package com.xyra.ai;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PersonaManager {
    
    private static final String TAG = "PersonaManager";
    private static final String PREFS_NAME = "xyra_personas";
    private static final String KEY_PERSONAS = "personas";
    private static final String KEY_ACTIVE_PERSONA = "active_persona";
    
    private Context context;
    private SharedPreferences prefs;
    private List<Persona> personas;
    private String activePersonaId;
    
    public static class Persona {
        public String id;
        public String name;
        public String icon;
        public String description;
        public String systemPrompt;
        public boolean isCustom;
        public long createdAt;
        
        public Persona() {
            this.id = String.valueOf(System.currentTimeMillis());
            this.createdAt = System.currentTimeMillis();
            this.isCustom = false;
        }
        
        public Persona(String id, String name, String icon, String description, String systemPrompt) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.description = description;
            this.systemPrompt = systemPrompt;
            this.isCustom = false;
            this.createdAt = System.currentTimeMillis();
        }
    }
    
    public PersonaManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.personas = new ArrayList<Persona>();
        initDefaultPersonas();
        loadCustomPersonas();
        this.activePersonaId = prefs.getString(KEY_ACTIVE_PERSONA, "default");
    }
    
    private void initDefaultPersonas() {
        personas.add(new Persona(
            "default",
            "XyraAI Default",
            "🤖",
            "Asisten AI umum yang bisa membantu berbagai tugas",
            ""
        ));
        
        personas.add(new Persona(
            "coding_helper",
            "Coding Helper",
            "💻",
            "Ahli programming dan debugging",
            "You are an expert programmer and software developer. Help users with coding questions, debugging, code review, and best practices. Always provide clear code examples with explanations. Use proper code formatting with language-specific syntax highlighting."
        ));
        
        personas.add(new Persona(
            "translator",
            "Translator",
            "🌍",
            "Penerjemah multi-bahasa profesional",
            "You are a professional translator fluent in multiple languages. Translate text accurately while preserving the original meaning, tone, and cultural nuances. When translating, also explain any idioms or cultural references that don't translate directly."
        ));
        
        personas.add(new Persona(
            "writer",
            "Creative Writer",
            "✍️",
            "Penulis kreatif untuk konten dan copywriting",
            "You are a creative writer and content creator. Help users write engaging content including articles, stories, marketing copy, emails, and social media posts. Focus on clarity, engagement, and the appropriate tone for the target audience."
        ));
        
        personas.add(new Persona(
            "tutor",
            "Study Tutor",
            "📚",
            "Tutor untuk belajar dan pendidikan",
            "You are a patient and encouraging tutor. Explain concepts clearly using simple language and examples. Break down complex topics into manageable steps. Ask questions to check understanding and provide practice problems when helpful."
        ));
        
        personas.add(new Persona(
            "analyst",
            "Data Analyst",
            "📊",
            "Analis data dan bisnis",
            "You are a data analyst expert. Help users understand data, create analyses, interpret statistics, and make data-driven decisions. Explain analytical concepts clearly and suggest appropriate visualization methods."
        ));
        
        personas.add(new Persona(
            "chef",
            "Chef & Recipe Helper",
            "👨‍🍳",
            "Asisten memasak dan resep",
            "You are a professional chef and cooking expert. Provide detailed recipes, cooking tips, ingredient substitutions, and meal planning advice. Adjust recipes based on dietary restrictions and available ingredients."
        ));
        
        personas.add(new Persona(
            "fitness",
            "Fitness Coach",
            "💪",
            "Pelatih kebugaran dan kesehatan",
            "You are a knowledgeable fitness coach and health advisor. Provide workout plans, exercise instructions, nutrition advice, and motivation. Always recommend consulting healthcare professionals for medical concerns."
        ));
        
        personas.add(new Persona(
            "brainstorm",
            "Brainstorm Partner",
            "💡",
            "Partner untuk brainstorming ide kreatif",
            "You are a creative brainstorming partner. Help generate ideas, explore possibilities, and think outside the box. Use techniques like mind mapping, lateral thinking, and SCAMPER to spark creativity."
        ));
        
        personas.add(new Persona(
            "interviewer",
            "Interview Coach",
            "🎯",
            "Pelatih untuk persiapan wawancara",
            "You are an interview coach and career advisor. Help users prepare for job interviews with practice questions, feedback on answers, and tips for presentation. Provide industry-specific advice when relevant."
        ));
    }
    
    private void loadCustomPersonas() {
        try {
            String json = prefs.getString(KEY_PERSONAS, "[]");
            JSONArray array = new JSONArray(json);
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Persona persona = new Persona();
                persona.id = obj.optString("id", "");
                persona.name = obj.optString("name", "");
                persona.icon = obj.optString("icon", "🤖");
                persona.description = obj.optString("description", "");
                persona.systemPrompt = obj.optString("systemPrompt", "");
                persona.isCustom = true;
                persona.createdAt = obj.optLong("createdAt", 0);
                personas.add(persona);
            }
        } catch (Exception e) {
        }
    }
    
    private void saveCustomPersonas() {
        try {
            JSONArray array = new JSONArray();
            for (Persona persona : personas) {
                if (persona.isCustom) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", persona.id);
                    obj.put("name", persona.name);
                    obj.put("icon", persona.icon);
                    obj.put("description", persona.description);
                    obj.put("systemPrompt", persona.systemPrompt);
                    obj.put("createdAt", persona.createdAt);
                    array.put(obj);
                }
            }
            prefs.edit().putString(KEY_PERSONAS, array.toString()).apply();
        } catch (Exception e) {
        }
    }
    
    public void addCustomPersona(String name, String icon, String description, String systemPrompt) {
        Persona persona = new Persona();
        persona.name = name;
        persona.icon = icon;
        persona.description = description;
        persona.systemPrompt = systemPrompt;
        persona.isCustom = true;
        
        personas.add(persona);
        saveCustomPersonas();
    }
    
    public void updatePersona(String id, String name, String icon, String description, String systemPrompt) {
        for (Persona persona : personas) {
            if (persona.id.equals(id) && persona.isCustom) {
                persona.name = name;
                persona.icon = icon;
                persona.description = description;
                persona.systemPrompt = systemPrompt;
                saveCustomPersonas();
                return;
            }
        }
    }
    
    public void deletePersona(String id) {
        for (int i = 0; i < personas.size(); i++) {
            Persona persona = personas.get(i);
            if (persona.id.equals(id) && persona.isCustom) {
                personas.remove(i);
                saveCustomPersonas();
                if (activePersonaId.equals(id)) {
                    setActivePersona("default");
                }
                return;
            }
        }
    }
    
    public List<Persona> getAllPersonas() {
        return new ArrayList<Persona>(personas);
    }
    
    public List<Persona> getDefaultPersonas() {
        List<Persona> result = new ArrayList<Persona>();
        for (Persona persona : personas) {
            if (!persona.isCustom) {
                result.add(persona);
            }
        }
        return result;
    }
    
    public List<Persona> getCustomPersonas() {
        List<Persona> result = new ArrayList<Persona>();
        for (Persona persona : personas) {
            if (persona.isCustom) {
                result.add(persona);
            }
        }
        return result;
    }
    
    public Persona getPersonaById(String id) {
        for (Persona persona : personas) {
            if (persona.id.equals(id)) {
                return persona;
            }
        }
        return personas.get(0);
    }
    
    public void setActivePersona(String id) {
        this.activePersonaId = id;
        prefs.edit().putString(KEY_ACTIVE_PERSONA, id).apply();
    }
    
    public Persona getActivePersona() {
        return getPersonaById(activePersonaId);
    }
    
    public String getActivePersonaId() {
        return activePersonaId;
    }
    
    public String getActiveSystemPrompt() {
        Persona active = getActivePersona();
        return active.systemPrompt != null ? active.systemPrompt : "";
    }
}
