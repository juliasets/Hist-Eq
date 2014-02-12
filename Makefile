JCC = javac

CLASSPATH = .:commons-imaging.jar
ifeq ($(shell uname -o),Cygwin)
        CLASSPATH := "$(shell cygpath -wp $(CLASSPATH))"
endif

JFLAGS = -g -cp $(CLASSPATH)

default: Worker.class KKMultiServerThread.class KKMultiServer.class KnockKnockServer.class KnockKnockProtocol.class KnockKnockClient.class ImageComm.class

Worker.class: Worker.java
	$(JCC) $(JFLAGS) Worker.java

KKMultiServerThread.class: KKMultiServerThread.java ProcessorAccessList.class
	$(JCC) $(JFLAGS) KKMultiServerThread.java

KKMultiServer.class: KKMultiServer.java ProcessorAccessList.class
	$(JCC) $(JFLAGS) KKMultiServer.java

KnockKnockServer.class: KnockKnockServer.java
	$(JCC) $(JFLAGS) KnockKnockServer.java

KnockKnockProtocol.class: KnockKnockProtocol.java
	$(JCC) $(JFLAGS) KnockKnockProtocol.java

KnockKnockClient.class: KnockKnockClient.java ImageComm.class commons-imaging.jar
	$(JCC) $(JFLAGS) KnockKnockClient.java

ImageComm.class: ImageComm.java commons-imaging.jar
	$(JCC) $(JFLAGS) ImageComm.java
	
ProcessorAccessList.class: ProcessorAccessList.java
	$(JCC) $(JFLAGS) ProcessorAccessList.java

commons-imaging.jar:
	wget -O commons-imaging.jar http://repository.apache.org/content/groups/snapshots/org/apache/commons/commons-imaging/1.0-SNAPSHOT/commons-imaging-1.0-20140107.130740-4.jar

clean: 
	$(RM) *.class *.jar *~

