package ac.at.tuwien.logparser.entities;

import ac.at.tuwien.logparser.entities.enums.AccessCall;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Agnes on 01.09.18.
 */
public class LogEntry {
    private String id;
    private AccessCall accessCall;
    @JsonProperty(value = "@timestamp")
    private String timestamp;
    private String logMessage;
    private Process hasProcess;
    private File hasFile;
    private LogType hasLogType;
    private String timestampLog;
    private Host originatesFrom;
    private User hasUser;

    public LogEntry(){
    }

    public LogEntry(String id, AccessCall accessCall, String timestamp, String logMessage, Process hasProcess, File hasFile, Host originatesFrom, User hasUser) {
        this.id = id;
        this.accessCall = accessCall;
        this.timestamp = timestamp;
        this.logMessage = logMessage;
        this.hasProcess = hasProcess;
        this.hasFile = hasFile;
        this.originatesFrom = originatesFrom;
        this.hasUser = hasUser;
    }

    public String getUri(){
        return id+"-"+timestamp+"-"+accessCall.toString().toLowerCase();
    }
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public AccessCall getAccessCall() {
        return accessCall;
    }
    public void setAccessCall(AccessCall accessCall) {
        this.accessCall = accessCall;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getLogMessage() {
        return logMessage;
    }
    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
    public Process getHasProcess() {
        return hasProcess;
    }
    public void setHasProcess(Process hasProcess) {
        this.hasProcess = hasProcess;
    }
    public File getHasFile() {
        return hasFile;
    }
    public void setHasFile(File hasFile) {
        this.hasFile = hasFile;
    }
    public LogType getHasLogType() {
        return hasLogType;
    }
    public void setHasLogType(LogType hasLogType) {
        this.hasLogType = hasLogType;
    }
    public String getTimestampLog() {
        return timestampLog;
    }
    public void setTimestampLog(String timestampLog) {
        this.timestampLog = timestampLog;
    }
    public Host getOriginatesFrom() {
        return originatesFrom;
    }
    public void setOriginatesFrom(Host originatesFrom) {
        this.originatesFrom = originatesFrom;
    }
    public User getHasUser() { return hasUser;}
    public void setHasUser(User hasUser) {
        this.hasUser = hasUser;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "accessCall='" + accessCall + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", logMessage='" + logMessage + '\'' +
                ", hasProcess=" + hasProcess +
                ", hasFile=" + hasFile +
                ", hasLogType=" + hasLogType +
                ", timestampLog='" + timestampLog + '\'' +
                ", originatesFrom=" + originatesFrom +
                ", hasUser=" + hasUser +
                '}';
    }


}
