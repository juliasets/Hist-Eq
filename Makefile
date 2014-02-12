JCC = javac
JFLAGS = -g

default: Worker.class KKMultiServerThread.class KKMultiServer.class KnockKnockServer.class KnockKnockProtocol.class KnockKnockClient.class

Worker.class: Worker.java
	$(JCC) $(JFLAGS) Worker.java

KKMultiServerThread.class: KKMultiServerThread.java
	$(JCC) $(JFLAGS) KKMultiServerThread.java

KKMultiServer.class: KKMultiServer.java
	$(JCC) $(JFLAGS) KKMultiServer.java

KnockKnockServer.class: KnockKnockServer.java
	$(JCC) $(JFLAGS) KnockKnockServer.java

KnockKnockProtocol.class: KnockKnockProtocol.java
	$(JCC) $(JFLAGS) KnockKnockProtocol.java

KnockKnockClient.class: KnockKnockClient.java
	$(JCC) $(JFLAGS) KnockKnockClient.java

clean: 
	$(RM) *.class *~

