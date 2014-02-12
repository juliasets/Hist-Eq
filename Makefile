JCC = javac
JFLAGS = -g

default: Worker.class KKMultiServerThread.class KKMultiServer.class KnockKnockServer.java

Worker.class: Worker.java
	$(JCC) $(JFLAGS) Worker.java

KKMultiServerThread.class: KKMultiServerThread.java
	$(JCC) $(JFLAGS) KKMultiServerThread.java

KKMultiServer.class: KKMultiServer.java
	$(JCC) $(JFLAGS) KKMultiServer.java

KnockKnockServer.class: KnockKnockServer.java
	$(JCC) $(JFLAGS) KnockKnockServer.java

clean: 
	$(RM) *.class
