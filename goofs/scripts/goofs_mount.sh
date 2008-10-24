#!/bin/sh

# Adjust this to point to the home directory of a java 1.5 jdk
JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun-1.5.0_update16

# Adjust this to point to the directory containing the version of libjavafs.so corresponding to your version of fuse
JNI_LIB=/home/rwynn/workspace2/goofs/jni/fuse-2.7.2-32.1

# Adjust this to point to the directory containing the fuse, gdata, commons-logging, and sun libraries (dependencies of gdata)
JAVA_LIB=/home/rwynn/workspace2/goofs/lib

USER=$1
PASS=$2
MNT=$3

$JAVA_HOME/bin/java -Djava.library.path=$JNI_LIB -Dusername=$USER -Dpassword=$PASS -Dorg.apache.commons.logging.Log=fuse.logging.FuseLog -Dfuse.logging.level=DEBUG -Dcom.sun.management.jmxremote -classpath /home/rwynn/workspace2/goofs/bin:$JAVA_LIB/activation.jar:$JAVA_LIB/mail.jar:$JAVA_LIB/commons-logging-1.0.4.jar:$JAVA_LIB/gdata-appsforyourdomain-1.0.jar:$JAVA_LIB/gdata-appsfhome/roryourdomain-meta-1.0.jar:$JAVA_LIB/gdata-base-1.0.jar:$JAVA_LIB/gdata-blogger-1.0.jar:$JAVA_LIB/gdata-blogger-meta-1.0.jar:$JAVA_LIB/gdata-books-1.0.jar:$JAVA_LIB/gdata-books-meta-1.0.jar:$JAVA_LIB/gdata-calendar-1.0.jar:$JAVA_LIB/gdata-calendar-meta-1.0.jar:$JAVA_LIB/gdata-client-1.0.jar:$JAVA_LIB/gdata-client-meta-1.0.jar:$JAVA_LIB/gdata-codesearch-1.0.jar:$JAVA_LIB/gdata-codesearch-meta-1.0.jar:$JAVA_LIB/gdata-contacts-1.0.jar:$JAVA_LIB/gdata-contacts-meta-1.0.jar:$JAVA_LIB/gdata-core-1.0.jar:$JAVA_LIB/gdata-docs-1.0.jar:$JAVA_LIB/gdata-docs-meta-1.0.jar:$JAVA_LIB/gdata-finance-1.0.jar:$JAVA_LIB/gdata-finance-meta-1.0.jar:$JAVA_LIB/gdata-health-1.0.jar:$JAVA_LIB/gdata-health-meta-1.0.jar:$JAVA_LIB/gdata-media-1.0.jar:$JAVA_LIB/gdata-photos-1.0.jar:$JAVA_LIB/gdata-photos-meta-1.0.jar:$JAVA_LIB/gdata-spreadsheet-1.0.jar:$JAVA_LIB/gdata-spreadsheet-meta-1.0.jar:$JAVA_LIB/gdata-webmastertools-1.0.jar:$JAVA_LIB/gdata-webmastertools-meta-1.0.jar:$JAVA_LIB/gdata-youtube-1.0.jar:$JAVA_LIB/gdata-youtube-meta-1.0.jar:$JAVA_LIB/fuse-j.jar goofs.fs.GoofsFS -f $MNT

