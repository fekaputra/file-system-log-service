package ac.at.tuwien.logparser.tdb;

import ac.at.tuwien.logparser.entities.Process;
import ac.at.tuwien.logparser.entities.UsbLog;
import ac.at.tuwien.logparser.entities.*;
import ac.at.tuwien.logparser.entities.enums.AccessCall;
import ac.at.tuwien.logparser.entities.schema.*;
import ac.at.tuwien.logparser.entities.schema.History_schema;
import ac.at.tuwien.logparser.services.util.ServiceUtil;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.apache.commons.httpclient.util.DateParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by Agnes on 12.09.18.
 */
public class TDBConnection {
    private Dataset ds;

    public TDBConnection(String path) {
        ds = TDBFactory.createDataset(path);
    }

    public TDBConnection() {
    }

    public void addDatatypeResource(Model model, Resource subject, DatatypeProperty property, String object) {
        Literal literal = model.createLiteral(object);
        subject.addProperty(property, literal);
    }

    public void addDateTimeResource(Model model, Resource subject, DatatypeProperty property, Date object) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(object);
        XSDDateTime dateTime = new XSDDateTime(calendar);
        // this.addProperty(subject, property, model.createTypedLiteral(dateTime));
    }

    private String extractFileExtension(String pathname) {
        String fileArray[] = pathname.split("\\."); //split file name/path from extension
        return fileArray[fileArray.length - 1]; // return the file extension
    }

    public boolean existsFileAccessEventResource(String id, String actionName) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_FileAccessEvent");
        Model model;
        ds.begin(ReadWrite.READ);
        boolean exists;
        try {
            model = ds.getDefaultModel();
            String sparqlQueryString = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                    "SELECT * WHERE { " +
                    "?s fae:id \"" + id + "\" ." +
                    "?s fae:hasAction/fae:actionName \"" + actionName + "\" }";
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            exists = extractSubjectWithIdFromResult(results, id);
            qexec.close();
        } finally {
            ds.end();
        }
        return exists;
    }

    private boolean extractSubjectWithIdFromResult(ResultSet results, String id) {
        while (results.hasNext()) {
            QuerySolution entry = results.next();
            String s = entry.get("s").toString();
            if (s.contains(id)) {
                return true;
            }
        }
        return false;
    }

    public void createFileAccessEventResource(FileAccessEvent event) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_FileAccessEvent");
        ds.begin(ReadWrite.WRITE);
        try {
            Model model = ds.getDefaultModel();
            Resource fileAccessEventResource = model.createResource(File_access_events.getURI() + event.getId(), File_access_events.FileAccessEvent);
            this.addTimestampToResource(model, fileAccessEventResource, event.getTimestamp(), File_access_events.timestamp);
            fileAccessEventResource.addProperty(File_access_events.id, model.createLiteral(event.getId()));
            fileAccessEventResource.addProperty(File_access_events.eventID, event.getEventID() != null ? model.createLiteral(event.getEventID()) : model.createResource());
            Resource actionResource = model.createResource(File_access_events.getURI() + event.getId() + "-Action", File_access_events.Action);
            actionResource.addProperty(File_access_events.actionName, event.getHasAction().getActionName());
            Resource sourceHostResource = model.createResource(File_access_events.getURI() + event.getId() + "-SourceHost", File_access_events.Host);
            sourceHostResource.addProperty(File_access_events.hostName, event.getHasSourceHost().getHostname());
            Resource targetHostResource = model.createResource(File_access_events.getURI() + event.getId() + "-TargetHost", File_access_events.Host);
            targetHostResource.addProperty(File_access_events.hostName, event.getHasTargetHost().getHostname());
            Resource sourceFileResource = model.createResource(File_access_events.getURI() + event.getId() + "-SourceFile", File_access_events.File);
            sourceFileResource.addProperty(File_access_events.fileName, event.getHasSourceFile().getFilename());
            sourceFileResource.addProperty(File_access_events.pathName, event.getHasSourceFile().getPathname());
            Resource targetFileResource = model.createResource(File_access_events.getURI() + event.getId() + "-TargetFile", File_access_events.File);
            targetFileResource.addProperty(File_access_events.fileName, event.getHasTargetFile().getFilename());
            targetFileResource.addProperty(File_access_events.pathName, event.getHasTargetFile().getPathname());
            Resource userResource = model.createResource(File_access_events.getURI() + event.getId() + "-User", File_access_events.User);
            userResource.addProperty(File_access_events.userName, event.getHasUser().getUsername());
            Resource programResource = model.createResource(File_access_events.getURI() + event.getId() + "-Program", File_access_events.Program);
            programResource.addProperty(File_access_events.programName,
                    event.getHasProgram().getProgramName() != null ? model.createLiteral(event.getHasProgram().getProgramName()) : model.createResource());
            programResource.addProperty(File_access_events.pid,
                    event.getHasProgram().getPid() != null ? model.createLiteral(event.getHasProgram().getPid()) : model.createResource());
            fileAccessEventResource.addProperty(File_access_events.hasAction, actionResource);
            fileAccessEventResource.addProperty(File_access_events.hasSourceHost, sourceHostResource);
            fileAccessEventResource.addProperty(File_access_events.hasTargetHost, targetHostResource);
            fileAccessEventResource.addProperty(File_access_events.hasSourceFile, sourceFileResource);
            fileAccessEventResource.addProperty(File_access_events.hasTargetFile, targetFileResource);
            fileAccessEventResource.addProperty(File_access_events.hasTargetFile, targetFileResource);
            fileAccessEventResource.addProperty(File_access_events.hasUser, userResource);
            fileAccessEventResource.addProperty(File_access_events.hasProgram, programResource);
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public void createRelatedToProperty(String previousEventURI, String currentEventURI) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_FileAccessEvent");
        ds.begin(ReadWrite.WRITE);
        try {
            Model model = ds.getDefaultModel();
            Resource prevFae = model.getResource(previousEventURI);
            Resource currFae = model.getResource(currentEventURI);
            prevFae.addProperty(File_access_events.relatedTo, currFae);
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public void createProgramPropertyForFileAccessEvent(String eventId, String programName) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_FileAccessEvent");
        ds.begin(ReadWrite.WRITE);
        try {
            Model model = ds.getDefaultModel();
            Resource eventResource = model.getResource(File_access_events.getURI() + eventId);
            Resource programResource = model.createResource(File_access_events.getURI() + eventId + "-Program",
                    File_access_events.Program);
            programResource.addProperty(File_access_events.programName,
                    programName != null ? model.createLiteral(programName) : model.createResource());
            eventResource.addProperty(File_access_events.hasProgram, programResource);
            ds.commit();
        } finally {
            ds.end();
        }
    }


    public void createCheckSumPropertyOfFileResource(String resourceURI, String checkSumOfFile) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_FileAccessEvent");
        Model model;
        ds.begin(ReadWrite.WRITE);
        try {
            model = ds.getDefaultModel();
            Resource fae = model.getResource(resourceURI);
            fae.addProperty(File_access_events.md5CheckSum, checkSumOfFile);
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public boolean existsProcessInfoResource(ProcessInfo info) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_ProcessInfo");
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getDefaultModel();
            String sparqlQueryString = "PREFIX process: <http://sepses.ifs.tuwien.ac.at/vocab/processInfo#> " +
                    "SELECT * " +
                    "WHERE { " +
                    "?s process:operation \"" + info.getOperation() + "\";" +
                    "   process:id \"" + info.getId() + "\";" +
                    "   process:timestamp \"" + info.getTimestamp() + "\" ;" +
                    "   process:processName \"" + info.getProcessName() + "\";" +
                    "   process:pid \"" + info.getPid() + "\" ." +
                    "}";
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            boolean exists = results.hasNext();
            qexec.close();
            return exists;
        } finally {
            ds.end();
        }
    }

    public String getProcessNameByPid(String processID, String timestamp) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_ProcessInfo");
        String processName = null;
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getDefaultModel();
            String sparqlQueryString =
                    "PREFIX process: " +
                            "<http://sepses.ifs.tuwien.ac.at" +
                            "/vocab/processInfo#> " +
                    "SELECT ?name WHERE { " +
                    "?s process:pid \"4567\" . " +
                    "?s process:operation \"start\" . " +
                    "?s process:processName ?name . " +
                    "?s process:timestamp ?timestamp . " +
                    "FILTER ( ?timestamp < \"2019-04-29T08:38:26Z\"" +
                            "^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                    "FILTER not exists {" +
                    "  ?s process:timestamp ?after" +
                    "  filter (?after > ?timestamp) . }}";
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                processName = results.next().getLiteral("?name").getString();
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return processName;
    }

    public synchronized void createProcessInfoResource(ProcessInfo info) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_ProcessInfo");
        ds.begin(ReadWrite.WRITE);
        try {
            Model model = ds.getDefaultModel();
            Resource processInfoResource = model.createResource(Process_info.getURI() + info.getId(), Process_info.ProcessInfo);
            processInfoResource.addProperty(Process_info.id, info.getId());
            processInfoResource.addProperty(Process_info.operation, info.getOperation());
            this.addTimestampToResource(model, processInfoResource, info.getTimestamp(), Process_info.timestamp);
            processInfoResource.addProperty(Process_info.processName, info.getProcessName());
            processInfoResource.addProperty(Process_info.pid, info.getPid());
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public boolean existsHistoryResource(String id, String name) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_History");
        Model model;
        ds.begin(ReadWrite.READ);
        boolean exists;
        try {
            model = ds.getDefaultModel();
            String sparqlQueryString = "PREFIX h: <http://sepses.ifs.tuwien.ac.at/vocab/history#> " +
                    "SELECT * WHERE { " +
                    "?s h:id \"" + id + "\" . " +
                    "?s h:name \"" + name + "\" . " +
                    "}";
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            exists = extractSubjectWithIdFromResult(results, id);
            qexec.close();
        } finally {
            ds.end();
        }
        return exists;
    }

    public synchronized void createHistoryResource(History h) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_History");
        ds.begin(ReadWrite.WRITE);
        try {
            Model model = ds.getDefaultModel();
            Resource historyResource = model.createResource(History_schema.getURI() + h.getId(), History_schema.History);
            historyResource.addProperty(History_schema.id, h.getId());
            historyResource.addProperty(History_schema.name, h.getName());
            try {
                historyResource.addProperty(History_schema.timestamp, model.createTypedLiteral(
                        ServiceUtil.parseStringToXSDDateTime(h.getTimestamp(),
                                "yyyy-MM-dd'T'HH:mm:ss'Z'")));
            } catch (DateParseException e) {
                System.out.println(h.toString());
                throw new TDBException("Cannot create History Resource");
            }
            historyResource.addProperty(History_schema.sourceFileName, h.getSourceFileName());
            historyResource.addProperty(History_schema.targetFileName, h.getTargetFileName());
            if (h.getNext() != null) {
                Resource nextEvent = model.createResource(History_schema.getURI() + ServiceUtil.extractIdFromUrL(h.getNext()), History_schema.History);
                historyResource.addProperty(History_schema.next, nextEvent);
            }
            if (h.getDuplicate() != null) {
                historyResource.addProperty(History_schema.duplicate, h.getDuplicate());
            }
            if (h.getMove() != null) {
                historyResource.addProperty(History_schema.move, h.getMove());
            }
            if (h.getRename() != null) {
                historyResource.addProperty(History_schema.rename, h.getRename());
            }
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public synchronized void createUsbLogResource(UsbLog log) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_LogEntry");
        Model model;
        ds.begin(ReadWrite.WRITE);
        try {
            model = ds.getDefaultModel();
            Resource usbLogResource = model.createResource("USBDeviceLogEntry-" + log.getId(), USB_log.UsbLog);
            this.addDatatypeResource(model, usbLogResource, USB_log.id, log.getId());
            this.addDatatypeResource(model, usbLogResource, USB_log.timestamp, log.getTimestamp());
            this.addDatatypeResource(model, usbLogResource, USB_log.instaceId, log.getInstanceId());
            this.addDatatypeResource(model, usbLogResource, USB_log.logMessage, log.getMessage());
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public LogEntry checkForPreviousGetAttrEvent(String timestamp, String pathname) {
        LogEntry e = null;
        Dataset ds = TDBFactory.createDataset("tdb/DB_LogEntry");
       /* String range = null;
        try {
            long time = ServiceUtil.parseDate(timestamp, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.GERMANY).getTime();
            Date date = new Date(time - 3 * 1000); //substract 5 sec
            range = ServiceUtil.parseDateToString(date, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        } catch (DateParseException e1) {
            e1.printStackTrace();
        }*/
        Model model;
        ds.begin(ReadWrite.READ);
        try {//TODO FIX MEEEEEEEE
            model = ds.getDefaultModel();
            String query2 = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                    "SELECT distinct ?id ?accessCall ?pathname ?processID ?username ?host ?timestamp ?logMessage " +
                    "WHERE { " +
                    "?logEntry file:id ?id . " +
                    "?logEntry file:accessCall ?accessCall . " +
                    "FILTER ( str(?accessCall) = \"getattrlist()\" ) . " +
                    "?logEntry file:hasFile/file:pathname ?pathname .  " +
                    "FILTER ( CONTAINS(?pathname, \"" + pathname + "\") ) " +
                    "?logEntry file:hasProcess/file:processID ?processID . " +
                    "?logEntry file:hasUser/file:username ?username . " +
                    "?logEntry file:originatesFrom/file:hostName ?host . " +
                    "?logEntry file:timestamp ?timestamp . " +
                    "FILTER (?timestamp < \"" + timestamp + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ) . " +
                    "?logEntry file:logMessage ?logMessage " +
                    "}  ORDER BY ASC(?timestamp)";
            Query query = QueryFactory.create(query2);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                e = new LogEntry(binding.get("id").toString(), AccessCall.findByValue(binding.get("accessCall").toString()),
                        binding.get("timestamp").toString(), binding.get("logMessage").toString(), new Process(binding.get("processID").toString()),
                        new File(null, binding.get("pathname").toString()), new Host(binding.get("host").toString()),
                        new User(binding.get("username").toString()));
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return e;
    }

    public boolean existsLogEntryResource(String id) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_LogEntry");
        Model model;
        ds.begin(ReadWrite.READ);
        boolean exists;
        try {
            model = ds.getDefaultModel();
            String sparqlQueryString = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#> " +
                    "SELECT * WHERE { " +
                    "?s file:id \"" + id + "\" }";
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            exists = extractSubjectWithIdFromResult(results, id);
            qexec.close();
        } finally {
            ds.end();
        }
        return exists;
    }

    private void addTimestampToResource(Model model, Resource resource, String timestamp, DatatypeProperty property) {
        try {
           /* String timestampEvent = ServiceUtil.parseDateToString(
                    ServiceUtil.parseDate(timestamp, "EEE MMM dd HH:mm:ss yyyy"),
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");*/
            XSDDateTime literal = ServiceUtil.parseStringToXSDDateTime(timestamp, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            resource.addProperty(property, model.createTypedLiteral(literal));
        } catch (DateParseException e) {
            throw new TDBException("Cannot create Resource!");
        }
    }

    public synchronized void createResourcesForLogEntry(LogEntry entry) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_LogEntry");
        Model model;
        ds.begin(ReadWrite.WRITE);
        try {
            model = ds.getDefaultModel();
            Resource logEntryResource = model.createResource(File_system_log_schema.getURI() + entry.getId(),
                    File_system_log_schema.LogEntry);
            this.addTimestampToResource(model, logEntryResource, entry.getTimestamp(), File_system_log_schema.timestamp);
            if (entry.getAccessCall() == null) {
                System.out.println("Access Call not supported: " + entry.getLogMessage());
                ds.close();
                return;
            }
            logEntryResource.addProperty(File_system_log_schema.id, entry.getId());
            logEntryResource.addProperty(File_system_log_schema.accessCall, entry.getAccessCall().getValue());
            logEntryResource.addProperty(File_system_log_schema.logMessage,
                    entry.getLogMessage() != null
                            ? model.createLiteral(entry.getLogMessage())
                            : model.createResource());

            Resource fileResource = model.createResource(File_system_log_schema.File);
            fileResource.addProperty(File_system_log_schema.filetype, this.extractFileExtension(entry.getHasFile().getPathname()));
            fileResource.addProperty(File_system_log_schema.pathname, entry.getHasFile().getPathname());

            Resource processResource = model.createResource(File_system_log_schema.Process);
            processResource.addProperty(File_system_log_schema.processname,
                    entry.getHasProcess().getProcessname() != null
                            ? model.createLiteral(entry.getHasProcess().getProcessname())
                            : model.createResource());
            processResource.addProperty(File_system_log_schema.processID, entry.getHasProcess().getProcessID());

            Resource logTypeResource = model.createResource(File_system_log_schema.LogType);
            logTypeResource.addProperty(File_system_log_schema.logTypeName, entry.getHasLogType().getLogTypeName());

            Resource hostResource = model.createResource(File_system_log_schema.Host);
            hostResource.addProperty(File_system_log_schema.hostName, entry.getOriginatesFrom().getHostname());
            hostResource.addProperty(File_system_log_schema.ipAddress,
                    entry.getOriginatesFrom().getIpAddress() != null
                            ? model.createLiteral(entry.getOriginatesFrom().getIpAddress())
                            : model.createResource());
            hostResource.addProperty(File_system_log_schema.ip4Address,
                    entry.getOriginatesFrom().getIp4Address() != null
                            ? model.createLiteral(entry.getOriginatesFrom().getIp4Address())
                            : model.createResource());
            hostResource.addProperty(File_system_log_schema.ip6Address,
                    entry.getOriginatesFrom().getIp6Address() != null
                            ? model.createLiteral(entry.getOriginatesFrom().getIp6Address())
                            : model.createResource());

            Resource userResource = model.createResource(File_system_log_schema.User);
            userResource.addProperty(File_system_log_schema.username, entry.getHasUser().getUsername());
            userResource.addProperty(File_system_log_schema.domain,
                    entry.getHasUser().getDomain() != null
                            ? model.createLiteral(entry.getHasUser().getDomain())
                            : model.createResource());

            logEntryResource.addProperty(File_system_log_schema.hasFile, fileResource);
            logEntryResource.addProperty(File_system_log_schema.hasProcess, processResource);
            logEntryResource.addProperty(File_system_log_schema.hasLogType, logTypeResource);
            logEntryResource.addProperty(File_system_log_schema.originatesFrom, hostResource);
            logEntryResource.addProperty(File_system_log_schema.hasUser, userResource);

            ds.commit();
        } finally {
            ds.end();
        }
    }

    public boolean updateProgramNamOfFileAccessEventResource(String resourceURI, String programName) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_FileAccessEvent");
        Model model;
        ds.begin(ReadWrite.WRITE);
        try {
            model = ds.getDefaultModel();
            Resource fae = model.getResource(resourceURI);
            Statement faeStatement = fae.getProperty(File_access_events.programName);
            RDFNode program = faeStatement.getObject();
            if (program.isLiteral()) {// removes programName property and adds new property
                fae.removeAll(File_access_events.programName);
                fae.addProperty(File_access_events.programName, programName);
            }
            ds.commit();
        } finally {
            ds.end();
        }
        return true;
    }

    public List<Statement> getStatements(String modelName, String subject, String property, String object) {
        List<Statement> results = new ArrayList<>();
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getNamedModel(modelName);
            Selector selector = new SimpleSelector(
                    (subject != null) ? model.createResource(subject) : (Resource) null,
                    (property != null) ? model.createProperty(property) : (Property) null,
                    (object != null) ? model.createResource(object) : (RDFNode) null
            );
            StmtIterator it = model.listStatements(selector);
            {
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    results.add(stmt);
                }
            }
            ds.commit();
        } finally {
            ds.end();
        }
        return results;
    }

    public void removeStatement(String modelName, String subject, String property, String object) {
        Model model;
        ds.begin(ReadWrite.WRITE);
        try {
            model = ds.getNamedModel(modelName);
            Statement stmt = model.createStatement(
                    model.createResource(subject),
                    model.createProperty(property),
                    model.createResource(object));
            model.remove(stmt);
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public void close() {
        ds.close();
    }

    public List<Triple> queryAllTriples(String modelName, int limit) {
        String queryString = "SELECT ?s ?p ?o { ?s ?p ?o } LIMIT " + limit + " OFFSET 0";
        List<Triple> triples = new ArrayList<>();
        Model model = ds.getNamedModel(modelName);
        ds.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                triples.add(new Triple(binding.get("s").toString(), binding.get("p").toString(), binding.get("o").toString()));
            }
            qexec.close();
        } finally {
            ds.close();
            ds.end();
        }

        return triples;
    }

    public String execQuery(String modelName, String sparqlQueryString) {
        String result = "";
        Model model = ds.getNamedModel(modelName);
        ds.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                for (String var : results.getResultVars()) {
                    RDFNode node = binding.get(var);
                    result += node.toString() + "  ";
                }
                result += "\n";
            }
            qexec.close();
        } finally {
            ds.close();
            ds.end();
        }
        return result;
    }

    public void execQueryAndPrint(String modelName, String sparqlQueryString) {
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getNamedModel(modelName);
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(results);
            qexec.close();
        } finally {
            ds.end();
        }
    }

    public void execQueryAndPrint(String sparqlQueryString) {
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getDefaultModel();
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(results);
            qexec.close();
        } finally {
            ds.end();
        }
    }

    public void execQueryAndPrintNTriples(String sparqlQueryString, String fileName) {
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getDefaultModel();
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
                    String subject = "<" + binding.get("s").toString() + ">";
                    String predicate = "<" + binding.get("p").toString() + ">";
                    String object = binding.get("o").toString();
                    String tempObj = object.length() > 45 ? object.substring(0, 45) : "";
                    if (predicate.matches("<http://purl.org/sepses/vocab/event/fileAccess#timestamp>")) {
                        String timestamp = ServiceUtil.getTimestampFromXSDDate(object);
                        object = "\"" + timestamp + "\"" + "^^<http://www.w3.org/2001/XMLSchema#dateTime>";
                    } else if (predicate.matches("<http://purl.org/sepses/vocab/event/fileAccess#relatedTo>")) {
                        object = "<" + object + ">";
                    } else if (predicate.matches("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")) {
                        object = "<" + object + ">";
                    } else if (predicate.matches("<http://purl.org/sepses/vocab/event/fileAccess#has.+?(?=>)>")) {
                        object = "<" + object + ">";
                    } else if (tempObj.matches("http://purl.org/sepses/vocab/event/fileAccess#")) {
                        object = "<" + object + ">";
                    } else {
                        object = "\"" + object + "\"" + "^^<http://www.w3.org/2001/XMLSchema#String>";
                    }
                    String line = subject + " " + predicate + " " + object + " . ";
                    writer.append(line);
                    writer.append("\n");
                    writer.close();
                } catch (Exception e) {//Catch exception if any
                    System.err.println("Error: " + e.getMessage());
                }
            }
            qexec.close();
        } finally {
            ds.end();
        }
    }

    public List<FileAccessEvent> execQueryAndGetFileAccessEvents(String sparqlQueryString) {
        List<FileAccessEvent> faeList = new ArrayList<>();
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getDefaultModel();
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                FileAccessEvent event = new FileAccessEvent(
                        binding.get("id").toString(),
                        binding.get("timestamp").toString(), null, //eventID is null
                        new Action(binding.get("actionName").toString()),
                        new Host(binding.get("hostnameSource").toString()),
                        new Host(binding.get("hostnameTarget").toString()),
                        new File(binding.get("fileNameSource").toString(), binding.get("pathNameSource").toString()),
                        new File(binding.get("fileNameTarget").toString(), binding.get("pathnameTarget").toString()),
                        new User(binding.get("username").toString()),
                        new Program(binding.get("programName").toString())
                );
                if (!faeList.contains(event)) {
                    ServiceUtil.setDateTimeOfEvent(event);
                    faeList.add(event);
                }
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return faeList;
    }

    public List<LogEntry> execQueryAndGetLogEntries(String sparqlQueryString) {
        List<LogEntry> entriesList = new ArrayList<>();
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getDefaultModel();
            Query query = QueryFactory.create(sparqlQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                LogEntry entry = new LogEntry(binding.get("logEntry").toString(),
                        AccessCall.findByValue(binding.get("accessCall").toString()),
                        binding.get("timestamp").toString(),
                        binding.get("logMessage").toString(),
                        new Process(binding.get("processID").toString(), null),
                        new File(null, binding.get("pathname").toString()),
                        new Host(binding.get("host").toString()),
                        new User(binding.get("username").toString()));

                if (!entriesList.contains(entry))
                    entriesList.add(entry);
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return entriesList;
    }

    public List<String> getFileNames() {
        String queryString = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT DISTINCT ?pathname WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "?s fae:hasSourceFile/fae:fileName ?fileName  . " +
                "FILTER REGEX (?fileName , \"[a-zA-Z0-9_ :~$]+[.][a-zA-Z0-9]{2,5}\") ." +
                "?s fae:hasSourceFile/fae:pathName ?pathname . " +
                "}";
        return this.queryNames(queryString);
    }

    public List<String> getFileNames(String range) {
        String queryString = "PREFIX fae: <http://purl.org/sepses/vocab/event/fileAccess#> " +
                "SELECT DISTINCT ?pathname WHERE { " +
                "?s fae:timestamp ?timestamp . " +
                "FILTER (?timestamp > \"" + range + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) . " +
                "?s fae:hasSourceFile/fae:fileName ?fileName . " +
                "FILTER REGEX (?fileName , \"[a-zA-Z0-9_ :~$]+[.][a-zA-Z0-9]{2,5}\") ." +
                "?s fae:hasSourceFile/fae:pathName ?pathname . " +
                "}";
        return this.queryNames(queryString);
    }

    private List<String> queryNames(String queryString) {
        List<String> names = new ArrayList<>();
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getDefaultModel();
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                names.add(binding.get("pathname").toString());
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return names;
    }


    public Set<String> queryForAccessCalls(String modelName, String queryString) {
        Set<String> result = new HashSet<>();
        Model model = ds.getNamedModel(modelName);
        // Start READ transaction.
        ds.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                result.add(binding.get("o").toString());
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return result;
    }

    public List<FileOperation> queryForFileOperations(String modelName, String filename) throws DateParseException {
        List<FileOperation> result = new ArrayList<>();
        Model model = ds.getNamedModel(modelName);
        String queryString = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?logEntry ?file ?accessCall ?filename ?timestamp " +
                "WHERE{ " +
                "?logEntry rdf:type  file:LogEntry ." +
                "?logEntry file:hasFile ?file ." +
                "?logEntry file:accessCall ?accessCall ." +
                "?logEntry file:timestamp ?timestamp ." +
                "?file file:pathname ?filename ." +
                "FILTER regex(?filename, \"" + filename + "\") " +
                "}ORDER BY ASC(?timestamp)";
        // Start READ transaction.
        ds.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                Date timestamp = ServiceUtil.parseDate(binding.get("timestamp").toString(), "EEE MMM dd HH:mm:ss yyyy");
                FileOperation operation = new FileOperation(binding.get("logEntry").toString(),
                        binding.get("file").toString(), binding.get("accessCall").toString(),
                        binding.get("filename").toString(), timestamp);
                result.add(operation);
            }
            qexec.close();
        } finally {
            ds.close();
            ds.end();
        }
        return result;
    }

    public LinkedHashMap<FileOperationKey, FileOperation> getMapOfFileOperations(String modelName, String filename) throws DateParseException {
        // Dataset ds = TDBFactory.createDataset("tdb/DB_SELECT4"); //OLD code using Jena3
        DatasetFactory dsFactory = new DatasetFactory();
        ds = dsFactory.create("tdb/DB_SELECT4");

        LinkedHashMap<FileOperationKey, FileOperation> operations = new LinkedHashMap<>();
        Model model = ds.getNamedModel(modelName);

        int tenMinutes = 123 * 60 * 1000; //first number is the amount of minutes (2 hours and 3 minnutes ago)

        long tenAgo = System.currentTimeMillis() - tenMinutes;
        Date thresholdTime = new Date(tenAgo); //last 10 Minutes
        String thresholdTimeString = ServiceUtil.parseDateToString(thresholdTime, "yyyy-MM-dd'T'HH:mm:ss'Z'");
        String queryString = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?file ?accessCall ?filename ?timestamp " +
                "WHERE{ " +
                "?logEntry rdf:type  file:LogEntry ." +
                "?logEntry file:timestamp ?timestamp ." +
                "?logEntry file:accessCall ?accessCall ." +
                "FILTER (xsd:dateTime(?timestamp) > \"" + thresholdTimeString + "\"^^xsd:dateTime) ." +
                "?file file:pathname ?filename ." +
                "FILTER regex(?filename, \"" + filename + "\") " +
                "}ORDER BY ASC(?timestamp)";
        // Start READ transaction.
        // model.enterCriticalSection(Lock.READ) ;
        ds.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                String timestampString = binding.get("timestamp").asLiteral().getString();
                // String timestampString = binding.get("StrTimestamp").toString();
                Date timestamp = ServiceUtil.parseDate(timestampString, "yyyy-MM-dd'T'HH:mm:ss'Z'");
                //  if(timestamp != null && ServiceUtil.dateWithin10Minutes(timestamp)){
                FileOperation operation = new FileOperation(binding.get("logEntry").toString(),
                        binding.get("file").toString(), binding.get("accessCall").toString(),
                        binding.get("filename").toString(), timestamp);
                FileOperationKey key = new FileOperationKey(AccessCall.findByValue(binding.get("accessCall").toString()), timestamp);
                operations.put(key, operation);
                // }

            }
            qexec.close();
        } finally {
            ds.end();
        }
        return operations;
    }


    public Set<String> queryFilenames(String modelName, List<AccessCall> accessCalls) {
        Set<String> result = new HashSet<>();
        Model model = ds.getNamedModel(modelName);

        String aCsString = this.getAccessCallFilter(accessCalls);

        String queryString = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?file ?accessCall ?accessCall ?filename ?timestamp " +
                "WHERE{ " +
                "?logEntry rdf:type  file:LogEntry ." +
                "?logEntry file:timestamp ?timestamp ." +
                "?logEntry file:accessCall ?accessCall ." +
                " FILTER(" + aCsString + ") ." +
                "?logEntry file:hasFile ?file ." +
                "?file file:pathname ?filename ." +
                "}ORDER BY ASC(?timestamp)";
        result.addAll(this.executeQuery(model, queryString));
        return result;
    }

    private String getAccessCallFilter(List<AccessCall> accessCalls) {
        String aCsString = "";
        for (int i = 0; i < accessCalls.size(); i++) {
            if (i == 0) {
                aCsString += "?accessCall = \"" + accessCalls.get(0).getValue() + "\"";
            } else {
                aCsString += " || ?accessCall = \"" + accessCalls.get(i).getValue() + "\"";
            }
        }
        return aCsString;
    }

    private String getFilepathFromMultiplePaths(String filepathsString) {
        String[] paths = filepathsString.split(",");
        return paths[0];
    }

    private List<String> executeQuery(Model model, String queryString) {
        List<String> result = new ArrayList<>();
        ds.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                String pathsString = binding.get("filename").toString();
                result.add(this.getFilepathFromMultiplePaths(pathsString));
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return result;
    }

    public Set<String> queryFilenamesByFilename(String modelName, List<AccessCall> accessCalls, String filename) {
        Set<String> result = new HashSet<>();
        Model model = ds.getNamedModel(modelName);

        String aCsString = this.getAccessCallFilter(accessCalls);

        String queryString = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?file ?accessCall ?accessCall ?filename ?timestamp " +
                "WHERE{ " +
                "?logEntry rdf:type  file:LogEntry ." +
                "?logEntry file:timestamp ?timestamp ." +
                "?logEntry file:accessCall ?accessCall ." +
                " FILTER(" + aCsString + ") ." +
                "?logEntry file:hasFile ?file ." +
                "?file file:pathname ?filename ." +
                "FILTER regex(?filename, \"" + filename + "\") " +
                "}ORDER BY ASC(?timestamp)";
        result.addAll(this.executeQuery(model, queryString));
        return result;
    }

    public Set<String> queryFilesnamesFromPrevious(String modelName, Set<String> previousFilenames, List<AccessCall> accessCalls) {
        Set<String> result = new HashSet<>();
        for (String pf : previousFilenames) {
            result.addAll(this.queryFilenamesByFilename(modelName, accessCalls, pf));
        }
        return result;
    }

    public Set<String> queryFilesnamesFromPreviousWithAllPaths(String modelName, Set<String> previousFilenames, List<AccessCall> accessCalls) {
        Set<String> result = new HashSet<>();
        for (String pf : previousFilenames) {
            result.addAll(this.queryFilenamesByFilenameFullPathString(modelName, accessCalls, pf));
        }
        return result;
    }

    private Set<String> queryFilenamesByFilenameFullPathString(String modelName, List<AccessCall> accessCalls, String filename) {
        Set<String> result = new HashSet<>();
        Model model = ds.getNamedModel(modelName);

        String aCsString = this.getAccessCallFilter(accessCalls);

        String queryString = "PREFIX file: <http://sepses.ifs.tuwien.ac.at/vocab/fileSystemLog#>" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "SELECT ?logEntry ?file ?accessCall ?accessCall ?filename ?timestamp " +
                "WHERE{ " +
                "?logEntry rdf:type  file:LogEntry ." +
                "?logEntry file:timestamp ?timestamp ." +
                "?logEntry file:accessCall ?accessCall ." +
                " FILTER(" + aCsString + ") ." +
                "?logEntry file:hasFile ?file ." +
                "?file file:pathname ?filename ." +
                "FILTER regex(?filename, \"" + filename + "\") " +
                "}ORDER BY ASC(?timestamp)";
        ds.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution binding = results.nextSolution();
                String pathsString = binding.get("filename").toString();
                result.add(pathsString);
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return result;
    }

    public void createPersonResource(Person p) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_Background");
        ds.begin(ReadWrite.WRITE);
        try {
            Model model = ds.getDefaultModel();
            Resource personResource = model.createResource(Background_schema.getURI() + p.getId(), Background_schema.Person);
            personResource.addProperty(Background_schema.firstName, p.getFirstName());
            personResource.addProperty(Background_schema.lastName, p.getLastName());
            personResource.addProperty(Background_schema.email, p.getEmail());
            for (String un : p.getUserName()) {
                personResource.addProperty(Background_schema.userName, un);
            }
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public void createChannelResource(Channel c) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_Background");
        ds.begin(ReadWrite.WRITE);
        try {
            Model model = ds.getDefaultModel();
            Resource personResource = model.createResource(Background_schema.getURI() + c.getId(), Background_schema.Channel);
            personResource.addProperty(Background_schema.name, c.getName());
            personResource.addProperty(Background_schema.type, c.getType());
            personResource.addProperty(Background_schema.path, c.getPath());
            personResource.addProperty(Background_schema.program, c.getProgram() != null ? c.getProgram() : "");
            ds.commit();
        } finally {
            ds.end();
        }
    }

    public List<Channel> getChannelsByType(String type) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_Background");
        List<Channel> channels = new ArrayList<>();
        String queryString = "PREFIX b: <http://purl.org/sepses/vocab/background#> " +
                "SELECT ?type ?name ?path ?program WHERE { " +
                "?s b:type \"" + type + "\" . " +
                "?s b:type ?type . " +
                "?s b:name ?name . " +
                "?s b:path ?path . " +
                "?s b:program ?program . " +
                "}";
        Model model;
        ds.begin(ReadWrite.READ);
        try {
            model = ds.getDefaultModel();
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                Channel c = extractChannel(results.nextSolution());
                channels.add(c);
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return channels;
    }

    public Channel getChannelByName(String name) {
        String queryString = "PREFIX b: <http://purl.org/sepses/vocab/background#> " +
                "SELECT ?type ?name ?path ?program WHERE { " +
                "?s b:type ?type . " +
                "?s b:name \"" + name + "\" . " +
                "?s b:name ?name . " +
                "?s b:path ?path . " +
                "?s b:program ?program . " +
                "}";
        return queryForChannel(queryString);
    }

    public Channel getChannelByPath(String path) {
        String queryString = "PREFIX b: <http://purl.org/sepses/vocab/background#> " +
                "SELECT ?type ?name ?path ?program WHERE { " +
                "?s b:type ?type . " +
                "?s b:name ?name . " +
                "?s b:path \"" + path + "\" . " +
                "?s b:path ?path . " +
                "?s b:program ?program . " +
                "}";
        return queryForChannel(queryString);
    }

    public Channel getChannelByProgram(String program) {
        String queryString = "PREFIX b: <http://purl.org/sepses/vocab/background#> " +
                "SELECT ?type ?name ?path ?program WHERE { " +
                "?s b:type ?type . " +
                "?s b:name ?name . " +
                "?s b:path ?path . " +
                "?s b:program \"" + program + "\" . " +
                "?s b:program ?program . " +
                "}";
        return queryForChannel(queryString);
    }

    private Channel queryForChannel(String queryString) {
        Dataset ds = TDBFactory.createDataset("tdb/DB_Background");
        Channel channel = null;
        ds.begin(ReadWrite.READ);
        try {
            Model model = ds.getDefaultModel();
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                channel = extractChannel(results.nextSolution());
            }
            qexec.close();
        } finally {
            ds.end();
        }
        return channel;
    }

    private Channel extractChannel(QuerySolution binding) {
        return new Channel(binding.get("type").toString(), binding.get("name").toString(),
                binding.get("path").toString(), binding.get("program").toString());
    }
}