import ac.at.tuwien.logparser.services.LogConverterService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

/**
 * Created by Agnes on 07.09.18.
 */

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = {"at.ac.tuwien.logparser.service"})
@ContextConfiguration(classes = {LogConverterService.class})
public class LogConverterServiceTest {


    @Autowired
    private LogConverterService logConverterService;

    @Test
    public void test_query() {
        String query = "";
        logConverterService.getNtFileOfTriples(query, "tdb/DB_FileAccessEvent");
    }
}
