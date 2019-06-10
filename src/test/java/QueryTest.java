
import ac.at.tuwien.logparser.entities.File;
import ac.at.tuwien.logparser.entities.FileAccessEvent;
import ac.at.tuwien.logparser.entities.Queries;
import ac.at.tuwien.logparser.entities.enums.FileAccessType;
import ac.at.tuwien.logparser.entities.schema.File_access_events;
import ac.at.tuwien.logparser.services.LogConverterService;
import ac.at.tuwien.logparser.services.util.ServiceUtil;
import ac.at.tuwien.logparser.tdb.TDBConnection;
import com.hp.hpl.jena.tdb.TDB;
import jdk.nashorn.internal.runtime.Source;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = {"at.ac.tuwien.logparser.service"})
@ContextConfiguration(classes = {LogConverterService.class})
public class QueryTest {

    @Autowired
    private LogConverterService logConverterService;

    @Test
    public void test_create_md5_checksum_of_filepath() throws IOException {
        String md5 = ServiceUtil.generateMD5DigestCheckSumOfFile("/Users/Agnes/Desktop/test/image.png");
        System.out.println(md5);
        String md50 = ServiceUtil.generateMD5DigestCheckSumOfFile("/Users/Agnes/Desktop/test/image1.png");
        System.out.println(md50);
        String md501 = ServiceUtil.generateMD5DigestCheckSumOfFile("/Users/Agnes/Desktop/test/image2.png");
        System.out.println(md501);
        String md5011 = ServiceUtil.generateMD5DigestCheckSumOfFile("/Users/Agnes/Desktop/test/new folder/image1.png");
        System.out.println(md5011);
        String md51 = ServiceUtil.generateMD5DigestCheckSumOfFile("/Users/Agnes/Desktop/test/test.txt");
        System.out.println(md51);
        String md52 = ServiceUtil.generateMD5DigestCheckSumOfFile("/Users/Agnes/Desktop/test/test.xml");
        System.out.println(md52);
        String md53 = ServiceUtil.generateMD5DigestCheckSumOfFile("/Users/Agnes/Desktop/test/word.docx");
        System.out.println(md53);
        String md54 = ServiceUtil.generateMD5DigestCheckSumOfFile("/Users/Agnes/Desktop/test/word2.doc");
        System.out.println(md54);
    }

    @Test
    public void query_test_get_all_history() {
        String query = "SELECT ?s ?p ?o WHERE{ ?s ?p ?o }";
        logConverterService.printRecords(query, "tdb/DB_History");
    }

    @Test
    public void query_check_history() {
        String query = "PREFIX h: <http://sepses.ifs.tuwien.ac.at/vocab/history#> " +
                "SELECT * WHERE { " +
                "?s h:id \"28d0c1cb-bbc8-4b2b-ba6e-640b45847a9f\" . " +
                "?s h:name \"Created_Copied\" . " +
                "}";
        logConverterService.printRecords(query, "tdb/DB_History");
    }

    @Test
    public void query_NT_history() {
        String query = "PREFIX h: <http://sepses.ifs.tuwien.ac.at/vocab/history#> " +
                "SELECT * WHERE { " +
                "?s ?p ?o . " +
                "}";
        TDBConnection tdb = new TDBConnection("tdb/DB_History");
        tdb.execQueryAndPrintNTriples(query, "history_18_2.nt");
    }

    @Test
    public void query_history_of_file() {
        String query = "PREFIX h: <http://sepses.ifs.tuwien.ac.at/vocab/history#> " +
                "SELECT * WHERE { " +
                "?s ?p ?o . " +
                "?s h:sourceFileName ?filename . " +
                "FILTER( str(?filename) = \"/Users/Agnes/Desktop/test-2/textwrangler2X.txt\") . " +
                "}";
        TDBConnection tdb = new TDBConnection("tdb/DB_History");
        tdb.execQueryAndPrintNTriples(query, "history_textwrangler2X_txt.nt");
    }

    @Test
    public void query_test_get_all_file_access_events() {
        String query = "SELECT ?s ?p ?o WHERE{ ?s ?p ?o } limit 1000";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }


