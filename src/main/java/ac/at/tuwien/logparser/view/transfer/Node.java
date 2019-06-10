package ac.at.tuwien.logparser.view.transfer;

import ac.at.tuwien.logparser.services.util.ServiceUtil;
import org.apache.commons.httpclient.util.DateParseException;

import java.util.Date;
import java.util.Locale;

public class Node implements Comparable<Node>{

    private Element time;
    private Element filename;
    private Element fileAccess;
    private Element tfilename;
    private Element program;
    private Element user;

    public Node(Element time, Element filename, Element fileAccess, Element tfilename, Element program, Element user) {
        this.time = time;
        this.filename = filename;
        this.fileAccess = fileAccess;
        this.tfilename = tfilename;
        this.program = program;
        this.user = user;
    }

    public Element getTime() {
        return time;
    }

    public void setTime(Element time) {
        this.time = time;
    }

    public Element getFilename() {
        return filename;
    }

    public void setFilename(Element filename) {
        this.filename = filename;
    }

    public Element getFileAccess() {
        return fileAccess;
    }

    public void setFileAccess(Element fileAccess) {
        this.fileAccess = fileAccess;
    }

    public Element getTfilename() {
        return tfilename;
    }

    public void setTfilename(Element tfilename) {
        this.tfilename = tfilename;
    }

    public Element getProgram() {
        return program;
    }

    public void setProgram(Element program) {
        this.program = program;
    }

    public Element getUser() {
        return user;
    }

    public void setUser(Element user) {
        this.user = user;
    }

    @Override
    public int compareTo(Node o) {
        int compare = 0;
        try {
            String dateString = ServiceUtil.getTimestampFromXSDDate(this.getTime().getValue());
            Date dateThis = ServiceUtil.parseDate(dateString, "yyyy-MM-dd'T'HH:mm:ss'Z'");
            String dateStringNode = ServiceUtil.getTimestampFromXSDDate(o.getTime().getValue());
            Date dateNode = ServiceUtil.parseDate(dateStringNode, "yyyy-MM-dd'T'HH:mm:ss'Z'");
            compare = dateThis.compareTo(dateNode);
        } catch (DateParseException e) {
            e.printStackTrace();
        }
        return compare;
    }
}
