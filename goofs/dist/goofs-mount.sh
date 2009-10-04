#!/bin/sh

#####################################################################
# Uncomment the following line to use a specific JRE
#JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun/jre

# Adjust this to point to the directory into which you extracted goofs.jar
GOOFS_HOME=.

# Adjust this to point to the directory containing the version of libjavafs.so corresponding to your version of fuse
JNI_LIB=$GOOFS_HOME/jni/lib32/fuse-2.7.4
#####################################################################

$JAVA_HOME/bin/java -Djava.library.path=$JNI_LIB -jar $GOOFS_HOME/goofs.jar -f $1