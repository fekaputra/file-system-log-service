package ac.at.tuwien.logparser; /**
 * Created by Agnes on 01.09.18.
 */

import ac.at.tuwien.logparser.entities.ProcessInfo;
import ac.at.tuwien.logparser.services.*;
import ac.at.tuwien.logparser.services.test.CSparqlStreamServiceTest;
import ac.at.tuwien.logparser.services.test.TestWebSocketClient;
import ac.at.tuwien.logparser.services.util.ServiceUtil;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.log4j.Logger;
import org.java_websocket.drafts.Draft_10;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.net.*;


@SpringBootApplication
@EnableScheduling
public class Application {
    private final static Logger logger = Logger.getLogger(Application.class.getName());

    @Autowired
    private CSparqlStreamServiceTest cSparqlStreamServiceTest;
    @Autowired
    private CSparqlStreamService cSparqlStreamService;
    @Autowired
    private LogConverterService logConverterService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initWebSocketTriplewave() {

       /* File outputFolderFAE = new File("tdb/DB_FileAccessEvent");
        final File[] filesFAE = outputFolderFAE.listFiles();
        for(File f : filesFAE) {
            f.delete();
        }
        File outputFolderLE = new File("tdb/DB_LogEntry");
        final File[] filesLE = outputFolderLE.listFiles();
        for(File f : filesLE) {
            f.delete();
        }
        File outputFolderPI = new File("tdb/DB_ProcessInfo");
        final File[] filesPI = outputFolderPI.listFiles();
        for(File f : filesPI) {
            f.delete();
        }*/

        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            String ipExt = in.readLine(); //you get the IP as a String
            LogConverterService.setIpExternal(ipExt);
            //System.out.println("Your external IP address : " + ipExt);
        } catch (IOException e) {
            logger.error("Error receiving external IP Address");
        }
        // get all currently running process information of current user
        String s;
        try {
            String username = System.getProperty("user.name");
            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    "ps aux | pgrep -l -u " + username
            };

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            int idCount = 0;
            String time = ServiceUtil.getCurrentTimeStamp();
            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                String[] values = s.split("(?<=([0-9] ))(?=[a-zA-Z ]+)");
                ProcessInfo info = new ProcessInfo("start", String.valueOf(idCount++),
                        time, values[1], values[0].replaceAll("\\s+", ""));
              /*  try {
                    String timestampString = ServiceUtil.parseDateToString(
                            ServiceUtil.parseDate(info.getTimestamp(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
                            "EEE MMM dd HH:mm:ss yyyy");
                    info.setTimestamp(timestampString);
                } catch (DateParseException e) {
                    e.printStackTrace();
                }*/

                logConverterService.handleProcessInfo(info);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // livestream loader into txt files - for testing purposes
      /* try {
            TestWebSocketClient client1 = new TestWebSocketClient(new URI("ws://localhost:8125/tw/stream"),
                    new Draft_10(), "processInfoLogs14.txt");
            client1.connect();
            TestWebSocketClient client2 = new TestWebSocketClient(new URI("ws://localhost:8124/tw/stream"),
                    new Draft_10(), "fileSystemLogs14.txt");
            client2.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }*/

        // Test csparql service for test files
     /*   try {
            cSparqlStreamServiceTest.initCSparqlEngine();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        // livestream csparql service
         try {
          cSparqlStreamService.initCSparqlEngine();
        } catch (ParseException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}