package ac.at.tuwien.logparser.entities;

public class Action {

    private String actionName;

    public Action(String actionName){
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
}
