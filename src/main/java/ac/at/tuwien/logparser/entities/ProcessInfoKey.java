package ac.at.tuwien.logparser.entities;

public class ProcessInfoKey {
    private String pid;
    private String timestamp;

    public ProcessInfoKey(String pid, String timestamp) {
        this.pid = pid;
        this.timestamp = timestamp;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
