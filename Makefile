JCC = javac
JFLAGS = -g

default: Worker.class ServerThread.class MultiServer.class

Worker.class: Worker.java
	$(JCC) $(JFLAGS) Worker.java

ServerThread.class: serverThread.java
	$(JCC) $(JFLAGS) serverThread.java

MultiServer.class: multiServer.java
	$(JCC) $(JFLAGS) multiServer.java

clean: 
	$(RM) *.class
