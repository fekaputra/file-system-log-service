package ac.at.tuwien.logparser.services;

import ac.at.tuwien.logparser.entities.*;
import ac.at.tuwien.logparser.entities.enums.FileAccessType;
import ac.at.tuwien.logparser.entities.schema.File_access_events;
import ac.at.tuwien.logparser.entities.schema.History_schema;
import ac.at.tuwien.logparser.services.util.ServiceUtil;
import ac.at.tuwien.logparser.tdb.TDBConnection;
import ac.at.tuwien.logparser.view.transfer.Element;
import ac.at.tuwien.logparser.view.transfer.Node;
import com.google.gson.Gson;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


/**
 * Created by Agnes on 04.09.18.
 */
@Service
public class LogConverterService {

    private final static Logger logger = Logger.getLogger(LogConverterService.class.getName());
    private static String ipExternal;
    private static String time;
    private static TDBConnection tdb;
    private static Map<String, LogEntry> eventCache;
    private static Map<String, LogEntry> eventCache2;

    @PostConstruct
    public void init() {
        tdb = new TDBConnection();
        eventCache = new ConcurrentHashMap<>();
        eventCache2 = new ConcurrentHashMap<>();
        time = ServiceUtil.getCurrentTimeStamp();
    }

    public void handleFileEvent(LogEntry entry, FileAccessType fileAccessType) {
        // add ip Address to log entry
        entry.getOriginatesFrom().setIp4Address(ipExternal);
        entry.getOriginatesFrom().setIpAddress(ipExternal);

        if (!tdb.existsFileAccessEventResource(entry.getId(), fileAccessType.name())) {
            eventCache.put(entry.getId(), entry); //put logentry into event cache - used for processInfo update
            FileAccessEvent event = this.parseLogEntryToFileAccessEvent(entry, fileAccessType);
            tdb.createFileAccessEventResource(event);
        }
    }

    public FileAccessEvent parseLogEntryToFileAccessEvent(LogEntry entry, FileAccessType fileAccessType) {
        String[] paths = entry.getHasFile().getPathname().split(",");
        Host host = new Host(entry.getOriginatesFrom().getHostname());
        File sourceFile = new File(new java.io.File(paths[0]).getName(), ServiceUtil.removeLeadingDoubleSlash(paths[0]));
        File targetFile = new File(new java.io.File(paths[paths.length - 1]).getName(), ServiceUtil.removeLeadingDoubleSlash(paths[paths.length - 1]));
        User user = new User(entry.getHasUser().getUsername());
        String timestamp = ServiceUtil.getTimestampFromXSDDate(entry.getTimestamp());
        String processName = tdb.getProcessNameByPid(entry.getHasProcess().getProcessID(), timestamp);
        Program program = new Program(processName, entry.getHasProcess().getProcessID());
        Action action = new Action(fileAccessType.name());
        FileAccessEvent event = new FileAccessEvent(entry.getId(), entry.getTimestamp(),
                null, action, host, host, sourceFile, targetFile, user, program);
        return event;
    }

    public void handleProcessInfo(ProcessInfo info) {
        if (!tdb.existsProcessInfoResource(info))
            tdb.createProcessInfoResource(info);
    }

