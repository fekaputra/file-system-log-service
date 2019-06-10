package ac.at.tuwien.logparser.entities;

public class Channel {

    private String id;
    private String type;
    private String name;
    private String path;
    private String program;

    public Channel(String id, String type, String name, String path, String program) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.path = path;
        this.program = program;
    }

    public Channel(String type, String name, String path, String program) {
        this.type = type;
        this.name = name;
        this.path = path;
        this.program = program;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }
}
