package ac.at.tuwien.logparser.entities.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AccessCall {

    GETATTRLIST("getattrlist()"),
    CLOSE("close(2)"),
    OPEN_READ("open(2) - read"),
    FCNTL("fcntl(2)"),
    ACCESS("access(2)"),
    OPENAT_READ("openat(2) - read"),
    RENAME("rename(2)"),
    ACCESS_EXTENDED("access_extended(2)"),
    ACCESS_EXTENDED_WRITE_CREAT("open_extended(2) - write,creat"),
    OPEN_ATTR_ONLY("open(2) - attr only"),
    FSTATAT("fstatat(2)"),
    UNLINK("unlink(2)"),
    CHMOD("chmod(2)"),
    OPEN_READ_WRITE("open(2) - read,write"),
    OPEN_READ_CREAT("open(2) - read,creat"),
    OPEN_WRITE_CREAT("open(2) - write,creat"),
    OPEN_READ_WRITE_CREAT("open(2) - read,write,creat"),
    OPEN_WRITE_CREAT_TRUNC("open(2) - write,creat,trunc"),
    OPEN_READE_WRITE_CREAT_TRUNC("open(2) - read,write,creat,trunc"),
    FTRUNCATE("ftruncate(2)"),
    OPEN_WRITE("open(2) - write"),
    FSYNC("fsync(2)"),
    FCHFLAGS("fchflags(2)"),
    LSTAT("lstat(2)"),
    CHDIR("chdir(2)"),
    MKDIR("mkdir(2)"),
    SETATTRLIST("setattrlist()"),
    READLINK("readlink(2)"),
    FSCTL("fsctl()"),
    RMDIR("rmdir(2)"),
    IOCTL("ioctl(2)"),
    UTIMES("utimes(2)"),
    FCHOWN("fchown(2)"),
    QUOTACTL("quotactl(2)"),
    FCHMODEXTENDED("fchmod_extended(2)"),
    FCHMOD("fchmod(2)");

    private String value;

    AccessCall(String value){
        this.value = value;
    }

    @JsonValue
    public String value(){
        return this.value;
    }

    public String getValue() {
        return value;
    }

    public static AccessCall findByValue(String value){
        for(AccessCall ac : values()){
            if( ac.value.equals(value)){
                return ac;
            }
        }
        return null;
    }
}
