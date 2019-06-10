package ac.at.tuwien.logparser.entities;

public class ProcessInfo {

    private String id;
    private String operation;
    private String timestamp;
    private String processName;
    private String pid;

    public ProcessInfo(String operation, String id,  String timestamp, String processName, String pid) {
        this.id = id;
        this.operation = operation;
        this.timestamp = timestamp;
        this.processName = processName;
        this.pid = pid;
    }

    public String getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getProcessName() {
        return processName;
    }

    public String getPid() {
        return pid;
    }

    @Override
    public String toString() {
        return "ProcessInfo{" +
                "operation='" + operation + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", processName='" + processName + '\'' +
                ", pid=" + pid +
                '}';
    }
}
