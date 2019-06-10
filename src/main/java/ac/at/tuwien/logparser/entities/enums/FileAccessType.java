package ac.at.tuwien.logparser.entities.enums;

public enum FileAccessType {

    Created, Created_Modified, Created_Copied, Renamed,
    Moved, MovedToRecycleBin, Deleted,
    MaliciousDownloadedFile, MaliciousFileExecution;
}
