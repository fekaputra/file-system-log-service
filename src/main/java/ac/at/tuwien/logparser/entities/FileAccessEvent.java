package ac.at.tuwien.logparser.entities;


import java.util.Date;
import java.util.Objects;

public class FileAccessEvent implements Comparable<FileAccessEvent>{

    private String id;
    private String timestamp;
    private String eventID;
    private Action hasAction;
    private Host hasSourceHost;
    private Host hasTargetHost;
    private File hasSourceFile;
    private File hasTargetFile;
    private User hasUser;
    private Program hasProgram;
    private Date dateTime;

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date datetime) {
        this.dateTime = datetime;
    }

    @Override
    public int compareTo(FileAccessEvent o) {
        return getDateTime().compareTo(o.getDateTime());
    }

    public FileAccessEvent(String id, String timestamp, String eventID, Action hasAction, Host hasSourceHost, Host hasTargetHost, File hasSourceFile, File hasTargetFile, User hasUser, Program hasProgram) {
        this.id = id;
        this.timestamp = timestamp;
        this.eventID = eventID;
        this.hasAction = hasAction;
        this.hasSourceHost = hasSourceHost;
        this.hasTargetHost = hasTargetHost;
        this.hasSourceFile = hasSourceFile;
        this.hasTargetFile = hasTargetFile;
        this.hasUser = hasUser;
        this.hasProgram = hasProgram;
    }
    public FileAccessEvent(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public Action getHasAction() {
        return hasAction;
    }

    public void setHasAction(Action hasAction) {
        this.hasAction = hasAction;
    }

    public Host getHasSourceHost() {
        return hasSourceHost;
    }

    public void setHasSourceHost(Host hasSourceHost) {
        this.hasSourceHost = hasSourceHost;
    }

    public Host getHasTargetHost() {
        return hasTargetHost;
    }

    public void setHasTargetHost(Host hasTargetHost) {
        this.hasTargetHost = hasTargetHost;
    }

    public File getHasSourceFile() {
        return hasSourceFile;
    }

    public void setHasSourceFile(File hasSourceFile) {
        this.hasSourceFile = hasSourceFile;
    }

    public File getHasTargetFile() {
        return hasTargetFile;
    }

    public void setHasTargetFile(File hasTargetFile) {
        this.hasTargetFile = hasTargetFile;
    }

    public User getHasUser() {
        return hasUser;
    }

    public void setHasUser(User hasUser) {
        this.hasUser = hasUser;
    }

    public Program getHasProgram() {
        return hasProgram;
    }

    public void setHasProgram(Program hasProgram) {
        this.hasProgram = hasProgram;
    }

    @Override
    public String toString() {
        return "FileAccessEvent{" +
                "id='" + id + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", eventID='" + eventID + '\'' +
                ", hasAction=" + hasAction +
                ", hasSourceHost=" + hasSourceHost +
                ", hasTargetHost=" + hasTargetHost +
                ", hasSourceFile=" + hasSourceFile +
                ", hasTargetFile=" + hasTargetFile +
                ", hasUser=" + hasUser +
                ", hasProgram=" + hasProgram +
                '}';
    }

    public String getInfo(){
        return hasAction.getActionName()+"{" +
                "sourceFile='" + hasSourceFile.getPathname() +
                ", targetFile='" + hasTargetFile.getPathname() +
                ", program='" + hasProgram.getProgramName() +
                ", time='" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
     //   if (this == o) return true;
        if (!(o instanceof FileAccessEvent)) return false;
        FileAccessEvent that = (FileAccessEvent) o;
        return Objects.equals(getTimestamp(), that.getTimestamp()) &&
                Objects.equals(getHasAction().getActionName(), that.getHasAction().getActionName()) &&
                Objects.equals(getHasSourceHost().getHostname(), that.getHasSourceHost().getHostname()) &&
                Objects.equals(getHasTargetHost().getHostname(), that.getHasTargetHost().getHostname()) &&
                Objects.equals(getHasSourceFile().getPathname(), that.getHasSourceFile().getPathname()) &&
                Objects.equals(getHasTargetFile().getPathname(), that.getHasTargetFile().getPathname()) &&
                Objects.equals(getHasUser().getUsername(), that.getHasUser().getUsername()) &&
                Objects.equals(getHasProgram().getProgramName(), that.getHasProgram().getProgramName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTimestamp(), getEventID(), getHasAction(), getHasSourceHost(), getHasTargetHost(), getHasSourceFile(), getHasTargetFile(), getHasUser(), getHasProgram());
    }

}