    @Test
    public void query_test_get_all_file_access_events_realtedTo() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT ?s ?p ?o WHERE{ " +
                "?s ?p ?o ." +
                "?s fae:relatedTo ?o ." +
                "} limit 1000";
        String query1 = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct  ?time ?filename ?action ?tfilename ?program ?user  WHERE {" +
                "  ?y fae:timestamp ?timestamp." +
                "  ?y fae:hasAction/fae:actionName ?action." +
                "  ?y fae:hasUser/fae:userName ?user." +
                "  ?y fae:hasProgram/fae:hasProgramName ?program." +
                "  ?y fae:hasSourceFile/fae:fileName ?filename. " +
                "  ?y fae:hasTargetFile/fae:fileName ?tfilename." +
                "  ?x fae:hasSourceFile/fae:fileName ?xfilename ." +
                "  ?x fae:relatedTo* ?y ." +
                "  FILTER  (contains(str(?xfilename), \"/Volumes/AGNES/excel2.xlsx\"))" +
                "} " +
                "    ORDER BY ASC(?timestamp)";
        logConverterService.printRecords(query1, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_all_file_access_events_get_all_copy() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT ?s ?p ?o WHERE{ " +
                "?s ?p ?o ." +
                "?s fae:hasAction/fae:actionName ?o ." +
                "} limit 1000";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_all_file_access_events_realtedTo2() {
        String queryString = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?y ?x ?xfilename ?time ?filename ?action ?tfilename ?program ?user  WHERE {" +
                "  ?y fae:id ?id." +
                "  ?y fae:timestamp ?time." +
                "  ?y fae:hasAction/fae:actionName ?action." +
                "  ?y fae:hasUser/fae:userName ?user." +
                "  ?y fae:hasProgram/fae:programName ?program." +
                "  ?y fae:hasTargetFile/fae:pathName ?tfilename." +
                "  ?y fae:hasSourceFile/fae:pathName ?filename. " +
                "  ?x fae:hasSourceFile/fae:pathName ?xfilename ." +
                "  ?x fae:relatedTo* ?y ." +
                "  FILTER  (contains(str(?xfilename), \"/Users/Agnes/Desktop/sample/excel.xlsx\"))" +
                "} ORDER BY ASC(?time)";

        String queryString1 = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct ?s ?p ?o  WHERE {" +
                " ?s ?p ?o . "+
                "  ?y fae:id ?id." +
                "  ?y fae:timestamp ?time." +
                "  ?y fae:hasAction/fae:actionName ?action." +
                "  ?y fae:hasUser/fae:userName ?user." +
                "  ?y fae:hasProgram/fae:programName ?program." +
                "  ?y fae:hasTargetFile/fae:pathName ?tfilename." +
                "  ?y fae:hasSourceFile/fae:pathName ?filename. " +
                "  ?x fae:hasSourceFile/fae:pathName ?xfilename ." +
                "  ?x fae:relatedTo* ?y ." +
                "  FILTER  (contains(str(?xfilename), \"/Users/Agnes/Desktop/sample/excel.xlsx\"))" +
                "} ORDER BY ASC(?time)";
        String queryString3 = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s ?p ?o . " +
                "}";
        logConverterService.printRecords(queryString3, "tdb/DB_FileAccessEvent");
        TDBConnection tdb = new TDBConnection("tdb/DB_FileAccessEvent");
        tdb.execQueryAndPrintNTriples(queryString3, "excel_copy_example.ttl");
    }

    private String fileAccessEventByActionQuery(String actionName) {
        return "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "FILTER ( str(?actionName) = \"" + actionName + "\" )  . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY ASC(?timestamp)";
    }

    @Test
    public void query_All_FileAccessEvents_exists() {
        String q = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                "WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
                "?logEntry file:hasFile/file:pathname ?pathname .  " +
             //   "BIND ( STRDT(STRAFTER(REPLACE(STRBEFORE(str(?pathname),\",\"), \"(/[a-zA-Z0-9-_ :.~$]+)+\", \"$1\"), \"/\"), xsd:string) AS ?filename) . " +
               // "BIND ( STRDT(STRBEFORE(str(?pathname), ?filename), xsd:string) AS ?path) . " +
       //         "FILTER ( CONTAINS(str(?pathname), \"textwranglerCopyV2.txt\") ) . " +
            //    "FILTER ( ?path != \"/Users/Agnes/Desktop/test/\" ) . " +
            //    "FILTER ( ?filename = \"textwranglerCopyV2.txt\" ) . " +
                "?logEntry file:timestamp ?timestamp . " +
          //      "FILTER ( ?timestamp < \"2019-04-15T12:37:10Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
               // "FILTER ( ?timestamp > \"2019-04-15T12:36:05Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:logMessage ?logMessage " +
                "} ORDER BY ASC(?timestamp)";
        String q1 = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct * WHERE { ?s ?p ?o . ?s fae:timestamp \"2019-04-15T09:36:08Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ." +
                " ?s fae:hasAction/fae:actionName ?actionName . } ";
        String q2 = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT distinct * " +
               // "SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource " +
              //  "?fileNameTarget ?pathnameTarget ?username ?programName ?hostnameSource ?hostnameTarget " +"SELECT distinct ?id ?timestamp ?actionName ?fileNameSource ?pathNameSource " +
                "WHERE { " +
                "?s fae:id ?id . " +
                "?s fae:timestamp ?timestamp . " +
               /* "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +*/
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( str(?pathnameTarget) = \"/Users/Agnes/Desktop/test/new folder/folder1/folder2/textwrangler.txt\" )  . " +
             //   "FILTER ( ?timestamp <=  \"2019-04-15T09:36:08Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )  . " +
              /*  "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +*/
                "FILTER ( ?id != \"b488df35-9d5c-47c4-8e4e-763c9f60b156\" )  . " +
                "} ORDER BY ASC(?timestamp)";


      //  logConverterService.printRecords(q2, "tdb/DB_FileAccessEvent");
        logConverterService.printRecords(q, "tdb/DB_LogEntry");


    }

    @Test
    public void query_All_FileAccessEvents_by_pathname() {
        List<FileAccessEvent> events = logConverterService.createRelatedBetweenEvents("/Users/Agnes/Desktop/sample/excel.xlsx");
        for (FileAccessEvent e : events)
            System.out.println(e.getHasAction().getActionName() + " - " + e.getTimestamp() + " - " + e.getHasSourceFile().getPathname() + " - " + e.getHasTargetFile().getPathname());
    }


    @Test
    public void query_count_All_FileAccessEvents() {
        TDBConnection tdb = new TDBConnection("tdb/DB_FileAccessEvent");
        String queryMoved = fileAccessEventByActionQuery("Moved");
        List<FileAccessEvent> moveEvents = tdb.execQueryAndGetFileAccessEvents(queryMoved);

        String queryCopy = fileAccessEventByActionQuery("Created_Copied");
        List<FileAccessEvent> copyEvents = tdb.execQueryAndGetFileAccessEvents(queryCopy);

        String queryCreated = fileAccessEventByActionQuery("Created");
        List<FileAccessEvent> createEvents = tdb.execQueryAndGetFileAccessEvents(queryCreated);

        String queryCreatedModified = fileAccessEventByActionQuery("Created_Modified");
        List<FileAccessEvent> createdModifiedEvents = tdb.execQueryAndGetFileAccessEvents(queryCreatedModified);

        String queryRenamed = fileAccessEventByActionQuery("Renamed");
        List<FileAccessEvent> renamedEvents = tdb.execQueryAndGetFileAccessEvents(queryRenamed);

        String queryMoveToRecycleBin = fileAccessEventByActionQuery("MovedToRecycleBin");
        List<FileAccessEvent> moveToTrashEvents = tdb.execQueryAndGetFileAccessEvents(queryMoveToRecycleBin);

        String queryDeleted = fileAccessEventByActionQuery("Deleted");
        List<FileAccessEvent> deletedEvents = tdb.execQueryAndGetFileAccessEvents(queryDeleted);
        int total = moveEvents.size() + copyEvents.size() + createEvents.size() + createdModifiedEvents.size()
                + renamedEvents.size() + moveToTrashEvents.size() + deletedEvents.size();
        System.out.println("\nTotal amount of events found: " + total);
        System.out.println("\nAmount of events found: ");
        System.out.println("Moved: " + moveEvents.size());
        System.out.println("Created_Copied: " + copyEvents.size());
        System.out.println("Created: " + createEvents.size());
        System.out.println("Created_Modified: " + createdModifiedEvents.size());
        System.out.println("Renamed: " + renamedEvents.size());
        System.out.println("MovedToRecycleBin: " + moveToTrashEvents.size());
        System.out.println("Deleted: " + deletedEvents.size());

        System.out.println("\n-----MOVED------");

        for (FileAccessEvent e : moveEvents)
            System.out.println(e.getInfo());

        System.out.println("\n-----COPY------");

        for (FileAccessEvent e : copyEvents)
            System.out.println(e.getInfo());

        System.out.println("\n-----CREATED------");

        for (FileAccessEvent e : createEvents)
            System.out.println(e.getInfo());

        System.out.println("\n-----MODIFIED------");

        for (FileAccessEvent e : createdModifiedEvents)
            System.out.println(e.getInfo());

        System.out.println("\n-----RENAMED------");

        for (FileAccessEvent e : renamedEvents)
            System.out.println(e.getInfo());

        System.out.println("\n-----MOVEDTORECYCLEBIN------");

        for (FileAccessEvent e : moveToTrashEvents)
            System.out.println(e.getInfo());

        System.out.println("\n-----DELETED------");

        for (FileAccessEvent e : deletedEvents)
            System.out.println(e.getInfo());
    }

    @Test
    public void query_test_get_all_file_access_events_timestamps() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT ?actionName ?timestamp " +
                "WHERE{ " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:timestamp ?timestamp }";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_all_file_name_ordered_by_timestamp() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT DISTINCT ?timestamp ?pathname WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasSourceFile/fae:fileName ?fileName  . " +
                "FILTER REGEX (?fileName , \"[a-zA-Z0-9_ :~$]+[.][a-zA-Z0-9]{2,5}\") ." +
                "?s fae:hasSourceFile/fae:pathName ?pathname . " +
                "} ORDER BY DESC(?timestamp)";
        ;
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_all_file_access_events_1() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:md5CheckSum ?md5CheckSum . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +

                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +

                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +

                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY DESC(?timestamp) " +
                "LIMIT 1";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_LAST_event_of_file() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( str(?pathNameSource) = \"/Users/Agnes/Dropbox/test/textedit3.rtf\" )  . " +
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
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_PREVIOUS_event_of_file() {
        String lastTimestamp = "\"2019-01-13T18:00:11Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>";
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( str(?pathNameSource) = \"/Users/Agnes/Desktop/test/textwrangler2.txt\" )  . " +
                "FILTER (?timestamp < " + lastTimestamp + " ) . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +

                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +

                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY DESC(?timestamp) " +
                "LIMIT 1";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_all_file_access_events_2() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( str(?actionName) = \"Renamed\" )  . " +
                "FILTER ( str(?pathnameTarget) = \"/Users/Agnes/Desktop/test/textwrangler2.txt\" )  . " +
                //"FILTER ( str(?pathNameSource) = \"/Users/Agnes/Desktop/test/textwrangler2.txt\" )  . " +

                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +

                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +

                "?s fae:hasUser/fae:userName ?username . " +

                "} ORDER BY ASC(?timestamp)";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_all_file_access_events_all_filename() {
        String range = "\"2019-01-13T18:03:42Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>";
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT DISTINCT ?timestamp ?pathname WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "FILTER (?timestamp > " + range + ") . " +
                "?s fae:hasSourceFile/fae:fileName ?fileName . " +
                "FILTER REGEX (?fileName , \"[a-zA-Z0-9_ :~$]+[.][a-zA-Z0-9]{2,5}\") ." +
                "?s fae:hasSourceFile/fae:pathName ?pathname . " +
                "}ORDER BY ASC(?timestamp)";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_all_file_access_events_all_filename_range() {
        String range = "2019-01-20T12:49:21.000Z";
        String range1 = "2019-01-20T12:49:35.000Z";
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT DISTINCT ?timestamp ?pathname WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                //    "FILTER (?timestamp > \""+range+"\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) . " +
                "?s fae:hasSourceFile/fae:fileName ?fileName . " +
                "FILTER REGEX (?fileName , \"[a-zA-Z0-9_ :~$]+[.][a-zA-Z0-9]{2,5}\") ." +
                "?s fae:hasSourceFile/fae:pathName ?pathname . }";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_all_file_access_events_all_actions_of_filename() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT DISTINCT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileName . " +
                "?s fae:hasSourceFile/fae:pathName ?pathname . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( str(?pathname) = \"/Users/Agnes/Desktop/test/excel.xlsx\" )  . " +
                //      "FILTER ( str(?pathnameTarget) = \"/Users/Agnes/.Trash/newword2.docx\" )  ."+
                //   "FILTER ( ?timestamp <  \"2019-01-13T18:01:37Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )  . "+
                //  "?s fae:hasSourceFile/fae:pathname ?pathnameSource . " +

                //    "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                //   "?s fae:hasTargetFile/fae:pathname ?pathnameTarget . " +

                //   "?s fae:hasSourceHost/fae:hostname ?hostnameSource . " +
                //   "?s fae:hasTargetHost/fae:hostname ?hostnameTarget . " +

                //    "?s fae:hasUser/fae:username ?username . " +

                "} ORDER BY DESC(?timestamp)";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_orignin_test() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( str(?pathnameTarget) = \"/Users/Agnes/.Trash/newword2.docx\" )  ." +
                "FILTER ( ?timestamp <  \"2019-01-13T18:01:37Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )  . " +

                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +

                "?s fae:hasUser/fae:userName ?username . " +

                "} ORDER BY ASC(?timestamp)";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_origin_test1() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( str(?pathnameTarget) = \"/Users/Agnes/Desktop/test/textwrangler copy.txt\" || str(?pathNameSource) = \"/Users/Agnes/Desktop/test/textwrangler copy.txt\" ) ." +

                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +

                "?s fae:hasUser/fae:userName ?username . " +

                "} ORDER BY ASC(?timestamp)";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_orignin_test2() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                //  "?s fae:id ?id . " +
                //     "FILTER ( str(?s) != <http://purl.org/sepses/vocab/event/fileAccess#28987865-e0a8-4a58-b3c3-16a02298d86f> )  . " +
                "?s fae:timestamp ?timestamp . ?s fae:hasAction/fae:actionName ?actionName . ?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . ?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . ?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( str(?pathnameTarget) = \"/Users/Agnes/Desktop/test/~$excel.xlsx\" )  . " +
                "FILTER ( ?timestamp <=  \"2019-01-13T17:59:30Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )  . " +
                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . ?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +
                "?s fae:hasUser/fae:userName ?username . " +
                "FILTER ( ?s != <http://purl.org/sepses/vocab/event/fileAccess#09b1801b-9abf-4b80-baa9-3ac4e47b7d45> )  .  " +
                "} ORDER BY ASC(?timestamp)";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_process_Info() {
        String query = "PREFIX process: <http://sepses.ifs.tuwien.ac.at/vocab/processInfo#> " +
                "SELECT ?s ?p ?o WHERE { " +
                "?s ?p ?o . " +
                "?s process:timestamp ?timestamp . " +
                "} " +
                "ORDER BY ASC(?timestamp) " +
                "LIMIT 100";
        logConverterService.printRecords(query, "tdb/DB_ProcessInfo");
    }

    @Test
    public void query_test_get_process_Info_1() {
        String sparqlQueryString = "PREFIX process: <http://sepses.ifs.tuwien.ac.at/vocab/processInfo#> " +
                "SELECT ?pid ?name ?timestamp WHERE { " +
                "?s process:pid ?pid . " +
                "FILTER(str(?pid)=\"337\")" +
                //  "?s process:pid \"22383\" . " +
              /*  "?s process:operation \"start\" . " +
                "?s process:processName ?name . " +
                "?s process:timestamp ?timestamp . " +
                "FILTER not exists {" +
                "  ?s process:timestamp ?after" +
                "  filter (?after > ?timestamp) ." +
                "}" +*/
                "}";
        logConverterService.printRecords(sparqlQueryString, "tdb/DB_ProcessInfo");
    }

    @Test
    public void query_test_get_process_Info_2() {
        String dateText = "2018-12-29T14:04:51Z";//yyyy-MM-dd'T'HH:mm:ss'Z'
        String query = "PREFIX process: <http://sepses.ifs.tuwien.ac.at/vocab/processInfo#> " +
                "SELECT * " +
                "WHERE { " +
                // "?s1 process:pid \"15235\" . " +
                "?s1 process:operation \"start\" . " +
                "?s1 process:processName ?name1 . " +
                "?s1 process:timestamp ?t1 . " +
                "OPTIONAL {?s2 process:operation \"exit\" . }" +
                "OPTIONAL {?s2 process:processName ?name2 . }" +
                "OPTIONAL {?s2 process:timestamp ?t2 . }" +
                "FILTER (?name1 = ?name2 ) . " +
                //    "FILTER (?t1 < \""+dateText+"\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                //    "FILTER (?t2 >= \""+dateText+"\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "}";
        logConverterService.printRecords(query, "tdb/DB_ProcessInfo");
    }

    @Test
    public void query_test_get_process_Info_3() {
        String dateText = "2019-01-02T12:00:23Z";//yyyy-MM-dd'T'HH:mm:ss'Z' 2019-01-02T12:17:40Z
        String query = "PREFIX process: <http://sepses.ifs.tuwien.ac.at/vocab/processInfo#> " +
                "SELECT ?name " +
                "WHERE { " +
                "?s process:pid \"22383\" . " +
                //   "?s process:operation \"start\" . " +
                "?s process:processName ?name . " +
                "?s process:timestamp ?timestamp . " +
                //   "FILTER (?t1 < \""+dateText+"\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "} ORDER BY DESC(?timestamp) ";
        logConverterService.printRecords(query, "tdb/DB_ProcessInfo");
    }


    @Test
    public void getProcessNameByPid() {
        String pid = "19664";
        String pid1 = "20365";
        String pid2 = "321";
        String timestamp = "2019-01-19T19:04:36.000Z";
        TDBConnection tdb = new TDBConnection();
        String processName = tdb.getProcessNameByPid(pid2, timestamp);
        System.out.println("#################### PROCESS NAME: " + processName);
    }

    @Test
    public void query_test_get_logentries() {
        String query = "SELECT ?s ?p ?o WHERE{ ?s ?p ?o } LIMIT 100";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void query_test_get_previous_log_entry() {
        String query = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "FILTER ( str(?accessCall) = \"getattrlist()\" ) .  " +
                "?logEntry file:hasFile/file:pathname ?pathname .  " +
                "FILTER ( CONTAINS(str(?pathname), \"/Users/Agnes/Desktop/sample/excel.xlsx\") ) . " +
                "?logEntry file:timestamp ?timestamp . " +
                "FILTER ( ?timestamp < \"2019-03-22T07:00:45Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:logMessage ?logMessage " +
                "} ORDER BY ASC(?timestamp) Limit 1";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void query_test_get_moved() {
        String query =
                "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                        "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                        //   "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
                        "WHERE { " +
                        "?logEntry file:accessCall ?accessCall . " +
                        "?logEntry file:hasFile/file:pathname ?pathname .  " +
                        "FILTER ( str(?accessCall) = \"rename(2)\" ) .  " +
                        "BIND ( STRAFTER(str(?pathname), \",\") AS ?temp)" +
                        "BIND ( STRBEFORE(str(?temp), \",\") AS ?path1)" +
                        "BIND ( STRAFTER(str(?temp), \",\") AS ?path2)" +
                        "BIND ( STRAFTER(REPLACE(?path1, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?oldFilename)" + //extract filename from path
                        "BIND ( STRAFTER(REPLACE(?path2, \"(/[a-zA-Z0-9-_ :.]+)+\", \"$1\"), \"/\") AS ?newFilename)" + //extract filename from path
                        "BIND ( STRBEFORE(str(?path1), ?oldFilename) AS ?directory1)" +
                        "BIND ( STRBEFORE(str(?path2), ?newFilename) AS ?directory2)" +
                        "FILTER ( ?directory1 != ?directory2 ) . " +
                        "FILTER ( !CONTAINS(?path2, \"/.Trash/\") ) " +
                        "?logEntry file:hasProcess/file:processID ?processID . " +
                        "?logEntry file:hasUser/file:username ?username . " +
                        "?logEntry file:originatesFrom/file:hostName ?host . " +
                        "?logEntry file:timestamp ?timestamp . " +
                        "?logEntry file:logMessage ?logMessage " +
                        "}";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void query_test_get_logentries_deleted() {
        String query = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                "WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "?logEntry file:hasFile/file:pathname ?pathname .  " +
                "FILTER ( str(?accessCall) = \"unlink(2)\" ) . " +
                // "FILTER ( CONTAINS(?pathname, \"/.Trash/\") ) " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:timestamp ?timestamp . " +
                "?logEntry file:logMessage ?logMessage " +
                "}";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void query_test_get_logentries_by_id() {
        String query = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                "WHERE { " +
                "?logEntry file:id \"8f9552b9-f0b7-44ab-b990-9ed1fe20f0f1\" . " +
                "?logEntry file:hasFile/file:pathname ?pathname .  " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:timestamp ?timestamp . " +
                "?logEntry file:logMessage ?logMessage " +
                "}";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }


    @Test
    public void query_test_get_fae_by_id() {
        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT ?action ?pathname ?timestamp " +
                "WHERE { " +
                //   "fae:8f9552b9-f0b7-44ab-b990-9ed1fe20f0f1 fae:id \"8f9552b9-f0b7-44ab-b990-9ed1fe20f0f1\" . " +
                "fae:8f9552b9-f0b7-44ab-b990-9ed1fe20f0f1 fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "fae:8f9552b9-f0b7-44ab-b990-9ed1fe20f0f1 fae:hasAction/fae:actionName ?action . " +
                "fae:8f9552b9-f0b7-44ab-b990-9ed1fe20f0f1 fae:timestamp ?timestamp . " +
                "}";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
    }

    @Test
    public void query_test_get_logentries_check_getattrlist_of_pathname() {
        String query2 = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "SELECT distinct ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                "WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "FILTER ( str(?accessCall) = \"getattrlist()\" ) . " +
                "?logEntry file:hasFile/file:pathname ?pathname .  " +
                "FILTER ( CONTAINS(?pathname, \"/Users/Agnes/Desktop/test/excel.xlsx\") ) " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:timestamp ?timestamp . " +
                "FILTER (?timestamp < \"2019-03-22T07:00:39.000Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "?logEntry file:logMessage ?logMessage " +
                "}  ORDER BY ASC(?timestamp)";
        logConverterService.printRecords(query2, "tdb/DB_LogEntry");
    }


    @Test
    public void query_test_get_logentries_of_file_name_same_directory() {
        String timestamp = "2019-01-13T18:00:06Z";
        String pathname = "/Users/Agnes/Desktop/test/textwrangler.txt";
        String query = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                "WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "FILTER ( str(?accessCall) = \"getattrlist()\" ) .  " +
                "?logEntry file:hasFile/file:pathname ?pathname .  " +
                "FILTER ( CONTAINS(str(?pathname), \"" + pathname + "\") ) . " +
                "?logEntry file:timestamp ?timestamp . " +
                "FILTER ( ?timestamp < \"" + timestamp + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:logMessage ?logMessage " +
                "} ORDER BY ASC(?timestamp)" +
                " Limit 1";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void query_test_get_logentries_of_file_name_diff_directory() {
        String timestamp = "2019-01-13T18:01:06Z";
        String path = "/Users/Agnes/Desktop/test/";
        String filename = "textedit3.rtf";
        String query = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage WHERE { " +
                "?logEntry file:accessCall ?accessCall . " +
                "FILTER ( str(?accessCall) = \"getattrlist()\" ) .  ?" +
                "logEntry file:hasFile/file:pathname ?pathname .  " +
                "BIND ( STRDT(STRAFTER(REPLACE(STRBEFORE(str(?pathname),\",\"), \"(/[a-zA-Z0-9-_ :.~$]+)+\", \"$1\"), \"/\"), xsd:string) AS ?filename) . " +
                "BIND ( STRDT(STRBEFORE(str(?pathname), ?filename), xsd:string) AS ?path) . " +
                "FILTER ( CONTAINS(str(?pathname), \"" + filename + "\") ) . " +
                "FILTER ( ?path != \"" + path + "\" ) . " +
                "FILTER ( ?filename = \"" + filename + "\" ) . " +
                "?logEntry file:timestamp ?timestamp . " +
                "FILTER ( ?timestamp < \"" + timestamp + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                "?logEntry file:hasProcess/file:processID ?processID . " +
                "?logEntry file:hasUser/file:username ?username . " +
                "?logEntry file:originatesFrom/file:hostName ?host . " +
                "?logEntry file:logMessage ?logMessage " +
                "} ORDER BY ASC(?timestamp) Limit 1";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void query_test_get_logentries_created_modified() {
        String query =
                //"REGISTER QUERY created AS " +
                "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                        "SELECT DISTINCT ?logEntry ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                        //  "SELECT DISTINCT * " +
                        // "FROM STREAM <http://streamreasoning.org/streams/fs> [RANGE 10s STEP 5s] " +
                        "WHERE { " +
                        "?logEntry file:accessCall ?accessCall . " +
                        "FILTER ( str(?accessCall) = \"setattrlist()\" ) .  " +
                        "?logEntry file:hasFile/file:pathname ?pathname .  " +
                        "BIND ( STRAFTER(str(?pathname), \",\") AS ?path1) . " +
                        "?logEntry2 file:accessCall ?accessCall2 . " +
                        "FILTER ( str(?accessCall2) = \"getattrlist()\" ) .  " +
                        "?logEntry2 file:hasFile/file:pathname ?pathname2 .  " +
                        "BIND ( STRAFTER(str(?pathname2), \",\") AS ?path2) . " +
                        "FILTER ( ?path1 = ?path2 ) . " +
                        "?logEntry file:timestamp ?timestamp . " +
                        "?logEntry2 file:timestamp ?timestamp2 . " +
                        "FILTER ( ?timestamp > ?timestamp2 ) . " +
                       /* "FILTER not exists {" +
                        "  ?logEntry2 file:timestamp ?after" +
                        "  filter (?after > ?timestamp2) ." +
                        "}" +*/
                        "?logEntry file:hasProcess/file:processID ?processID . " +
                        "?logEntry file:hasUser/file:username ?username . " +
                        "?logEntry file:originatesFrom/file:hostName ?host . " +
                        "?logEntry file:logMessage ?logMessage " +
                        "} Limit 500";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void query_test_get_logentries_of_id() {
        String query = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "SELECT * WHERE{ " +
                //  "?logEntry file:id ?id . " +
                // "FILTER ( str(?id) = \"11e2bb40-8988-42c1-9b53-c1077029489e\" ) ." +
                "?logEntry file:hasFile/file:pathname ?pathname . " +
                "?logEntry file:hasProcess/file:processID ?pid . " +
                "}";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void query_test_get_logentries_action() {
        String query = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                "SELECT * WHERE{ " +
                "?logEntry file:accessCall ?accessCall . " +
                //   "FILTER ( str(?accessCall) = \"setattrlist()\" ) ." +
                "?logEntry file:hasFile/file:pathname ?pathname . " +
                "FILTER ( str(?pathname) = \"/Users/Agnes/Desktop/test/excel.xlsx\" )  . " +
                "?logEntry file:hasProcess/file:processID ?pid . " +
                "}";
        logConverterService.printRecords(query, "tdb/DB_LogEntry");
    }

    @Test
    public void test_date_convert() throws ParseException {
        String dateString = "Wed Oct 3 12:46:55 2018";
        Date date = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy").parse(dateString);
        String dateString2 = "Wed Oct 3 12:50:55 2018";
        Date date2 = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy").parse(dateString2);
        boolean date1AfterDate2 = date.after(date2);
        long timeDifferenceMilliseconds;
        if (date1AfterDate2) {
            System.out.println("date1 '" + date + "' is after date2 '" + date2);
            timeDifferenceMilliseconds = date2.getTime() - date.getTime();
        } else {
            System.out.println("date1 '" + date + "' is before date2 '" + date2);
            timeDifferenceMilliseconds = date.getTime() - date2.getTime();
        }
        long diffSeconds = timeDifferenceMilliseconds / 1000;
        long diffMinutes = timeDifferenceMilliseconds / (60 * 1000);
        System.out.println(diffSeconds);
        System.out.println(diffMinutes);

        String string_date = "05-October-2018 13:44:00";

        SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        long searchTimestamp = 0;
        try {
            Date d = f.parse(string_date);
            searchTimestamp = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int tenMinutes = 10 * 60 * 1000;
        long tenAgo = System.currentTimeMillis() - tenMinutes;
        if (searchTimestamp < tenAgo) {
            System.out.println("searchTimestamp is older than 10 minutes");
        } else {
            System.out.println("searchTimestamp is within the last 10 minutes");
        }

        Date thresholdTime = new Date(System.currentTimeMillis() - 3600);
        String parseDateToString = ServiceUtil.parseDateToString(thresholdTime, "yyyy-MM-d'T'HH:mm:ss+hh:mm");
        System.out.println(parseDateToString);
    }
}
