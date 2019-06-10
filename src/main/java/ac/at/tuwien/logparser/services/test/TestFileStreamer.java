package ac.at.tuwien.logparser.services.test;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ConcurrentModificationException;
import java.util.Scanner;

public class TestFileStreamer extends RdfStream implements Runnable{

    private long sleepTime;
    private String testFileName;

    public TestFileStreamer(String iri, long sleepTime, String testFileName) {
        super(iri);
        this.sleepTime = sleepTime;
        this.testFileName = testFileName;
    }

    @Override
    public void run() {
        Scanner read = null;
        try {
            read = new Scanner (new File(testFileName));
            read.useDelimiter(";\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        RdfQuadruple q = null;
        while(true){
            try{
                String line;
                while (read.hasNext()){
                    line = read.next();
                    String[] triple = line.split("§");
                    try{
                        q = new RdfQuadruple(triple[0], triple[1], triple[2], System.currentTimeMillis());
                        //logger.info(q.toString());
                        this.put(q);
                    } catch (ArrayIndexOutOfBoundsException e1) {
                        System.out.println(q.toString());
                    }catch (ConcurrentModificationException e2) {
                        System.out.println(e2.getMessage());
                        System.out.println(e2.getCause());
                    }
                }

                Thread.sleep(sleepTime);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
