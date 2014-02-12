JCC = javac
JFLAGS = -g

default: Worker.class KKKKMultiServerThread.class KKMultiServer.class

Worker.class: Worker.java
	$(JCC) $(JFLAGS) Worker.java

ServerThread.class: KKKKMultiServerThread.java
	$(JCC) $(JFLAGS) KKKKMultiServerThread.java

KKMultiServer.class: KKMultiServer.java
	$(JCC) $(JFLAGS) KKMultiServer.java

clean: 
	$(RM) *.class
