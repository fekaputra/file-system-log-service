# FileSystem-LogParser

**Preconditions**
    
   Create folder structure:
       
   - tdb/DB_Background
   - tdb/DB_FileAccessEvent
   - tdb/DB_LogEntry
   - tdb/DB_ProcessInfo
        
---


- **Start Logstash**

   - Download Logstash 6.3.0 from https://www.elastic.co/downloads/logstash
   - add .config files from '/logstash-files'
   - define pipelines.yml - example can be found in '/logstash-files'
   
   to start logstash run:
    
        > cd logstash-6.3.0
        > sudo bin/logstash
        

- **Start process info script**

        > sudo sh processinfo.sh
        

- **Start TripleWave instance for process info**

        > cd TripleWave-processInfo
        > sh start.sh


- **Start TripleWave instance for file system logs**

        > cd TripleWave-fileSystemLog
        > sh start.sh


- **Run Application**

        mvn spring-boot:run


- **Start Web UI**

        > cd app
        > npm start
