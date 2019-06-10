package ac.at.tuwien.logparser.entities;

public class UsbLog {

    private String id;
    private String timestamp;
    private String instanceId;
    private String message;

    public UsbLog(){}

    public UsbLog(String id, String timestamp, String instanceId, String message) {
        this.id = id;
        this.timestamp = timestamp;
        this.instanceId = instanceId;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "UsbLog{" +
                "id='" + id + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
