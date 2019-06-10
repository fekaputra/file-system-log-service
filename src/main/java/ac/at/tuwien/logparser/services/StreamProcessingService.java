package ac.at.tuwien.logparser.services;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

public class StreamProcessingService extends RdfStream {

    private static Logger logger = LoggerFactory.getLogger(StreamProcessingService.class.getName());
    private WebSocketClient mWs;
    private String wsIri;
    private StreamProcessingService instance;

    public StreamProcessingService(String iri) {
        super(iri);
        instance = this;
        this.wsIri = iri;
    }

    public void initService() throws URISyntaxException {
        mWs = new WebSocketClient(new URI(wsIri), new Draft_10()) {
            @Override
            public synchronized void onMessage(String message) {
                try {
                    long time = System.currentTimeMillis();
                    Object jsonObject = JsonUtils.fromString(message);
                    RDFDataset rdfDataset = (RDFDataset) JsonLdProcessor.toRDF(jsonObject);
                    List<RDFDataset.Quad> key = rdfDataset.getQuads("@default");
                    List<RDFDataset.Quad> graph = rdfDataset.getQuads(key.get(0).getSubject().getValue());

                    for (Iterator<RDFDataset.Quad> t = graph.iterator(); t.hasNext(); ) {
                        RDFDataset.Quad quad = t.next();
                        RdfQuadruple quadTriple = new RdfQuadruple(quad.getSubject().getValue(),
                                quad.getPredicate().getValue(), quad.getObject().getValue(), time);

                        instance.put(quadTriple);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
                logger.info("open websocket connection");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("close websocket connection: " + code + " - " + reason);
            }

            @Override
            public void onError(Exception ex) {
                logger.error("error websocket: " + ex);
            }
        };
        mWs.connect();
    }
}
