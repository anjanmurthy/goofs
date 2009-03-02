#!/bin/sh

# Adjust this to point to the home directory of a java 1.5 jre
JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun/jre

# Adjust this to point to the directory containing the version of libjavafs.so corresponding to your version of fuse
JNI_LIB=/home/rwynn/workspace/goofs/jni/fuse-2.7.2

# Adjust this to point to the directory containing the fuse, gdata, commons-logging, and sun libraries (dependencies of gdata)
JAVA_LIB=/home/rwynn/workspace/goofs/lib

# Adjust this to point to the bin directory of goofs project
GOOFS_BIN=/home/rwynn/workspace/goofs/bin

$JAVA_HOME/bin/java -Djava.library.path=$JNI_LIB -Dorg.apache.commons.logging.Log=fuse.logging.FuseLog -Dfuse.logging.level=DEBUG -Dcom.sun.management.jmxremote -classpath $GOOFS_BIN:$JAVA_LIB/activation.jar:$JAVA_LIB/mail.jar:$JAVA_LIB/commons-logging-1.1.1.jar:$JAVA_LIB/gdata-appsforyourdomain-1.0.jar:$JAVA_LIB/gdata-appsforyourdomain-1.0-meta.jar:$JAVA_LIB/gdata-base-1.0.jar:$JAVA_LIB/gdata-blogger-2.0.jar:$JAVA_LIB/gdata-blogger-meta-2.0.jar:$JAVA_LIB/gdata-books-1.0.jar:$JAVA_LIB/gdata-books-meta-1.0.jar:$JAVA_LIB/gdata-calendar-2.0.jar:$JAVA_LIB/gdata-calendar-meta-2.0.jar:$JAVA_LIB/gdata-client-1.0.jar:$JAVA_LIB/gdata-client-meta-1.0.jar:$JAVA_LIB/gdata-codesearch-2.0.jar:$JAVA_LIB/gdata-codesearch-meta-2.0.jar:$JAVA_LIB/gdata-contacts-2.0.jar:$JAVA_LIB/gdata-contacts-meta-2.0.jar:$JAVA_LIB/gdata-core-1.0.jar:$JAVA_LIB/gdata-docs-2.0.jar:$JAVA_LIB/gdata-docs-meta-2.0.jar:$JAVA_LIB/gdata-finance-2.0.jar:$JAVA_LIB/gdata-finance-meta-2.0.jar:$JAVA_LIB/gdata-health-2.0.jar:$JAVA_LIB/gdata-health-meta-2.0.jar:$JAVA_LIB/gdata-media-1.0.jar:$JAVA_LIB/gdata-photos-1.0.jar:$JAVA_LIB/gdata-photos-meta-1.0.jar:$JAVA_LIB/gdata-spreadsheet-2.0.jar:$JAVA_LIB/gdata-spreadsheet-meta-2.0.jar:$JAVA_LIB/gdata-webmastertools-2.0.jar:$JAVA_LIB/gdata-webmastertools-meta-2.0.jar:$JAVA_LIB/gdata-youtube-2.0.jar:$JAVA_LIB/gdata-youtube-meta-2.0.jar:$JAVA_LIB/fuse-j.jar goofs.fs.GoofsFS -f $1

