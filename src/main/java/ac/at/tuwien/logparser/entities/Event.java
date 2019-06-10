package ac.at.tuwien.logparser.entities;

import ac.at.tuwien.logparser.entities.enums.AccessCall;

import java.util.Objects;

public class Event {

    private AccessCall accessCall;
    private boolean occurred; // flag indicating if event with this accessCall already occurred
    private FileOperation operation;

    public Event(AccessCall accessCall, boolean occurred) {
        this.accessCall = accessCall;
        this.occurred = occurred;
    }

    public AccessCall getAccessCall() {
        return accessCall;
    }

    public void setAccessCall(AccessCall accessCall) {
        this.accessCall = accessCall;
    }

    public boolean isOccurred() {
        return occurred;
    }

    public void setOccurred(boolean occurred) {
        this.occurred = occurred;
    }

    public FileOperation getOperation() {
        return operation;
    }

    public void setOperation(FileOperation operation) {
        this.operation = operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return isOccurred() == event.isOccurred() &&
                getAccessCall() == event.getAccessCall();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccessCall(), isOccurred());
    }
}
