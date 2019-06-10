# FileSystem-LogParser
- **Start Logstash**
    
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

