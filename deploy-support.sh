#!/bin/bash

# Update projects and sub projects
updateProjects(){
    local file='project.properties'
    cd $1
    local folder=$(pwd)
    if [ -f $file ]
    then      
        regex="android\.library\.reference\.[0-9]+=(.*)"
        while read -r line; do
            if [[ $line =~ $regex ]] 
            then
                local libpath="${BASH_REMATCH[1]}"
                if [ -d $libpath ]
                then
                    echo '---------------------------------'
                    echo "Update project ${BASH_REMATCH[1]}"

                    android update project -p ${libpath} -t android-19

                    # Update support lib to have the same everwhere
                    if [[ -e "$libpath/libs/android-support-v4.jar" ]]
                    then
                        echo "Copy lib $folder/android-support-v4.jar to $libpath/libs/"
                        cp -f $folder/android-support-v4.jar $libpath/libs/
                    fi
                    echo '---------------------------------'

                    updateProjects ${libpath}
                    cd $folder
                fi
            fi
        done < "$file"
    fi
}

updateProjects .