    @Scheduled(fixedDelay = 60000)
    public void processCache() {
        System.out.println("################################### SCHEDULED TASK #########################################");

        String tempTime = time;

        // set time again
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date now = new Date();
        Date temp = new Date(now.getTime() - 2 * 60 * 60 * 1000); //substract 2 h
        time = sdfDate.format(temp);

        // Update program name of all FileAccessEvents which got created from cached events
        synchronized (eventCache) {
            eventCache.putAll(eventCache2);
            eventCache2.clear();
            for (Map.Entry<String, LogEntry> entry : eventCache.entrySet()) {
                String resourceURI = File_access_events.getURI() + entry.getKey() + "-Program";
                String processName = tdb.getProcessNameByPid(entry.getValue().getHasProcess().getProcessID(),
                        ServiceUtil.getTimestampFromXSDDate(entry.getValue().getTimestamp()));
                if (processName != null && !processName.isEmpty()) { //TODO add better check for programname?? how to recognise a blanknode?
                    tdb.updateProgramNamOfFileAccessEventResource(resourceURI, processName);
                } else {
                    eventCache2.put(entry.getValue().getId(), entry.getValue());
                }
            }
            eventCache.clear();
        }

        //TODO use this method in live streaming examples to get only filenames from last time range, since last scheduling task
         List<String> pathNames = this.getAllFilenamesOfFileAccessEvents(tempTime); // get pathnames in last timestamp range since last scheduling task
      //  List<String> pathNames = this.getAllFilenamesOfFileAccessEvents();
        for (String name : pathNames) {
            if (name.contains(".tmp")) continue; //skip temp files
            String path[] = name.split("/");
            String filename = path[path.length - 1];
            if (filename.matches("~\\$.*\\..*")) continue;
            if (name.equals("/Users/Agnes/Desktop/test/excel.xlsx")) continue; //TODO remove again

            System.out.println("--------- Result of file with name: \"" + name + "\""); ///Users/Agnes/Desktop/test/excel.xlsx
            FileAccessEvent event = this.getLastEventOfFilepath(name);
            if (event != null) {

                LinkedHashMap<String, FileAccessEvent> history = new LinkedHashMap<>();

                // create MD5 checksum in case an operation on a USB device occurred
                if (name.contains("/Volumes/")) {
                    try {
                        java.io.File file = new java.io.File(name);
                        if (file.exists()) {
                            String checkSumOfFile = ServiceUtil.generateMD5DigestCheckSumOfFile(name);
                            tdb.createCheckSumPropertyOfFileResource(event.getId(), checkSumOfFile);
                        }
                    } catch (NoSuchFileException e) {
                        logger.error(e);
                    }
                }

                this.addEventToHistory(event, history);
                LinkedHashMap<String, FileAccessEvent> excludedFromHistory = new LinkedHashMap<>();
                this.getHistoryOfPathname(event, history, excludedFromHistory);
                this.printHistory(history);
                // this.saveHistory(history);
                System.out.println("\n");

                this.createRelatedToProperty(history);
            }
        }
    }

    public String getHistoryJson(String pathname) {
        LinkedHashMap<String, FileAccessEvent> history = new LinkedHashMap<>();
        FileAccessEvent event = this.getLastEventOfFilepath(pathname);
        if (event != null) {
            this.addEventToHistory(event, history);
            LinkedHashMap<String, FileAccessEvent> excludedFromHistory = new LinkedHashMap<>();
            this.getHistoryOfPathname(event, history, excludedFromHistory);
        }
        List<Node> nodes = new ArrayList<>();
        for (Map.Entry<String, FileAccessEvent> e : history.entrySet()) {
            FileAccessEvent eV = e.getValue();
            nodes.add(this.createNode(ServiceUtil.getTimestampFromXSDDate(eV.getTimestamp()),
                    eV.getHasSourceFile().getPathname(),
                    eV.getHasAction().getActionName(),
                    eV.getHasTargetFile().getPathname(),
                    eV.getHasProgram().getProgramName(),
                    eV.getHasUser().getUsername()));
        }
        Collections.sort(nodes);
        Gson gson = new Gson();
        String json = gson.toJson(nodes);
        System.out.println(json);
        return json;
    }

    private Node createNode(String t, String f, String fa, String tf, String p, String u) {
        Element time = new Element("literal", t);
        Element filename = new Element("literal", f);
        Element filAccess = new Element("literal", fa);
        Element tfilename = new Element("literal", tf);
        Element program = new Element("literal", p);
        Element user = new Element("literal", u);
        Node node = new Node(time, filename, filAccess, tfilename, program, user);
        return node;
    }

    private void printHistory(LinkedHashMap<String, FileAccessEvent> history) {
        for (Map.Entry<String, FileAccessEvent> e : history.entrySet()) {
            FileAccessEvent event = e.getValue();
            System.out.println("id: " + event.getId() +
                    " - action: " + event.getHasAction().getActionName() +
                    " - sourcefile: " + event.getHasSourceFile().getPathname() +
                    " - targetfile: " + event.getHasTargetFile().getPathname() +
                    " - program: " + event.getHasProgram().getProgramName() +
                    " - timestamp: " + event.getTimestamp());
        }
    }

