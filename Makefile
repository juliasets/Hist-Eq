SHELL := /bin/bash

JCC := javac

CLASSPATH := .:commons-imaging.jar:sigar.jar

JARCP := $(shell echo $(CLASSPATH) | sed "s/:/ /g")
ifeq ($(shell uname -o),Cygwin)
        CLASSPATH := "$(shell cygpath -wp $(CLASSPATH))"
endif

JFLAGS = -g -cp $(CLASSPATH)

JAR = jar cmfe <(echo "Class-Path: $(JARCP)")

.PHONY: default
default: Comrade.class Commissar.class
	$(JAR) Comrade.jar Comrade *.class
	$(JAR) Commissar.jar Commissar *.class

Comrade.class: Comrade.java Protocol.java Communicator.java ImageCommunicator.java Worker.java sigar.jar commons-imaging.jar
	$(JCC) $(JFLAGS) Comrade.java
	
Commissar.class: Commissar.java Protocol.java Communicator.java ImageCommunicator.java commons-imaging.jar sigar.jar
	$(JCC) $(JFLAGS) Commissar.java

.PHONY: olddefault
olddefault: Worker.class CreateWorkers.class KKMultiServerThread.class KKMultiServer.class KnockKnockProtocol.class KnockKnockClient.class MassClient.class ImageComm.class
	$(JAR) MassClient.jar MassClient *.class
	$(JAR) CreateWorkers.jar CreateWorkers *.class
	$(JAR) KKMultiServer.jar KKMultiServer *.class
	# $(JAR) OUTPUT.JAR ENTRYPOINTNAME *.class

CreateWorkers.class: CreateWorkers.java Worker.class
	$(JCC) $(JFLAGS) CreateWorkers.java

Worker.class: Worker.java commons-imaging.jar
	$(JCC) $(JFLAGS) Worker.java

KKMultiServerThread.class: KKMultiServerThread.java ProcessorAccessList.class commons-imaging.jar
	$(JCC) $(JFLAGS) KKMultiServerThread.java

KKMultiServer.class: KKMultiServer.java ProcessorAccessList.class KKMultiServerThread.class
	$(JCC) $(JFLAGS) KKMultiServer.java

TestServer.class: TestServer.java ImageComm.class commons-imaging.jar
	$(JCC) $(JFLAGS) TestServer.java

KnockKnockProtocol.class: KnockKnockProtocol.java
	$(JCC) $(JFLAGS) KnockKnockProtocol.java

KnockKnockClient.class: KnockKnockClient.java ImageComm.class commons-imaging.jar
	$(JCC) $(JFLAGS) KnockKnockClient.java

MassClient.class: MassClient.java KnockKnockClient.class
	$(JCC) $(JFLAGS) MassClient.java

TestClient.class: TestClient.java ImageComm.class commons-imaging.jar
	$(JCC) $(JFLAGS) TestClient.java

ImageComm.class: ImageComm.java commons-imaging.jar
	$(JCC) $(JFLAGS) ImageComm.java

ProcessorAccessList.class: ProcessorAccessList.java
	$(JCC) $(JFLAGS) ProcessorAccessList.java

sigar.jar:
	wget -O hyperic-sigar-1.6.4.tar.gz http://downloads.sourceforge.net/project/sigar/sigar/1.6/hyperic-sigar-1.6.4.tar.gz
	tar -zxvf ./hyperic-sigar-1.6.4.tar.gz
	rm hyperic-sigar-1.6.4.tar.gz
	mv hyperic-sigar-1.6.4/sigar-bin/lib/* .
	rm -r hyperic-sigar-1.6.4/
	chmod +x *.dll

commons-imaging.jar:
	wget -O commons-imaging.jar http://repository.apache.org/content/groups/snapshots/org/apache/commons/commons-imaging/1.0-SNAPSHOT/commons-imaging-1.0-20140224.222237-6.jar

.PHONY: clean
clean:
	$(RM) *.class *.jar *~ *.sl *.so *.dylib *.dll *.lib

