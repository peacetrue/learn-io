#!/bin/bash

#rm -rf strace/*
#strace -o strace/java.log -f gradlew test --tests SocketTest.defaultProperties
strace -o strace/java.log -f java Main
#strace -o strace/java.log -f java Main
#strace -o strace/java.log -ff java Main
#strace -o strace/java.log java -version
#strace -o strace/java1.log -f java -version
