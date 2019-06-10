package ac.at.tuwien.logparser.services.test;

import ac.at.tuwien.logparser.entities.Queries;
import ac.at.tuwien.logparser.services.formatter.*;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class CSparqlStreamServiceTest {

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


    public CSparqlStreamServiceTest() {
    }

    public void initCSparqlEngine() throws ParseException {
        TestFileStreamer fs = new TestFileStreamer("http://streamreasoning.org/streams/fs", 1000L, "fileSystemLogs14.txt");
        TestFileStreamer ps = new TestFileStreamer("http://streamreasoning.org/streams/ps", 1000L, "processInfoLogs14.txt");

        //Create csparql engine instance
        CsparqlEngineImpl engine = new CsparqlEngineImpl();

        //Initialize the engine instance
        //The initialization creates the static engine (SPARQL) and the stream engine (CEP)
        engine.initialize(true);

        //Register new stream in the engine
        engine.registerStream(fs);
        engine.registerStream(ps);

        Thread fsThread = new Thread(fs);
        Thread psThread = new Thread(ps);

        //Register new query in the engine
        CsparqlQueryResultProxy createdProxy = engine.registerQuery(Queries.createdTest, false);
        CsparqlQueryResultProxy deletedProxy = engine.registerQuery(Queries.deletedTest, false);
        CsparqlQueryResultProxy modifiedProxy = engine.registerQuery(Queries.modifiedTest, false);
        CsparqlQueryResultProxy renamedProxy = engine.registerQuery(Queries.renamedTest, false);
        CsparqlQueryResultProxy movedProxy = engine.registerQuery(Queries.movedTest, false);
        CsparqlQueryResultProxy movedRecycleBinProxy = engine.registerQuery(Queries.movedToRecycleBinTest, false);
        CsparqlQueryResultProxy processInfoProxy = engine.registerQuery(Queries.processInfoTest, false);
        CsparqlQueryResultProxy logEntryProxy = engine.registerQuery(Queries.allLogEntriesTest, false);

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
        fsThread.start();
        psThread.start();
    }
}
