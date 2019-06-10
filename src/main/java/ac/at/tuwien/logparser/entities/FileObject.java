package ac.at.tuwien.logparser.entities;

import java.util.HashMap;
import java.util.Map;

public class FileObject {

    private String id;
    private Map<String, LogEntry> history;

    public FileObject(String id){
        this.id = id;
        history = new HashMap<>();
    }

    public void addHistory(String key, LogEntry entry){
        if(history == null) history = new HashMap<>();
        history.put(key, entry);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, LogEntry> getHistory() {
        return history;
    }

    public void setHistory(Map<String, LogEntry> history) {
        this.history = history;
    }
}