    private void createRelatedToProperty(LinkedHashMap<String, FileAccessEvent> history) {
        FileAccessEvent previousEvent = null;
        for (Map.Entry<String, FileAccessEvent> e : history.entrySet()) {
            if (previousEvent != null) {
                String previousEventURI = File_access_events.getURI() + previousEvent.getId();
                String currentEventURI = File_access_events.getURI() + e.getValue().getId();
                tdb.createRelatedToProperty(previousEventURI, currentEventURI);
            }
            previousEvent = e.getValue();
        }
    }

    public FileAccessEvent getPotentialCopyOperation(FileAccessEvent event) {
        FileAccessEvent copyEvent;
        //2 cases:
        String timestamp = ServiceUtil.getTimestampFromXSDDate(event.getTimestamp());
        String filename = event.getHasSourceFile().getFilename();
        String[] path = event.getHasSourceFile().getPathname().split(filename);
        String[] filetype = filename.split("[.]");
        String regex = " copy.[a-zA-Z0-9]{2,5}";
        boolean copyContained = Pattern.compile(regex).matcher(filename).find();
        if (copyContained) { //1) copied from same directory ("copy" in filename)
            String originalFilename = (filetype.length > 0 ? filename.split(regex)[0] + "." + filetype[filetype.length - 1] : "");
            String originalPathname = (path.length > 0 ? path[0] + originalFilename : originalFilename);
            // check if potentially the constructed original pathname got accessed before
            copyEvent = this.getPreviousFileAccessOfFilename(filename, event.getHasSourceFile().getPathname(), originalFilename, originalPathname, timestamp);
        } else {//2) copied from other directory ==> same filename has been accessed before with different directory path
            copyEvent = this.getPreviousFileAccessOfFilenameInOtherDirectory(filename, path[0], filename, event.getHasSourceFile().getPathname(), timestamp);
        }
        return copyEvent;
    }

