package ac.at.tuwien.logparser.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Agnes on 02.09.18.
 */
public class LogType {

    private String logTypeName;
    @JsonProperty(value = "@type")
    private String type;

    public LogType(){}

    public LogType(String logTypeName) {
        this.logTypeName = logTypeName;
    }

    public String getLogTypeName() {
        return logTypeName;
    }

    public void setLogTypeName(String logTypeName) {
        this.logTypeName = logTypeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "LogType{" +
                "logTypeName='" + logTypeName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
