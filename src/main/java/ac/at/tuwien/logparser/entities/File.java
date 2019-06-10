package ac.at.tuwien.logparser.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Agnes on 01.09.18.
 */
public class File {

    private String fileType;
    private String pathname;
    private String filename;
    @JsonProperty(value = "@type")
    private String type;

    public File(){}

    public File(String filename, String pathname) {
        this.filename = filename;
        this.pathname = pathname;
    }


    public File(String filename) {
        this.filename = filename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "File{" +
                "fileType='" + fileType + '\'' +
                ", pathname='" + pathname + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
