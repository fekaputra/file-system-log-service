package ac.at.tuwien.logparser.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Agnes on 01.09.18.
 */
public class Process {

    @JsonProperty(value = "@type")
    private String type;
    private String processname;
    private String processID;

    public Process(){}

    public Process(String processID) {
        this.processID = processID;
    }

    public Process(String processID, String processname) {
        this.processID = processID;
        this.processname = processname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProcessname() {
        return processname;
    }

    public void setProcessname(String processname) {
        this.processname = processname;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    @Override
    public String toString() {
        return "Process{" +
                "type='" + type + '\'' +
                ", processname='" + processname + '\'' +
                ", processID='" + processID + '\'' +
                '}';
    }
}
