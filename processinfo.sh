#!/bin/bash

(sudo dtrace -qn 'proc:::exit { printf("exit %Y %s\n",walltimestamp,curpsinfo->pr_psargs); } proc:::start { printf("start %Y %s %d\n",walltimestamp,curpsinfo->pr_psargs,pid); }') | while IFS= read -r line; do
   IFS=' ' read -a processInfo <<< "$line";  # save line content into array 'processInfo'

   operation="${processInfo[0]}"
   year="${processInfo[1]}"
   month="${processInfo[2]}"
   day="${processInfo[3]}"
   time="${processInfo[4]}"
   timestamp=$year' '$month' '$day' '$time
   processName="${processInfo[5]}"

   # get length of processInfo array
   tLen=${#processInfo[@]}

   if [ "$operation" == "start" ]; then
        processNameIndex=${tLen}-1
        index=${tLen}-1
        pid="${processInfo[${index}]}" #in case of start a pid will be set
   else
        processNameIndex=${tLen}
        pid=-1 #otherwise: pid = -1 when process exits
   fi

   # loop over array fields between 6 until second to last element in order to get full process name (in case of whitespaces)
   for (( i=6; i<${processNameIndex}; i++ ));
   do
      processName="${processName} "${processInfo[$i]} #get process name in case full name is separated by whitespaces
   done

   if [[ "$processName" != "curl" && "$processName" != "sh" && "$processName" != "bash" && "$processName" != "kernel_task" ]]; then
        body="{\"operation\": \"$operation\", \"timestamp\": \"$timestamp\", \"processName\": \"$processName\", \"pid\": "$pid"}"
       curl -X POST -i -H "Content-type: application/json" -X POST http://127.0.0.1:9600 -d "$body"
       echo $body
   fi
done