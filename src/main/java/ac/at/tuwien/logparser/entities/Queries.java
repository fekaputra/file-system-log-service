package ac.at.tuwien.logparser.entities;


public final class Queries {

    public static final String copySameDirectory = "REGISTER QUERY copySameDirectory AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT * " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 5m STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "FILTER ( str(?accessCall) = \"setattrlist()\" || str(?accessCall) = \"rename(2)\") ." +
            "OPTIONAL { ?logEntry file:hasFile/file:pathname ?pathname . }" +
            "OPTIONAL { ?logEntry file:hasProcess/file:processID ?processID . }" +
            "OPTIONAL { ?logEntry file:hasUser/file:username ?username . }" +
            "OPTIONAL { ?logEntry file:originatesFrom/file:hostname ?hostname . }" +
            "?logEntry file:timestamp ?timestamp . " +
            "{ " +
            "SELECT * " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname2 .  " +
            "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
            "FILTER ( regex(?pathname2, ?pathname) ) " + // get pathname of rename event which comes from a setattrlist event (create of new file)
            "} " +
            "} " +
            "}";

    public static final String copyInOtherDirectory2 = "REGISTER QUERY copyInOtherDirectory2 AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT DISTINCT ?pathname1 ?filename ?path1  " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 5m STEP 5s] " +
            "WHERE { " +
            "?logEntry1 file:accessCall ?accessCall1 . " +
            "?logEntry2 file:accessCall ?accessCall2 . " +
            "FILTER ( str(?accessCall1) = \"setattrlist()\" ) . " +
            "FILTER ( str(?accessCall2) = \"getattrlist()\" ) . " +
            "OPTIONAL { ?logEntry1 file:hasFile/file:pathname ?pathname1 . }" +
            "OPTIONAL { ?logEntry2 file:hasFile/file:pathname ?pathname2 . }" +
            "BIND ( STRAFTER(REPLACE(STRBEFORE(str(?pathname1),\",\"), \"(/[a-zA-Z0-9-_\\s:.\\w{1,5}]+)+\", \"$1\"), \"/\") AS ?filename)" + //extract filename from path
            "BIND ( STRBEFORE(str(?pathname1), ?filename) AS ?path1)" +
            "BIND ( STRBEFORE(str(?pathname2), ?filename) AS ?path2)" +
            "FILTER ( CONTAINS(str(?pathname2), str(?filename)) ) . " +
            "FILTER ( ?path1 != ?path2 ) . " + // paths of setattrlist and getattrlist are not equal in case the file got copied to other directory
            "}";
    public static final String copyInOtherDirectory = "REGISTER QUERY copyInOtherDirectory AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
            "SELECT DISTINCT ?accessCall1 ?temp ?filename1 ?accessCall2 ?path2  ?filename2 " +
            //  "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 5m STEP 5s] " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry1 file:accessCall ?accessCall1 . " +
            "FILTER ( str(?accessCall1) = \"setattrlist()\" ) . " +
            "?logEntry1 file:timestamp ?timestamp1 . " +
            "BIND(STRDT(STR(?timestamp1), xsd:dateTime) AS ?timestamp2)" +
            "?logEntry2 file:accessCall ?accessCall2 . " +
            "FILTER ( str(?accessCall2) = \"getattrlist()\" ) . " +
            "?logEntry2 file:timestamp ?timestamp3 . " +
            "BIND(STRDT(STR(?timestamp3), xsd:dateTime) AS ?timestamp4)" +
            "OPTIONAL { ?logEntry1 file:hasFile/file:pathname ?pathname1 . }" +
            "BIND ( STRDT(STRAFTER(str(?pathname1),\",\"), xsd:string) AS ?temp ) . " +
            "FILTER REGEX( ?temp, \"[a-zA-Z0-9]+[.][a-zA-Z0-9]+\" ) . " + //filter only paths of files
            "OPTIONAL { ?logEntry2 file:hasFile/file:pathname ?pathname2 . }" +
            "BIND ( STRDT(STRAFTER(REPLACE(STRBEFORE(str(?pathname1),\",\"), \"(/[a-zA-Z0-9-_ :.~$]+)+\", \"$1\"), \"/\"), xsd:string) AS ?filename1)" + //extract filename from path
            "BIND ( STRDT(STRAFTER(REPLACE(STRBEFORE(str(?pathname2),\",\"), \"(/[a-zA-Z0-9-_ :.~$]+)+\", \"$1\"), \"/\"), xsd:string) AS ?filename2)" + //extract filename from path
            "BIND ( STRDT(STRBEFORE(str(?pathname1), ?filename1), xsd:string) AS ?path1)" +
            "BIND ( STRDT(STRBEFORE(str(?pathname2), ?filename2), xsd:string) AS ?path2)" +
            "FILTER ( ?filename1 = ?filename2 ) . " + // filenames have to be equal
            "FILTER ( ?path1 != ?path2 ) . " + // directory is different
            "FILTER ( !CONTAINS(?path1, \"/.Trash/\") ) " +
            "FILTER ( !CONTAINS(?path2, \"/.Trash/\") ) " +
            "FILTER ( ?timestamp2 > ?timestamp4 ) " +
            "}";

    /****************** queries for test csparql engine 24.12.18******************/
    public static final String allLogEntries = "REGISTER QUERY logentries AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String created = "REGISTER QUERY created1 AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            //"FILTER ( str(?accessCall) = \"setattrlist()\" ) .  " +
            "FILTER ( str(?accessCall) = \"open(2) - read,creat\" ) . " +
            /*"str(?accessCall) = \"open(2) - read,write,creat\" " +
            "|| str(?accessCall) = \"open(2) - write,creat,trunc\" " +
            "|| str(?accessCall) = \"open(2) - write,creat,trunc\" " +*/
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String modified = "REGISTER QUERY modified AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"open(2) - read,write\" " +
            "|| str(?accessCall) = \"open(2) - read,write,creat,trunc\" " + // create should be excluded .. but also contains write operations
            "|| str(?accessCall) = \"openat(2) - read\" " +
            "|| str(?accessCall) = \"open(2) - write,creat,trunc\" " +
            "|| str(?accessCall) = \"setattrlist()\" " +
            "|| str(?accessCall) = \"open(2) - read,write\" " +
            "|| str(?accessCall) = \"open(2) - write\" ) . " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String deleted = "REGISTER QUERY deleted AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"unlink(2)\" ) . " +
            "FILTER ( CONTAINS(?pathname, \".Trash\") ) " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String renamed = "REGISTER QUERY rename AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
            "BIND ( STRAFTER(str(?pathname), \",\") AS ?temp)" +
            "BIND ( STRBEFORE(str(?temp), \",\") AS ?path1)" +
            "BIND ( STRAFTER(str(?temp), \",\") AS ?temp2)" +
            "BIND ( STRAFTER(str(?temp2), \",\") AS ?path2)" +
            "BIND ( STRAFTER(REPLACE(?path1, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?oldFilename)" + //extract filename from path
            "BIND ( STRAFTER(REPLACE(?path2, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?newFilename)" + //extract filename from path
            "BIND ( STRBEFORE(str(?path1), ?oldFilename) AS ?directory1)" +
            "BIND ( STRBEFORE(str(?path2), ?newFilename) AS ?directory2)" +
            "FILTER ( ?directory1 = ?directory2 ) . " +
            "FILTER ( ?oldFilename != ?newFilename ) . " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String moved = "REGISTER QUERY moved AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
            "BIND ( STRAFTER(str(?pathname), \",\") AS ?temp)" +
            "BIND ( STRBEFORE(str(?temp), \",\") AS ?path1)" +
            "BIND ( STRAFTER(str(?temp), \",\") AS ?temp2)" +
            "BIND ( STRAFTER(str(?temp2), \",\") AS ?path2)" +
            "BIND ( STRAFTER(REPLACE(?path1, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?oldFilename)" + //extract filename from path
            "BIND ( STRAFTER(REPLACE(?path2, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?newFilename)" + //extract filename from path
            "BIND ( STRBEFORE(str(?path1), ?oldFilename) AS ?directory1)" +
            "BIND ( STRBEFORE(str(?path2), ?newFilename) AS ?directory2)" +
            "FILTER ( ?directory1 != ?directory2 ) . " +
            "FILTER ( ?oldFilename = ?newFilename ) . " +
            "FILTER ( !CONTAINS(?path2, \"/.Trash/\") ) " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String movedToRecycleBin = "REGISTER QUERY movedToRecycleBin AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <ws://localhost:8124/tw/stream> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
            "BIND ( STRAFTER(str(?pathname), \",\") AS ?temp)" +
            "BIND ( STRAFTER(str(?temp), \",\") AS ?path2)" +
            "FILTER ( CONTAINS(?path2, \"/.Trash/\") ) " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static String processInfo = "REGISTER QUERY processInfo AS " +
            "PREFIX process: <http://sepses.ifs.tuwien.ac.at/vocab/processInfo#> " +
            "SELECT *  " +
            "FROM STREAM <ws://localhost:8125/tw/stream> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?s process:operation ?operation;" +
            "   process:id ?id;" +
            "   process:timestamp ?timestamp;" +
            "   process:processName ?processName;" +
            "   process:pid ?pid ." +
            "}";

    /****************** TEST queries for test csparql engine 24.12.18******************/
    public static final String allLogEntriesTest = "REGISTER QUERY logentries AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String createdTest = "REGISTER QUERY created1 AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            //"FILTER ( str(?accessCall) = \"setattrlist()\" ) .  " +
            "FILTER ( str(?accessCall) = \"open(2) - read,creat\" ) . " +
            /*"str(?accessCall) = \"open(2) - read,write,creat\" " +
            "|| str(?accessCall) = \"open(2) - write,creat,trunc\" " +
            "|| str(?accessCall) = \"open(2) - write,creat,trunc\" " +*/
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String setattrlistTest = "REGISTER QUERY created1 AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"setattrlist()\" ) . " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String modifiedTest = "REGISTER QUERY modified AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( " +
            "str(?accessCall) = \"open(2) - read,write\" " +
            "|| str(?accessCall) = \"open(2) - read,write,creat,trunc\" " + // create should be excluded .. but also contains write operations
            "|| str(?accessCall) = \"openat(2) - read\" " +
            "|| str(?accessCall) = \"open(2) - write,creat,trunc\" " +
            "|| str(?accessCall) = \"setattrlist()\" " +
            "|| str(?accessCall) = \"open(2) - read,write\" " +
            "|| str(?accessCall) = \"open(2) - write\" ) . " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String del = "REGISTER QUERY modified AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"unlink(2)\" ) . " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String deletedTest = "REGISTER QUERY deleted AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"unlink(2)\" ) . " +
            "FILTER ( CONTAINS(?pathname, \"/.Trash/\") ) " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String renamedTest = "REGISTER QUERY rename AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
            "BIND ( STRAFTER(str(?pathname), \",\") AS ?temp)" +
            "BIND ( STRBEFORE(str(?temp), \",\") AS ?path1)" +
            "BIND ( STRAFTER(str(?temp), \",\") AS ?temp2)" +
            "BIND ( STRAFTER(str(?temp2), \",\") AS ?path2)" +
            "BIND ( STRAFTER(REPLACE(?path1, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?oldFilename)" + //extract filename from path
            "BIND ( STRAFTER(REPLACE(?path2, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?newFilename)" + //extract filename from path
            "BIND ( STRBEFORE(str(?path1), ?oldFilename) AS ?directory1)" +
            "BIND ( STRBEFORE(str(?path2), ?newFilename) AS ?directory2)" +
            "FILTER ( ?directory1 = ?directory2 ) . " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String movedTest = "REGISTER QUERY moved AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
            "BIND ( STRAFTER(str(?pathname), \",\") AS ?temp)" +
            "BIND ( STRBEFORE(str(?temp), \",\") AS ?path1)" +
            "BIND ( STRAFTER(str(?temp), \",\") AS ?temp2)" +
            "BIND ( STRAFTER(str(?temp2), \",\") AS ?path2)" +
            "BIND ( STRAFTER(REPLACE(?path1, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?oldFilename)" + //extract filename from path
            "BIND ( STRAFTER(REPLACE(?path2, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?newFilename)" + //extract filename from path
            "BIND ( STRBEFORE(str(?path1), ?oldFilename) AS ?directory1)" +
            "BIND ( STRBEFORE(str(?path2), ?newFilename) AS ?directory2)" +
            "FILTER ( ?directory1 != ?directory2 ) . " +
            "FILTER ( ?oldFilename = ?newFilename ) . " +
            "FILTER ( !CONTAINS(?path2, \"/.Trash/\") ) " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static final String movedToRecycleBinTest = "REGISTER QUERY movedToRecycleBin AS " +
            "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
            "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
            "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?logEntry file:accessCall ?accessCall . " +
            "?logEntry file:hasFile/file:pathname ?pathname .  " +
            "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
            "BIND ( STRAFTER(str(?pathname), \",\") AS ?temp)" +
            "BIND ( STRAFTER(str(?temp), \",\") AS ?path2)" +
            "FILTER ( CONTAINS(?path2, \"/.Trash/\") ) " +
            "?logEntry file:hasProcess/file:processID ?processID . " +
            "?logEntry file:hasUser/file:username ?username . " +
            "?logEntry file:originatesFrom/file:hostName ?host . " +
            "?logEntry file:timestamp ?timestamp . " +
            "?logEntry file:logMessage ?logMessage " +
            "}";

    public static String processInfoTest = "REGISTER QUERY processInfo AS " +
            "PREFIX process: <http://sepses.ifs.tuwien.ac.at/vocab/processInfo#> " +
            "SELECT *  " +
            "FROM STREAM <http://streamreasoning.org/streams/ps> [RANGE 10s STEP 5s] " +
            "WHERE { " +
            "?s process:operation ?operation;" +
            "   process:id ?id;" +
            "   process:timestamp ?timestamp;" +
            "   process:processName ?processName;" +
            "   process:pid ?pid ." +
            "}";

    /****************** Collection of queries used in LogConverterService ******************/

    public static String getCopySameDirectory(String targetPath) {
        return "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                "WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "FILTER ( str(?accessCall) = \"setattrlist()\" ) .  " +
                "?logEntry file:hasFile/file:pathname ?pathname .  " +
                "FILTER ( CONTAINS(str(?pathname), \"" + targetPath + "\") ) . " +
                "?logEntry file:timestamp ?timestamp . " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:logMessage ?logMessage " +
                "} ORDER BY DESC(?timestamp)"+
                " Limit 1";
    }

    public static String getPreviousFileAccessOfFilenameInOtherDirectory(String sourceFilename, String path, String timestamp, String range){
        return "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "FILTER ( str(?accessCall) = \"getattrlist()\" ) .  ?" +
                "logEntry file:hasFile/file:pathname ?pathname .  " +
                "BIND ( STRDT(STRAFTER(REPLACE(STRBEFORE(str(?pathname),\",\"), \"(/[a-zA-Z0-9-_ :.~$]+)+\", \"$1\"), \"/\"), xsd:string) AS ?filename) . " +
                "BIND ( STRDT(STRBEFORE(str(?pathname), ?filename), xsd:string) AS ?path) . " +
                "FILTER ( CONTAINS(str(?pathname), \"" + sourceFilename + "\") ) . " +
                "FILTER ( ?path != \"" + path + "\" ) . " +
                "FILTER ( ?filename = \"" + sourceFilename + "\" ) . " +
                "?logEntry file:timestamp ?timestamp . " +
                "FILTER ( ?timestamp < \"" + timestamp + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "FILTER ( ?timestamp > \"" + range + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:logMessage ?logMessage " +
                "} ORDER BY ASC(?timestamp) Limit 1";
    }

    public static String getPreviousFileAccessOfFilename(String sourcePathname, String timestamp){
        return "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                "WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "FILTER ( str(?accessCall) = \"getattrlist()\" ) .  " +
                "?logEntry file:hasFile/file:pathname ?pathname .  " +
                "FILTER ( CONTAINS(str(?pathname), \"" + sourcePathname + "\") ) . " +
                "?logEntry file:timestamp ?timestamp . " +
                "FILTER ( ?timestamp < \"" + timestamp + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:logMessage ?logMessage " +
                "} ORDER BY DESC(?timestamp)"+
                " Limit 1";
    }

    public static String findCreationOriginOfFile(String pathName, String timestamp, String excludedEventsFilter){
        return "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource ?fileNameTarget " +
                "?pathnameTarget ?username ?programName ?pid ?hostnameSource ?hostnameTarget WHERE { " +
                "?s fae:id ?id . " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasProgram/fae:pid ?pid . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( str(?pathnameTarget) = \"" + pathName + "\" )  . " +
                "FILTER ( ?timestamp <=  \"" + timestamp + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )  . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                excludedEventsFilter +
                "} ORDER BY ASC(?timestamp)";
    }

    public static String findCreationOriginOfFile2(String pathName){
        return "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource ?fileNameTarget " +
                "?pathnameTarget ?username ?programName ?pid ?hostnameSource ?hostnameTarget WHERE { " +
                "?s fae:id ?id . " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasProgram/fae:pid ?pid . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( str(?pathnameTarget) = \"" + pathName + "\" )  . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY ASC(?timestamp)";
    }

    public static String getFileAccessEventById(String id){
        return "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource ?fileNameTarget " +
                "?pathnameTarget ?username ?programName ?pid ?hostnameSource ?hostnameTarget WHERE { " +
                "?s fae:id ?id . " +
                "FILTER ( str(?id) = \"" + id + "\" )  . " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasProgram/fae:pid ?pid . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY ASC(?timestamp)";
    }

    public static String fileAccessEventBySourcePathname(String pathname) {
        return "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource ?fileNameTarget " +
                "?pathnameTarget ?username ?programName ?pid ?hostnameSource ?hostnameTarget WHERE { " +
                "?s fae:id ?id . " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasProgram/fae:pid ?pid . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( str(?pathNameSource) = \"" + pathname + "\" )  . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY ASC(?timestamp)";
    }

    public static String getLastEventOfFilepath(String pathname){
        return "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource ?fileNameTarget " +
                "?pathnameTarget ?username ?programName ?pid ?hostnameSource ?hostnameTarget WHERE { " +
                "?s fae:id ?id . " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasProgram/fae:pid ?pid . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( str(?pathNameSource) = \"" + pathname + "\" )  ." +
                "FILTER not exists {" +
                "  ?s fae:timestamp ?after" +
                "  filter (?after > ?timestamp) ." +
                "}" +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY DESC(?timestamp) " +
                "LIMIT 1";
    }

    public static String getLastEventOfFilepathInTargetFile(String pathname){
        return "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource ?fileNameTarget " +
                "?pathnameTarget ?username ?programName ?pid ?hostnameSource ?hostnameTarget WHERE { " +
                "?s fae:id ?id . " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasProgram/fae:pid ?pid . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( str(?pathnameTarget) = \"" + pathname + "\" )  ." +
                "FILTER not exists {" +
                "  ?s fae:timestamp ?after" +
                "  filter (?after > ?timestamp) ." +
                "}" +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY DESC(?timestamp) " +
                "LIMIT 1";
    }

    public static String getLastEventOfFilepathAndTimestamp(String pathname, String lastTimestamp, String excludedEventsFilter){
        return "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource ?fileNameTarget " +
                "?pathnameTarget ?username ?programName ?pid ?hostnameSource ?hostnameTarget WHERE { " +
                "?s fae:id ?id . " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasProgram/fae:pid ?pid . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( str(?pathNameSource) = \"" + pathname + "\" )  . " +
                "FILTER (?timestamp <= \"" + lastTimestamp + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                excludedEventsFilter +
                "} ORDER BY DESC(?timestamp) " +
                "LIMIT 1";
    }
}
