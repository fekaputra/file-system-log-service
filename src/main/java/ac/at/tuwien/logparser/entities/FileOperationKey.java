package ac.at.tuwien.logparser.entities;

import ac.at.tuwien.logparser.entities.enums.AccessCall;

import java.util.Date;
import java.util.Objects;

public class FileOperationKey implements Comparable<FileOperationKey>{

    private AccessCall accessCall;
    private Date timestamp;

    public FileOperationKey(AccessCall accessCall, Date timestamp) {
        this.accessCall = accessCall;
        this.timestamp = timestamp;
    }

    public AccessCall getAccessCall() {
        return accessCall;
    }

    public void setAccessCall(AccessCall accessCall) {
        this.accessCall = accessCall;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileOperationKey)) return false;
        FileOperationKey that = (FileOperationKey) o;
        return getAccessCall() == that.getAccessCall() &&
                Objects.equals(getTimestamp(), that.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccessCall(), getTimestamp());
    }

    @Override
    public String toString() {
        return "FileOperationKey{" +
                "accessCall=" + accessCall +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int compareTo(FileOperationKey o) {
        System.out.println("compare: "+o.toString());
       return this.timestamp.compareTo(o.getTimestamp());
      /*  if(this.timestamp.before(o.getTimestamp())){ //current timestamp is before new timestamp
            return -1;
        }else if(this.timestamp.after(o.getTimestamp())){ // current timestamp is after new timestamp
            return 1;
        }
        return 0;*/
    }
}
