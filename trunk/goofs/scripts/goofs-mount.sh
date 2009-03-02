#!/bin/sh

#####################################################################
# Adjust this to point to the home directory of a java 1.5 jre
JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun/jre

# Adjust this to point to the directory into which you extracted goofs.jar
GOOFS_HOME=/home/rwynn/goofs/goofs-0.7.2
#####################################################################

$JAVA_HOME/bin/java -Djava.library.path=$GOOFS_HOME/jni/fuse-2.7.2 -jar $GOOFS_HOME/goofs.jar -f $1