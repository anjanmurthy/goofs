#!/bin/sh

#####################################################################
# Adjust this to point to the home directory of a java 1.5 jre
JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun-1.5.0_update17/jre

# Adjust this to point to the directory in which you extracted goofs
GOOFS_HOME=/home/rwynn/Desktop/goofs
#####################################################################

$JAVA_HOME/bin/java -Djava.library.path=$GOOFS_HOME/jni/fuse-2.7.2 -jar $GOOFS_HOME/goofs.jar -f $1