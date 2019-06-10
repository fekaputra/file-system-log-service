import ac.at.tuwien.logparser.entities.Channel;
import ac.at.tuwien.logparser.entities.FileAccessEvent;
import ac.at.tuwien.logparser.entities.Person;
import ac.at.tuwien.logparser.services.LogConverterService;
import ac.at.tuwien.logparser.tdb.TDBConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = {"at.ac.tuwien.logparser.service"})
@ContextConfiguration(classes = {LogConverterService.class})
public class BackgroundKnowledge {

    @Autowired
    private LogConverterService logConverterService;
    private TDBConnection tdb;

    @PostConstruct
    public void init() {
        tdb = new TDBConnection();
    }

    @Test
    public void create_Background_Knowledge_Resources() {
        Person p = new Person("Agnes", "Froeschl", "agnes.froeschl@gmx.at", Arrays.asList("Agnes"));
        p.setId(UUID.randomUUID().toString());

        Channel dropbox = new Channel(UUID.randomUUID().toString(), "extern", "Dropbox", "/Users/Agnes/Dropbox/", "Dropbox");
        Channel googledrive = new Channel(UUID.randomUUID().toString(), "extern", "Google Drive", "/Users/Agnes/Google Drive/", "Backup and sync from Google");
        Channel usb = new Channel(UUID.randomUUID().toString(), "extern", "USB", "/Volumes/", null);
        Channel email = new Channel(UUID.randomUUID().toString(), "extern", "E-Mail", "/Users/Agnes/Library/Containers/com.apple.mail/Data/Library/Mail Downloads/", "Mail");
        Channel smb = new Channel(UUID.randomUUID().toString(), "extern", "SMB", null, null);
        Channel ssh = new Channel(UUID.randomUUID().toString(), "extern", "SSH", null, null);
        Channel ftp = new Channel(UUID.randomUUID().toString(), "extern", "FTP", null, null);

        Channel test = new Channel(UUID.randomUUID().toString(), "intern", "Folder", "/Users/Agnes/Desktop/test/", null);
        Channel test2 = new Channel(UUID.randomUUID().toString(), "intern", "Folder", "/Users/Agnes/Desktop/test-2/", null);
        Channel sample = new Channel(UUID.randomUUID().toString(), "intern", "Folder", "/Users/Agnes/Desktop/sample/", null);

        TDBConnection tdb = new TDBConnection();
        tdb.createPersonResource(p);
        tdb.createChannelResource(dropbox);
        tdb.createChannelResource(googledrive);
        tdb.createChannelResource(usb);
        tdb.createChannelResource(email);
        tdb.createChannelResource(test);
        tdb.createChannelResource(test2);
        tdb.createChannelResource(sample);
    }

    @Test
    public void getBackgroundKnowledge() {
        String query = "SELECT ?s ?p ?o WHERE{ ?s ?p ?o }";
        logConverterService.printRecords(query, "tdb/DB_Background");
    }

    @Test
    public void test1() {
        String query = "PREFIX h: <http://sepses.ifs.tuwien.ac.at/vocab/history#> " +
                "SELECT * WHERE { " +
                "?s ?p ?o . " +
                "?s h:sourceFileName ?filename . " +
                "?s h:timestamp ?timestamp . " +
                "FILTER(str(?filename) = \"/Volumes/AGNES/1550498964.docx\") . " +
                "FILTER(?timestamp <= \"2019-02-18T13:35:04Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) . " +
                "}";
        logConverterService.printRecords(query, "tdb/DB_History");
    }

    @Test
    public void test_queries_by_attributes() {
        List<Channel> externalChannels = tdb.getChannelsByType("extern");
        List<Channel> internalChannels = tdb.getChannelsByType("intern");
        Channel dropboxChannel = tdb.getChannelByName("Dropbox");
        Channel USBChannel = tdb.getChannelByPath("/Users/Agnes/Volumes/");
        Channel MailChannel = tdb.getChannelByProgram("Mail");
        System.out.println("finish");
    }

