package ac.at.tuwien.logparser.services.formatter;

import ac.at.tuwien.logparser.entities.enums.FileAccessType;
import ac.at.tuwien.logparser.services.LogConverterService;
import ac.at.tuwien.logparser.services.util.ServiceUtil;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Observable;

@Component
public class RenameFileAccessEventsFormatter extends ResultFormatter {

    @Autowired
    private LogConverterService logConverterService;

    @Override
    public void update(Observable o, Object arg) {
        RDFTable q = (RDFTable) arg;
        Iterator var4 = q.iterator();

        while (var4.hasNext()) {
            RDFTuple t = (RDFTuple) var4.next();
            logConverterService.handleFileEvent(ServiceUtil.extractLogEntryFromRDFTruple(t), FileAccessType.Renamed);
        }
    }
}
