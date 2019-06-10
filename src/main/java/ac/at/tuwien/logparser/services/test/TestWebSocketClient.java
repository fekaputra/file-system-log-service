package ac.at.tuwien.logparser.services.test;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class TestWebSocketClient extends WebSocketClient {

    private static Logger logger = LoggerFactory.getLogger(TestWebSocketClient.class.getName());
    private String testFileName;

    public TestWebSocketClient(URI serverUri, Draft draft, String testFileName) {
        super(serverUri, draft);
        this.testFileName = testFileName;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("open websocket: "+uri);
    }

    @Override
    public void onMessage(String message) {
        try {
            long time = System.currentTimeMillis();
            Object jsonObject = JsonUtils.fromString(message);
            RDFDataset rdfDataset = (RDFDataset) JsonLdProcessor.toRDF(jsonObject);
            List<RDFDataset.Quad> key = rdfDataset.getQuads("@default");
            List<RDFDataset.Quad> graph = rdfDataset.getQuads(key.get(0).getSubject().getValue());
            for(RDFDataset.Quad t: graph) {
                RdfQuadruple quadTriple = new RdfQuadruple(t.getSubject().getValue(),
                        t.getPredicate().getValue(), t.getObject().getValue(), time);
                try{
                    BufferedWriter writer = new BufferedWriter(new FileWriter(testFileName, true));
                    String line = quadTriple.getSubject()+"§"+quadTriple.getPredicate()+"§"+quadTriple.getObject()+";";
                    logger.debug(line);
                    writer.append(line);
                    writer.append("\n");
                    writer.close();
                }catch (Exception e){//Catch exception if any
                    System.err.println("Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.info("close websocket: "+uri);
    }

    @Override
    public void onError(Exception e) {
        logger.info("error on websocket: "+uri);
    }
}
