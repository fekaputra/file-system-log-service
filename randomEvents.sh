#!/usr/bin/env bash

files=()
num=0
arrOps=(create editFile copyFileDiffDir moveFile moveFileToTrash renameFile)
arrTargetPaths=("/Users/Agnes/Desktop/test" "/Users/Agnes/Desktop/test-2" "/Volumes/USB" "/Users/Agnes/Dropbox/test" "/Users/Agnes/Google Drive/test" "/Users/Agnes/Desktop/test")
arrFileExt=(xml docx txt xlsx txt)
echo "see log file 'logRandomEvents.txt' in current directory for performed file operations"
touch logRandomEvents.txt

editFile(){
    # get random file to edit
    getFilesOfDirectory
    # select random file from files selection
    calcRandom $((${#files[@]}-1))
    fileToEdit="${files[$num]}"
    if [ -e "$fileToEdit" ]
    then
        addLogEntry "edit;""$fileToEdit"";"
        open "$fileToEdit"
        sleep 10
        #send some text to open file and hit cmd+s to save changes and close program again
        cliclick w:1000 kd:cmd t:v ku:cmd
        cliclick w:500 kd:cmd t:s ku:cmd w:1000 kd:cmd t:q
    fi
}

copyFileDiffDir(){
    # get random selection of files to copy
    getFilesOfDirectory
    # select random file from files selection
    calcRandom $((${#files[@]}-1))
    fileToCopy="${files[$num]}"

    # get random target directory to copy to
    calcRandom $((${#arrTargetPaths[@]}-1))
    targetDir="${arrTargetPaths[$num]}"
    dir=$(dirname "$fileToCopy")
    basename=$(basename -- "$fileToCopy")
    newDirAndName=$targetDir"/"$basename

    if [ -e "$fileToCopy" ]
    then
        open "$fileToCopy"
        cliclick w:1000 kd:cmd t:q
        diff=false
        #check if current file and "new file" (to copy to) are identical
        cmp --silent "$fileToCopy" "$newDirAndName" || diff=true
        if [ "$diff" = true ] ; then
            addLogEntry "copy;""$fileToCopy"";""$targetDir"
            #cp -fr "$fileToCopy" "$targetDir"
            cp "$fileToCopy" "$targetDir"
        fi
    fi

}

#copyFileSameDir(){
    # get random selection of files to copy
#    getFilesOfDirectory
    # select random file from files selection
#    calcRandom $((${#files[@]}-1))
#    fileToCopy="${files[$num]}"

#    if [ -e "$fileToCopy" ]
#    then
        # extract dir, filename and extension
#        dir=$(dirname "$fileToCopy")
#        filename=$(basename -- "$fileToCopy")
#        extension="${filename##*.}"
#        filename="${filename%.*}"

        #create new filename + previous path
#        newDirAndFilename=$dir"/"$filename"Copy."$extension

#        addLogEntry "copy;""$fileToCopy"";""$newDirAndFilename"
#        cp "$fileToCopy" "$newDirAndFilename"
#    fi
#}

moveFile(){
    # get random selection of files to move
    getFilesOfDirectory
    # select random file from files selection
    calcRandom $((${#files[@]}-1))
    fileToMove="${files[$num]}"

    if [ -e "$fileToMove" ]
    then
        # get random target directory to copy to
        calcRandom $((${#arrTargetPaths[@]}-1))
        targetDir="${arrTargetPaths[$num]}"
        originalDir=$(dirname "$fileToMove")
        if [ "$originalDir" != "$targetDir" ] ; then
            addLogEntry "move;""$fileToMove"";""$targetDir"
            mv "$fileToMove" "$targetDir"
        fi
    fi
}

moveFileToTrash(){
    # get random selection of files to move
    getFilesOfDirectory
    # select random file from files selection
    calcRandom $((${#files[@]}-1))
    fileToMove="${files[$num]}"

    if [ -e "$fileToMove" ]
    then
        addLogEntry "moveToTrash;""$fileToMove"";/Users/Agnes/.Trash"
        mv "$fileToMove" "/Users/Agnes/.Trash/"
    fi
}

renameFile(){
    # get random selection of file to rename
    getFilesOfDirectory
    # select random file from files selection
    calcRandom $((${#files[@]}-1))
    fileToRename="${files[$num]}"

    if [ -e "$fileToRename" ]
    then
         dir=$(dirname "$fileToRename")
        filename=$(basename "$fileToRename")
        extension="${filename##*.}"
        filename="${filename%.*}"
        newName=$dir"/"$filename"X."$extension

        addLogEntry "rename;""$fileToRename"";""$newName"
        mv "$fileToRename" "$newName"
    fi
}

create(){
    # get random target directory to create a file in
    calcRandom $((${#arrTargetPaths[@]}-1))
    targetDir="${arrTargetPaths[$num]}"

    # get random file extension
    calcRandom $((${#arrFileExt[@]}-1))
    fileExt="${arrFileExt[$num]}"

    timestamp=$(date +%s)
    filename=$timestamp".""$fileExt"
    filepath="$targetDir""/""$filename"

    if [[ "$fileExt" == "xlsx" ]]; then
        open -a "Microsoft Excel"
        sleep 2
        cliclick kd:cmd t:n ku:cmd # neue Arbeitsmappe erstellen
        cliclick kd:cmd t:v ku:cmd
        sleep 3
        cliclick kd:cmd t:s ku:cmd
        timestamp=$(date +%s)
        filename=$timestamp
        pathname="$targetDir""/"$timestamp
        addLogEntry "create;""$pathname"".xlsx;"
        cliclick w:500 t:"$pathname"
        sleep 3
        cliclick kp:enter
        sleep 3
        cliclick kp:enter
        sleep 1
        cliclick kd:cmd t:q
    else
        addLogEntry "create;""$filepath"";"
        touch "$filepath"
        open "$filepath"
        sleep 10
        cliclick w:1000 kd:cmd t:v ku:cmd
        cliclick kd:cmd t:s ku:cmd w:1000 kd:cmd t:q
    fi


}

# returns a random number between 0 and a max (parameter)
calcRandom(){
    if [ $1 == 0 ]; then
        num=0
    else
        num=$(( ( RANDOM % $1 ) + 0 ))
    fi
}

# saves all files of a directory (1. input parameter) in array "files"
getFilesOfDirectory(){
    calcRandom $((${#arrTargetPaths[@]}-1))
    if [ $num == -1 ]; then
        echo no event triggered
        exit 1
    else
        path=${arrTargetPaths[$num]}
        for f in "$path"/*; do
            if [[ -f "$f" ]]; then
               files+=("$f")
            fi
        done
    fi
    if [ ${#files[@]} == 0 ]; then
       echo no files found in dir ${arrTargetPaths[$num]} - no event triggered
       exit 1
    fi
}

addLogEntry(){
    echo "$1"";"$(date +%Y-%m-%d-%H:%M:%S) >> logRandomEvents.txt
}


numlines=$(wc -l logRandomEvents.txt | awk '{ print $1 }')

echo create events until 100 events are logged
while [ $numlines -lt 100 ]
do
    calcRandom $((${#arrOps[@]}))
    echo $i event: ${arrOps[$num]}
    ${arrOps[$num]}
    sleep 7
    numlines=$(wc -l logRandomEvents.txt | awk '{ print $1 }')
done
