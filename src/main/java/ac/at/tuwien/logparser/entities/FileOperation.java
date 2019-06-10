package ac.at.tuwien.logparser.entities;

import java.util.Date;

public class FileOperation {

    private String logEntryNode;
    private String fileNode;
    private String accessCall;
    private String filename;
    private Date timestamp;

    public FileOperation(){}

    public FileOperation(String logEntryNode, String fileNode, String accessCall, String filename, Date timestamp) {
        this.logEntryNode = logEntryNode;
        this.fileNode = fileNode;
        this.accessCall = accessCall;
        this.filename = filename;
        this.timestamp = timestamp;
    }

    public String getLogEntryNode() {
        return logEntryNode;
    }

    public void setLogEntryNode(String logEntryNode) {
        this.logEntryNode = logEntryNode;
    }

    public String getFileNode() {
        return fileNode;
    }

    public void setFileNode(String fileNode) {
        this.fileNode = fileNode;
    }

    public String getAccessCall() {
        return accessCall;
    }

    public void setAccessCall(String accessCall) {
        this.accessCall = accessCall;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FileOperation{" +
                "logEntryNode='" + logEntryNode + '\'' +
                ", fileNode='" + fileNode + '\'' +
                ", accessCall='" + accessCall + '\'' +
                ", filename='" + filename + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