    public FileAccessEvent getPreviousFileAccessOfFilenameInOtherDirectory(String targetFilename, String
            path, String sourceFilename, String sourcePathname, String timestamp) {
        String range = null;
        try {
            long time = ServiceUtil.parseDate(timestamp, "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime();
            Date date = new Date(time - 5 * 1000); //substract 5 sec
            range = ServiceUtil.parseDateToString(date, "yyyy-MM-dd'T'HH:mm:ss'Z'");
        } catch (DateParseException e) {
            System.out.println("Date parsing error!");
        }
        String query = Queries.getPreviousFileAccessOfFilenameInOtherDirectory(sourceFilename, path, timestamp, range);
        List<LogEntry> entries = this.extractLogEntryFromQueryResult(query);
        if (entries != null && !entries.isEmpty()) {
            LogEntry e = entries.get(0);
            return this.createCopyFileAccessEvent(e, targetFilename, e.getHasFile().getPathname().split(",")[0], sourceFilename, sourcePathname, true);
        } else {
            return null;
        }
    }

    public FileAccessEvent getPreviousFileAccessOfFilename(String targetFilename, String targetPathname, String
            sourceFilename, String sourcePathname, String timestamp) {
        String query = Queries.getPreviousFileAccessOfFilename(sourcePathname, timestamp);
        List<LogEntry> entries = this.extractLogEntryFromQueryResult(query);
        if (entries != null && !entries.isEmpty()) {
            LogEntry e = entries.get(0);
            return this.createCopyFileAccessEvent(e, sourceFilename, sourcePathname, targetFilename, targetPathname, false);
        } else {
            return null;
        }
    }

    public FileAccessEvent getCreationEventOfCopyInSameDir(String targetFilename, String targetPathname, String
            sourceFilename, String sourcePathname) {
        String query = Queries.getCopySameDirectory(targetPathname);
        List<LogEntry> entries = this.extractLogEntryFromQueryResult(query);
        if (entries != null && !entries.isEmpty()) {
            LogEntry e = entries.get(0);
            return this.createCopyFileAccessEvent(e, sourceFilename, sourcePathname, targetFilename, targetPathname, false);
        } else {
            return null;
        }
    }

    private FileAccessEvent createCopyFileAccessEvent(LogEntry e, String sourceFilename, String
            sourcePathname, String targetFilename, String targetPathname, boolean diffDir) {
        if (diffDir) {
            try {
                java.io.File source = new java.io.File(sourcePathname);
                java.io.File target = new java.io.File(targetPathname);
                if (source.exists() && target.exists()) {
                    String checkSumOfsourceFile = ServiceUtil.generateMD5DigestCheckSumOfFile(sourcePathname);
                    String checkSumOftargetFile = ServiceUtil.generateMD5DigestCheckSumOfFile(targetPathname);
                    if (!checkSumOfsourceFile.equals(checkSumOftargetFile)) {
                        return null; //files are not the same
                    }
                }else{
                    System.out.println("Hash Value for Copy Operation could not be created. Source/Target file cannot be found anymore!");
                }
            } catch (NoSuchFileException e1) {
                System.out.println("error creating files checksums because file does not exist");
                logger.error("error creating files checksums because file does not exist :" + e1);
            }
        }

        String timestamp = ServiceUtil.getTimestampFromXSDDate(e.getTimestamp());
        //String processName = tdb.getProcessNameByPid(e.getHasProcess().getProcessID(), timestamp); //commented out because causes performance issues
        // create a new File Access Event of type Created/Copied
        FileAccessEvent copyEvent = null;
        try {
            //parse date string to correct format in order to save it as FileAccessEvent Resource
            String timestampEvent = ServiceUtil.parseDateToString(
                    ServiceUtil.parseDate(timestamp, "yyyy-MM-dd'T'HH:mm:ss'Z'"),
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String id = ServiceUtil.extractIdFromUrL(e.getId());
            copyEvent = new FileAccessEvent(id, timestampEvent,
                    null, new Action(FileAccessType.Created_Copied.toString()),
                    new Host(e.getOriginatesFrom().getHostname()), new Host(e.getOriginatesFrom().getHostname()),
                    new File(sourceFilename, sourcePathname), new File(targetFilename, targetPathname),
                    new User(e.getHasUser().getUsername()), new Program(""));
            // save newly created File Access Event of potential copy operation
            if (!tdb.existsFileAccessEventResource(e.getId(), FileAccessType.Created_Copied.name()))
                tdb.createFileAccessEventResource(copyEvent);
        } catch (DateParseException e1) {
            System.out.println("ERROR: Date cannot parsed for copy event!");
        }
        String getCopyEventQuery = Queries.getFileAccessEventById(copyEvent.getId());
        List<FileAccessEvent> event = this.extractFileAccessEventsFromQueryResult(getCopyEventQuery);
        return event.get(0);
    }

    private boolean equalEvent(FileAccessEvent event1, FileAccessEvent event2) {
        boolean equal = false;
        try {
            boolean inTimeRange;
            if (event1.getTimestamp().equals(event2.getTimestamp()))
                inTimeRange = true;
            else {
                long timeEvent1 = ServiceUtil.parseDate(event1.getTimestamp(), "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime();
                Date date = new Date(timeEvent1 - 10 * 1000); //substract 10 sec
                long timeEvent2 = ServiceUtil.parseDate(event2.getTimestamp(), "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime();
                inTimeRange = timeEvent2 >= date.getTime();
            }
            if (event1.getHasAction().getActionName().equals(event2.getHasAction().getActionName())
                    && inTimeRange
                    && event1.getHasSourceFile().getPathname().equals(event2.getHasSourceFile().getPathname())
                    && event1.getHasTargetFile().getPathname().equals(event2.getHasTargetFile().getPathname()))
                equal = true;
        } catch (DateParseException e) {
            e.printStackTrace();
        }
        return equal;
    }

    private LinkedHashMap<String, FileAccessEvent> getHistoryOfPathname(FileAccessEvent event,
                                                                        LinkedHashMap<String, FileAccessEvent> history,
                                                                        LinkedHashMap<String, FileAccessEvent> excludedFromHistory) {
        String lastTimestamp = ServiceUtil.getTimestampFromXSDDate(event.getTimestamp());
        FileAccessEvent lastEvent = this.getLastEventOfFilepathAndTimestamp(event.getHasSourceFile().getPathname(), lastTimestamp, history, excludedFromHistory);
        if (lastEvent != null) {
            if (!this.equalEvent(event, lastEvent))
                this.addEventToHistory(lastEvent, history);
            else
                this.addEventToHistory(lastEvent, excludedFromHistory);
            this.getHistoryOfPathname(lastEvent, history, excludedFromHistory);
        } else {
            FileAccessEvent lastEventFromHistory = this.getLastEventFromHistory(history);
            String timestampOrigin = ServiceUtil.getTimestampFromXSDDate(lastEventFromHistory.getTimestamp());
            FileAccessEvent originEvent = this.findCreationOriginOfFile(lastEventFromHistory.getHasSourceFile().getPathname(), timestampOrigin, history, excludedFromHistory);
            if (originEvent != null) {
                this.addEventToHistory(originEvent, history);
                this.getHistoryOfPathname(originEvent, history, excludedFromHistory);
            } else {
                FileAccessEvent copyEvent = this.getPotentialCopyOperation(lastEventFromHistory);
                if (copyEvent != null) {
                    this.addEventToHistory(copyEvent, history);
                    this.getHistoryOfPathname(copyEvent, history, excludedFromHistory);
                } else return history;
            }
        }
        return history;
    }

    private FileAccessEvent getLastEventFromHistory(LinkedHashMap<String, FileAccessEvent> history) {
        FileAccessEvent lastEventFromHistory = null;
        Iterator<Map.Entry<String, FileAccessEvent>> iterator = history.entrySet().iterator();
        while (iterator.hasNext()) lastEventFromHistory = iterator.next().getValue();
        return lastEventFromHistory;
    }

    private void addEventToHistory(FileAccessEvent e, LinkedHashMap<String, FileAccessEvent> h) {
        h.put(e.getId() + e.getHasSourceFile().getPathname() + e.getTimestamp() + e.getHasAction().getActionName(), e);
    }

    private void saveHistory(LinkedHashMap<String, FileAccessEvent> history) {
        List<History> historyList = this.transformHistory(history);
       /* for (History h : historyList) {
            if (!tdb.existsHistoryResource(h.getId(), h.getName())) {
                tdb.createHistoryResource(h);
            }
        }*/
    }

    public void createNTFileforHistory(LinkedHashMap<String, FileAccessEvent> history, String fileName) {
        List<History> historyList = this.transformHistory(history);
        for (History h : historyList) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(fileName, true));
                String xsdString = "^^<http://www.w3.org/2001/XMLSchema#String>";
                String xsdTime = "^^<http://www.w3.org/2001/XMLSchema#dateTime>";
                String resourceString = "<" + History_schema.getURI() + h.getId() + ">";
                //history Resource
                writer.append(resourceString + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + History_schema.History + "> .");
                writer.append("\n");
                // id
                writer.append(resourceString + " <" + History_schema.id + "> \"" + h.getId() + "\"" + xsdString + " .");
                writer.append("\n");
                // name
                writer.append(resourceString + " <" + History_schema.name + "> \"" + h.getName() + "\"" + xsdString + " .");
                writer.append("\n");
                // timestamp
                writer.append(resourceString + " <" + History_schema.timestamp + "> \"" + h.getTimestamp() + "\"" + xsdTime + " .");
                writer.append("\n");
                // sourceFileName
                writer.append(resourceString + " <" + History_schema.sourceFileName + "> \"" + h.getSourceFileName() + "\"" + xsdString + " .");
                writer.append("\n");
                // targetFileName
                writer.append(resourceString + " <" + History_schema.targetFileName + "> \"" + h.getTargetFileName() + "\"" + xsdString + " .");
                writer.append("\n");
                // next
                if (h.getNext() != null) {
                    writer.append(resourceString + " <" + History_schema.next + "> <" + h.getNext() + "> .");
                    writer.append("\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<History> transformHistory(LinkedHashMap<String, FileAccessEvent> history) {
        List<History> historyList = new ArrayList<>();
        FileAccessEvent nextEvent = null;
        for (Map.Entry<String, FileAccessEvent> e : history.entrySet()) {
            FileAccessEvent event = e.getValue();
            System.out.println("Create History Entry for: action: " + event.getHasAction().getActionName() +
                    " - sourcefile: " + event.getHasSourceFile().getPathname() +
                    " - targetfile: " + event.getHasTargetFile().getPathname() +
                    " - program: " + event.getHasProgram().getProgramName() +
                    " - timestamp: " + event.getTimestamp());
            String[] fileType = event.getHasSourceFile().getFilename().split("\\.");
            // if (fileType.length >= 2 && fileType[1] != null && !fileType[1].equals("tmp")) {

            History h = new History(ServiceUtil.extractIdFromUrL(event.getId()), event.getHasAction().getActionName(),
                    ServiceUtil.getTimestampFromXSDDate(event.getTimestamp()),
                    event.getHasSourceFile().getPathname(), event.getHasTargetFile().getPathname());

            String actions = event.getHasAction().getActionName();
            if (actions.equals(FileAccessType.Created_Copied.toString())) {
                h.setDuplicate(event.getHasTargetFile().getPathname());
            } else if (actions.equals(FileAccessType.Moved.toString())
                    || actions.equals(FileAccessType.MovedToRecycleBin.toString())) {
                h.setMove(event.getHasTargetFile().getPathname());
            } else if (actions.equals(FileAccessType.Renamed.toString())
                    && !event.getHasTargetFile().getFilename().split("\\.")[1].equals("tmp")) {
                h.setRename(event.getHasTargetFile().getPathname());
            }

            if (nextEvent != null) {
                h.setNext(nextEvent.getId());
            }

            historyList.add(h);
            nextEvent = event;
            //   }
        }
        return historyList;
    }

    /**
     * Method for finding how a specific file got created
     * Finds event which has current pathname as target pathname
     * and happened previous to given timestamp
     * <p>
     * Different cases can be:
     * * 1) created by renaming a different file
     * * 2) file got copied from different file from the same or different location
     * * 3) file got created by program and never existed before on different location or with different name
     * * 4) moved from other directory
     *
     * @param pathName pathname of file which origin needs to be found
     */
    private FileAccessEvent findCreationOriginOfFile(String pathName, String timestamp,
                                                     LinkedHashMap<String, FileAccessEvent> history,
                                                     LinkedHashMap<String, FileAccessEvent> excludedFromHistory) {
        String excludedEventsFilter = "";
        for (Map.Entry<String, FileAccessEvent> event : excludedFromHistory.entrySet()) {
            excludedEventsFilter += "FILTER ( ?id != \"" + event.getValue().getId() + "\" )  . ";
        }
        for (Map.Entry<String, FileAccessEvent> event : history.entrySet()) {
            excludedEventsFilter += "FILTER ( ?id != \"" + event.getValue().getId() + "\" )  . ";
        }
        String query = Queries.findCreationOriginOfFile(pathName, timestamp, excludedEventsFilter);
        List<FileAccessEvent> events = extractFileAccessEventsFromQueryResult(query);
        if (events != null && !events.isEmpty()) {
            return extractFileAccessEventsFromQueryResult(query).get(0);
        } else {
            return null;
        }
    }

    /**
     * Find origin of file creation
     * Can be: pathname in target of other event (e.g. Move)
     * Potential copy operation (in same or different dir)
     *
     * @param event FileAccessEvent for which the origin creation event should be found
     * @return the origin event or null if nothing can be found
     */
    public FileAccessEvent findOriginOfFile(FileAccessEvent event) {
        String query = Queries.findCreationOriginOfFile2(event.getHasTargetFile().getPathname());
        List<FileAccessEvent> events = extractFileAccessEventsFromQueryResult(query);
        if (events != null && !events.isEmpty()) {
            return extractFileAccessEventsFromQueryResult(query).get(0);
        } else {
            FileAccessEvent copyEvent = this.getPotentialCopyOperation(event);
            if (copyEvent != null)
                return copyEvent;
            else
                return null;
        }
    }

    public List<FileAccessEvent> createRelatedBetweenEvents(String pathname) {
        TDBConnection tdb = new TDBConnection("tdb/DB_FileAccessEvent");
        String query = Queries.fileAccessEventBySourcePathname(pathname);
        // this.printRecords(query, "tdb/DB_FileAccessEvent");
        List<FileAccessEvent> events = tdb.execQueryAndGetFileAccessEvents(query);
        Collections.sort(events);
        FileAccessEvent lastEvent = events.get(0);
        // check if pathname is in target of other event
        FileAccessEvent originEvent = this.findOriginOfFile(lastEvent);
        if (originEvent != null)
            events.add(originEvent);
        FileAccessEvent previousEvent = null;
        for (FileAccessEvent e : events) {
            if (previousEvent != null) {
                String previousEventURI = File_access_events.getURI() + previousEvent.getId();
                String currentEventURI = File_access_events.getURI() + e.getId();
                tdb.createRelatedToProperty(previousEventURI, currentEventURI);
            }
            previousEvent = e;
        }
        return events;
    }

    private FileAccessEvent getLastEventOfFilepath(String pathname) {
        String query = Queries.getLastEventOfFilepath(pathname);
        List<FileAccessEvent> events = extractFileAccessEventsFromQueryResult(query);
        if (events != null && !events.isEmpty()) {
            return extractFileAccessEventsFromQueryResult(query).get(0);
        } else {
            return null;
        }
    }

    private FileAccessEvent getLastEventOfFilepathAndTimestamp(String pathname, String lastTimestamp,
                                                               LinkedHashMap<String, FileAccessEvent> history,
                                                               LinkedHashMap<String, FileAccessEvent> excludedFromHistory) {
        String excludedEventsFilter = "";
        for (Map.Entry<String, FileAccessEvent> event : excludedFromHistory.entrySet()) {
            excludedEventsFilter += "FILTER ( ?id != \"" + event.getValue().getId() + "\" )  . ";
        }
        for (Map.Entry<String, FileAccessEvent> event : history.entrySet()) {
            excludedEventsFilter += "FILTER ( ?id != \"" + event.getValue().getId() + "\" )  . ";
        }
        String query = Queries.getLastEventOfFilepathAndTimestamp(pathname, lastTimestamp, excludedEventsFilter);
        List<FileAccessEvent> events = extractFileAccessEventsFromQueryResult(query);
        if (events != null && !events.isEmpty()) {
            return extractFileAccessEventsFromQueryResult(query).get(0);
        } else {
            return null;
        }
    }

    public synchronized void handleLogRecord(LogEntry entry) {
        entry.setHasLogType(new LogType("UnixFileSystemLogEntry"));
        if (!tdb.existsLogEntryResource(entry.getId())) {
            tdb.createResourcesForLogEntry(entry);
        }
    }

    public List<FileAccessEvent> extractFileAccessEventsFromQueryResult(String sparqlQueryString) {
        tdb = new TDBConnection("tdb/DB_FileAccessEvent");
        return tdb.execQueryAndGetFileAccessEvents(sparqlQueryString);
    }

    private List<LogEntry> extractLogEntryFromQueryResult(String sparqlQueryString) {
        tdb = new TDBConnection("tdb/DB_LogEntry");
        return tdb.execQueryAndGetLogEntries(sparqlQueryString);
    }

    public void printRecords(String sparqlQueryString, String tdbPath) {
        tdb = new TDBConnection(tdbPath);
        tdb.execQueryAndPrint(sparqlQueryString);
    }

    public void getNtFileOfTriples(String sparqlQueryString, String tdbPath) {
        tdb = new TDBConnection(tdbPath);
        tdb.execQueryAndPrint(sparqlQueryString);
    }

    public List<String> getAllFilenamesOfFileAccessEvents() {
        tdb = new TDBConnection("tdb/DB_FileAccessEvent");
        return tdb.getFileNames();
    }

    public List<String> getAllFilenamesOfFileAccessEvents(String timestamp) {
        tdb = new TDBConnection("tdb/DB_FileAccessEvent");
        return tdb.getFileNames(timestamp);
    }

    public static void setIpExternal(String ipExternal) {
        LogConverterService.ipExternal = ipExternal;
    }
}
