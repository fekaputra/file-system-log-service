package ac.at.tuwien.logparser.entities;

public class Program {

    private String programName;
    private String pid;

    public Program() {
    }

    public Program(String programName) {
        this.programName = programName;
    }

    public Program(String programName, String pid) {
        this.programName = programName;
        this.pid = pid;
    }

    public String getProgramName() {
        return programName;
    }

    public String getPid() {
        return pid;
    }
}
