JCC = javac
JFLAGS = -g

default: Worker.class KKMultiServerThread.class KKMultiServer.class

Worker.class: Worker.java
	$(JCC) $(JFLAGS) Worker.java

KKMultiServerThread.class: KKMultiServerThread.java
	$(JCC) $(JFLAGS) KKMultiServerThread.java

KKMultiServer.class: KKMultiServer.java
	$(JCC) $(JFLAGS) KKMultiServer.java

clean: 
	$(RM) *.class
