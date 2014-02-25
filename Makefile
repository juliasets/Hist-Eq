SHELL := /bin/bash

JCC := javac

CLASSPATH := .:commons-imaging.jar:sigar.jar:log4j.jar

JARCP := $(shell echo $(CLASSPATH) | sed "s/:/ /g")
ifeq ($(shell uname -o),Cygwin)
        CLASSPATH := "$(shell cygpath -wp $(CLASSPATH))"
endif

JFLAGS = -g -cp $(CLASSPATH)

JAR = jar cmfe classpath.txt

.PHONY: default
default: Comrade.jar Commissar.jar

Comrade.jar: Comrade.class classpath.txt
	$(JAR) Comrade.jar Comrade *.class

Commissar.jar: Commissar.class classpath.txt
	$(JAR) Commissar.jar Commissar *.class

Comrade.class: Comrade.java Protocol.java Communicator.java ImageCommunicator.java Worker.java commons-imaging.jar sigar.jar
	$(JCC) $(JFLAGS) Comrade.java
	
Commissar.class: Commissar.java Protocol.java Communicator.java ImageCommunicator.java commons-imaging.jar sigar.jar
	$(JCC) $(JFLAGS) Commissar.java

classpath.txt:
	echo "Class-Path: $(JARCP)" >classpath.txt

sigar.jar:
	wget -O hyperic-sigar-1.6.4.tar.gz http://downloads.sourceforge.net/project/sigar/sigar/1.6/hyperic-sigar-1.6.4.tar.gz
	tar -zxvf ./hyperic-sigar-1.6.4.tar.gz
	rm hyperic-sigar-1.6.4.tar.gz
	mv hyperic-sigar-1.6.4/sigar-bin/lib/* .
	rm -r hyperic-sigar-1.6.4/
	chmod +x *.dll

commons-imaging.jar:
	wget -O commons-imaging.jar http://repository.apache.org/content/groups/snapshots/org/apache/commons/commons-imaging/1.0-SNAPSHOT/commons-imaging-1.0-20140224.222237-6.jar

.PHONY: cleanish
cleanish:
	mv commons-imaging.jar commons-imaging.jar.stash
	mv sigar.jar sigar.jar.stash
	mv log4j.jar log4j.jar.stash
	$(RM) *.class *.jar *~ classpath.txt
	mv log4j.jar.stash log4j.jar
	mv sigar.jar.stash sigar.jar
	mv commons-imaging.jar.stash commons-imaging.jar

.PHONY: clean
clean:
	$(RM) *.class *.jar *~ classpath.txt