    @Test
    public void getCopyOrMoveFromExternToIntern() {
        List<Channel> internalChannels = tdb.getChannelsByType("intern");
        List<Channel> externalChannels = tdb.getChannelsByType("extern");
        String inChannelString = "";
        for (int i = 0; i < internalChannels.size(); i++) {
            if (i != internalChannels.size() - 1) {
                inChannelString += " CONTAINS(?pathnameTarget, \""+internalChannels.get(i).getPath() +"\") || ";
            } else {
                inChannelString += " CONTAINS(?pathnameTarget, \""+internalChannels.get(i).getPath() +"\") ";
            }
        }
        String exChannelString = "";
        for (int i = 0; i < externalChannels.size(); i++) {
            if (i != externalChannels.size() - 1) {
                exChannelString += " CONTAINS(?pathNameSource, \""+externalChannels.get(i).getPath() +"\") || ";
            } else {
                exChannelString += " CONTAINS(?pathNameSource, \""+externalChannels.get(i).getPath() +"\") ";
            }
        }

        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "FILTER ( str(?actionName) = \"Created_Copied\" || str(?actionName) = \"Moved\" ) . " +
                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( " + exChannelString + " ) " +

                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( " + inChannelString + " ) " +

                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +

                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY DESC(?timestamp) ";
       // logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
        List<FileAccessEvent> events = logConverterService.extractFileAccessEventsFromQueryResult(query);
        for(FileAccessEvent event: events) {
            LinkedHashMap<String, FileAccessEvent> history = new LinkedHashMap<>();
           // logConverterService.createHistoryFromPathnameAndTime(event, history);
            logConverterService.createNTFileforHistory(history, "copy_from_extern_to_intern.nt");
        }
        /*
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| s                                                                                    | timestamp                                                           | actionName       | programName | fileNameSource  | pathNameSource                 | fileNameTarget  | pathnameTarget                              | hostnameSource                  | hostnameTarget                  | username |
=================================================================================================================================================================================================================================================================================================================================================================================================
| <http://purl.org/sepses/vocab/event/fileAccess#08a4f11a-670d-4de9-8955-bec65316d9f5> | "2019-01-19T10:24:51Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> | "Created_Copied" | _:b0        | "textedit3.rtf" | "/Volumes/AGNES/textedit3.rtf" | "textedit3.rtf" | "/Users/Agnes/Desktop/test-2/textedit3.rtf" | "e241-244.eduroam.tuwien.ac.at" | "e241-244.eduroam.tuwien.ac.at" | "Agnes"  |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
         */
    /*
    performed on 4.3.2019
 -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| s                                                                                    | timestamp                                                           | actionName       | programName | fileNameSource    | pathNameSource                             | fileNameTarget    | pathnameTarget                                | hostnameSource                  | hostnameTarget                  | username |
===================================================================================================================================================================================================================================================================================================================================================================================================================
| <http://purl.org/sepses/vocab/event/fileAccess#62d4a648-e870-48a6-b680-7735d892d658> | "2019-02-18T13:35:04Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> | "Moved"          | "mv"        | "1550498964.docx" | "/Volumes/AGNES/1550498964.docx"           | "1550498964.docx" | "/Users/Agnes/Desktop/test-2/1550498964.docx" | "e241-059.eduroam.tuwien.ac.at" | "e241-059.eduroam.tuwien.ac.at" | "Agnes"  |
| <http://purl.org/sepses/vocab/event/fileAccess#bb00a03a-2459-4ce2-a1c6-987fb115fdf3> | "2019-02-18T12:33:20Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> | "Created_Copied" | "xpcproxy"  | "testdoc2X.docx"  | "/Users/Agnes/Dropbox/test/testdoc2X.docx" | "testdoc2X.docx"  | "/Users/Agnes/Desktop/test/testdoc2X.docx"    | "e241-059.eduroam.tuwien.ac.at" | "e241-059.eduroam.tuwien.ac.at" | "Agnes"  |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     */
    }

    @Test
    public void getCopyOrMoveToExtern() {
        List<Channel> internalChannels = tdb.getChannelsByType("intern");
        List<Channel> externalChannels = tdb.getChannelsByType("extern");
        String inChannelString = "";
        for (int i = 0; i < internalChannels.size(); i++) {
            if (i != internalChannels.size() - 1) {
                inChannelString += " CONTAINS(?pathNameSource, \""+internalChannels.get(i).getPath() +"\") || ";
            } else {
                inChannelString += " CONTAINS(?pathNameSource, \""+internalChannels.get(i).getPath() +"\") ";
            }
        }
        String exChannelString = "";
        for (int i = 0; i < externalChannels.size(); i++) {
            if (i != externalChannels.size() - 1) {
                exChannelString += " CONTAINS(?pathnameTarget, \""+externalChannels.get(i).getPath() +"\") || ";
            } else {
                exChannelString += " CONTAINS(?pathnameTarget, \""+externalChannels.get(i).getPath() +"\") ";
            }
        }

        String query = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT * WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasAction/fae:actionName ?actionName . " +
                "FILTER ( str(?actionName) = \"Created_Copied\" || str(?actionName) = \"Moved\" ) . " +

                "?s fae:hasProgram/fae:programName ?programName . " +
                "?s fae:hasSourceFile/fae:fileName ?fileNameSource . " +
                "?s fae:hasSourceFile/fae:pathName ?pathNameSource . " +
                "FILTER ( " + inChannelString + " ) " +

                "?s fae:hasTargetFile/fae:fileName ?fileNameTarget . " +
                "?s fae:hasTargetFile/fae:pathName ?pathnameTarget . " +
                "FILTER ( " + exChannelString + " ) " +

                "?s fae:hasSourceHost/fae:hostName ?hostnameSource . " +
                "?s fae:hasTargetHost/fae:hostName ?hostnameTarget . " +

                "?s fae:hasUser/fae:userName ?username . " +
                "} ORDER BY DESC(?timestamp) ";
        logConverterService.printRecords(query, "tdb/DB_FileAccessEvent");
        List<FileAccessEvent> events = logConverterService.extractFileAccessEventsFromQueryResult(query);
        for(FileAccessEvent event: events) {
            LinkedHashMap<String, FileAccessEvent> history = new LinkedHashMap<>();
         //   logConverterService.createHistoryFromPathnameAndTime(event, history);
            logConverterService.createNTFileforHistory(history, "copy_or_move_to_extern.nt");
        }
/*
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| s                                                                                    | timestamp                                                           | actionName | programName | fileNameSource  | pathNameSource                                       | fileNameTarget  | pathnameTarget                            | hostnameSource                  | hostnameTarget                  | username |
===============================================================================================================================================================================================================================================================================================================================================================================================================
| <http://purl.org/sepses/vocab/event/fileAccess#91fda10d-6e5f-48f9-b437-f42b076d3a5f> | "2019-01-19T11:23:34Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> | "Moved"    | _:b0        | "textedit3.rtf" | "/Users/Agnes/Desktop/test/new folder/textedit3.rtf" | "textedit3.rtf" | "/Users/Agnes/Dropbox/test/textedit3.rtf" | "e241-244.eduroam.tuwien.ac.at" | "e241-244.eduroam.tuwien.ac.at" | "Agnes"  |
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 */
    }


}
