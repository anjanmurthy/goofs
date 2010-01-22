#!/bin/sh

#####################################################################
# Edit the following line to use a specific JRE
JAVA=java

# Adjust this to point to the directory into which you extracted goofs.jar
GOOFS_HOME=.

# Adjust this to your installed fuse version
FUSE_VERSION=2.7.4
#####################################################################

arch=$(uname -m)
if [ "$arch"=="x86_64" ]
then
    JNI_HOME=$GOOFS_HOME/jni/lib64
else
    JNI_HOME=$GOOFS_HOME/jni/lib32
fi

JNI_LIB=$JNI_HOME/fuse-$FUSE_VERSION

$JAVA -Djava.library.path=$JNI_LIB -jar $GOOFS_HOME/goofs.jar -f $1