package ac.at.tuwien.logparser.services;

import ac.at.tuwien.logparser.entities.Queries;
import ac.at.tuwien.logparser.services.formatter.*;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.text.ParseException;

@Service
public class CSparqlStreamService {

    @Autowired
    private CreateFileAccessEventsFormatter createFileAccessEventsFormatter;
    @Autowired
    private DeleteFileAccessEventsFormatter deleteFileAccessEventsFormatter;
    @Autowired
    private ModifyFileAccessEventsFormatter modifyFileAccessEventsFormatter;
    @Autowired
    private RenameFileAccessEventsFormatter renameFileAccessEventsFormatter;
    @Autowired
    private MoveFileAccessEventsFormatter moveFileAccessEventsFormatter;
    @Autowired
    private MoveToRecycleBinFileAccessEventsFormatter moveToRecycleBinFileAccessEventsFormatter;
    @Autowired
    private ProcessInfoFormatter processInfoFormatter;
    @Autowired
    private LogEntryFormatter logEntryFormatter;


    public CSparqlStreamService() {
    }

    public void initCSparqlEngine() throws ParseException, URISyntaxException {
        StreamProcessingService fsStream = new StreamProcessingService("ws://localhost:8124/tw/stream");
        StreamProcessingService psStream = new StreamProcessingService("ws://localhost:8125/tw/stream");

        //Create csparql engine instance
        CsparqlEngineImpl engine = new CsparqlEngineImpl();

        //Initialize the engine instance
        //The initialization creates the static engine (SPARQL) and the stream engine (CEP)
        engine.initialize(true);

        //Register new stream in the engine
        engine.registerStream(fsStream);
        engine.registerStream(psStream);
        //  engine.registerStream(observerStream);

        //Register new query in the engine
        CsparqlQueryResultProxy createdProxy = engine.registerQuery(Queries.created, false);
        CsparqlQueryResultProxy deletedProxy = engine.registerQuery(Queries.deleted, false);
        CsparqlQueryResultProxy modifiedProxy = engine.registerQuery(Queries.modified, false);
        CsparqlQueryResultProxy renamedProxy = engine.registerQuery(Queries.renamed, false);
        CsparqlQueryResultProxy movedProxy = engine.registerQuery(Queries.moved, false);
        CsparqlQueryResultProxy movedRecycleBinProxy = engine.registerQuery(Queries.movedToRecycleBin, false);
        CsparqlQueryResultProxy processInfoProxy = engine.registerQuery(Queries.processInfo, false);
        CsparqlQueryResultProxy logEntryProxy = engine.registerQuery(Queries.allLogEntries, false);

        //Attach a result consumer to the query result proxy to print the results on the console
        createdProxy.addObserver(createFileAccessEventsFormatter);
        deletedProxy.addObserver(deleteFileAccessEventsFormatter);
        modifiedProxy.addObserver(modifyFileAccessEventsFormatter);
        renamedProxy.addObserver(renameFileAccessEventsFormatter);
        movedProxy.addObserver(moveFileAccessEventsFormatter);
        movedRecycleBinProxy.addObserver(moveToRecycleBinFileAccessEventsFormatter);
        processInfoProxy.addObserver(processInfoFormatter);
        logEntryProxy.addObserver(logEntryFormatter);

        //Start the thread that put the triples in the engine
        try {
            fsStream.initService();
            psStream.initService();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
