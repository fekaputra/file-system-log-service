package ac.at.tuwien.logparser.entities.enums;

public enum FileType {

    DOC("doc"), XSLX("xlsx"), TXT("txt");

    private String value;

    FileType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FileType findByValue(String value){
        for(FileType ft : values()){
            if( ft.value.equals(value)){
                return ft;
            }
        }
        return null;
    }

    }
