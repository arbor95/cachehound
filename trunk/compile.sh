#!/bin/sh

ENCODING=${ENCODING:=utf8}

javac \
	-source 1.3 \
	-target 1.1 \
	-encoding $ENCODING \
	-cp ./lib/CompileEwe.zip:./lib/  \
	-d ./bin/ \
	-deprecation \
	-nowarn \
	./src/CacheWolf/*.java \
	./src/CacheWolf/*/*.java \
	./src/exp/*.java \
	./src/utils/*.java
