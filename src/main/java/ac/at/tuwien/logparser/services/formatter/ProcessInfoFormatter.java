package ac.at.tuwien.logparser.services.formatter;

import ac.at.tuwien.logparser.entities.ProcessInfo;
import ac.at.tuwien.logparser.services.CSparqlStreamService;
import ac.at.tuwien.logparser.services.LogConverterService;
import ac.at.tuwien.logparser.services.util.ServiceUtil;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;
import org.apache.commons.httpclient.util.DateParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Observable;

@Component
public class ProcessInfoFormatter extends ResultFormatter {

    @Autowired
    private LogConverterService logConverterService;

    @Override
    public void update(Observable o, Object arg) {
        RDFTable q = (RDFTable) arg;
        Iterator var4 = q.iterator();

        while (var4.hasNext()) {
            RDFTuple t = (RDFTuple) var4.next();
            ProcessInfo info = new ProcessInfo(t.get(1), t.get(2), t.get(3), t.get(4), t.get(5));
            logConverterService.handleProcessInfo(info);
        }
    }
}
