import ac.at.tuwien.logparser.services.LogConverterService;
import ac.at.tuwien.logparser.tdb.TDBConnection;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Agnes on 12.09.18.
 */
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = {"at.ac.tuwien.logparser.service"})
@ContextConfiguration(classes = {LogConverterService.class})
public class TDBConnectionTest {

    @Test
    public void create_Background_Knowledge_Resources(){

    }


}
