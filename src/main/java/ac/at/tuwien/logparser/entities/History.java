package ac.at.tuwien.logparser.entities;

public class History {

    private String id;
    private String name;
    private String timestamp;
    private String sourceFileName;
    private String targetFileName;
    private String next;
    private String duplicate;
    private String move;
    private String rename;

    public History(String id, String name, String timestamp, String sourceFileName, String targetFileName, String next) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.sourceFileName = sourceFileName;
        this.targetFileName = targetFileName;
        this.next = next;
    }

    public History(String id, String name, String timestamp, String sourceFileName, String targetFileName) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.sourceFileName = sourceFileName;
        this.targetFileName = targetFileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(String duplicate) {
        this.duplicate = duplicate;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public String getRename() {
        return rename;
    }

    public void setRename(String rename) {
        this.rename = rename;
    }

    @Override
    public String toString() {
        return "History{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", sourceFileName='" + sourceFileName + '\'' +
                ", targetFileName='" + targetFileName + '\'' +
                ", next='" + next + '\'' +
                ", duplicate='" + duplicate + '\'' +
                ", move='" + move + '\'' +
                ", rename='" + rename + '\'' +
                '}';
    }
}
