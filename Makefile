JCC = javac
JFLAGS = -g

default: Worker.class ServerThread.class

Worker.class: Worker.java
	$(JCC) $(JFLAGS) Worker.java

ServerThread.class: serverThread.java
	$(JCC) $(JFLAGS) serverThread.java

clean: 
	$(RM) *.class
