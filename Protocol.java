

import java.net.*;
import java.io.*;
import java.util.*;


public class Protocol {

    private static final byte GETBUSYNESS = 0;
    private static final byte GETLIST = 1;
    private static final byte ANNOUNCESERVER = 2;
    private static final byte COMMUNICATE = 3;

    private class Host {
        public String hostname;
        public int port;
        public double busyness;
        public long lastpinged;
        public long lastsuccess;
        public boolean equals (Host other) {
            return this.hostname.equals(other.hostname) &&
                this.port == other.port;
        }
        public void toDataStream (DataOutputStream ds, long toffset)
            throws IOException {
            byte[] b = hostname.getBytes("UTF-8");
            ds.writeInt(b.length);
            ds.write(b, 0, b.length);
            ds.writeInt(port);
            ds.writeDouble(busyness);
            ds.writeLong(lastpinged - toffset);
            ds.writeLong(lastsuccess - toffset);
        }
        public void fromDataStream (DataInputStream ds, long toffset)
            throws IOException {
            byte[] b = new byte[ds.readInt()];
            ds.readFully(b);
            hostname = new String(b, "UTF-8");
            port = ds.readInt();
            busyness = ds.readDouble();
            lastpinged = ds.readLong() + toffset;
            lastsuccess = ds.readLong() + toffset;
        }
    }

    private class ProtocolHelper extends Thread {
        private ArrayList<Host> servers;
        public ProtocolHelper () {
            servers = new ArrayList<Host>();
            this.start();
        }
        /*
            Host host must already be in ArrayList<Host> servers.
        */
        private synchronized boolean ping (Host host) {
            host.lastpinged = System.currentTimeMillis();
            try (
                Socket socket = new Socket(host.hostname, host.port);
            ) {
                DataOutputStream dos =
                    new DataOutputStream(socket.getOutputStream());
                dos.writeByte(GETBUSYNESS);
                DataInputStream dis =
                    new DataInputStream(socket.getInputStream());
                host.busyness = dis.readDouble();
                dos.writeByte(GETLIST);
                long t_min = System.currentTimeMillis();
                int numentries = dis.readInt();
                long t_max = System.currentTimeMillis();
                long toffset = (t_min + t_max) / 2; // Close enough.
                for (int i = 0; i < numentries; ++i) {
                    Host remote = new Host();
                    remote.fromDataStream(dis, toffset);
                    if (servers.contains(remote)) {
                        Host localcopy = servers.get(servers.indexOf(remote));
                        if (localcopy.lastsuccess < remote.lastsuccess) {
                            localcopy.lastsuccess = remote.lastsuccess;
                            localcopy.busyness = remote.busyness;
                        }
                        if (localcopy.lastpinged < remote.lastpinged)
                            localcopy.lastpinged = remote.lastpinged;
                    } else servers.add(remote);
                }
                host.lastsuccess = host.lastpinged;
                return true;
            } catch (IOException e) {
                if (host.lastpinged - host.lastsuccess > 10000)
                    servers.remove(host);
                return false;
            }
        }
        public synchronized void run () {
            try {
                boolean dowait = true;
                for (;;) {
                    if (dowait) wait(500);
                    else dowait = true;
                    long bestlastpinged = Long.MAX_VALUE;
                    Host besthost = null;
                    for (int i = 0; i < servers.size(); ++i) {
                        Host host = servers.get(i);
                        if (host.lastpinged < bestlastpinged)
                            besthost = host;
                    }
                    if (besthost != null) dowait = ping(besthost);
                    else dowait = true;
                }
            } catch (InterruptedException e) { return; }
        }
        public synchronized void findAll (Host remote) {
            if (servers.contains(remote)) return;
            remote.lastpinged = 0;
            remote.busyness = 1.0;
            servers.add(remote);
            notifyAll();
        }
    }

    private ProtocolHelper ph;

    public Protocol () {
        ph = new ProtocolHelper();
    }

    public void addServer (String hostname, int port) {
        Host host = new Host();
        host.hostname = hostname;
        host.port = port;
        host.lastpinged = 0;
        host.lastsuccess = System.currentTimeMillis();
        ph.findAll(host);
    }

    private void handleBusynessRequest (Socket socket) {
        
    }

    private void handleAnnounceServer (Socket socket) {
        
    }

    private void announceServer (int port) {
        
    }

    public Communicator serveOnce (int port) throws IOException {
        announceServer(port);
        try (
            ServerSocket server = new ServerSocket(port);
        ) {
            Socket socket = null;
            for (;;) try {
                socket = server.accept(); // Must not use try with resource
                // since we are returning an object that relies on socket
                // still being alive.
                DataInputStream dis =
                    new DataInputStream(socket.getInputStream());
                switch (dis.readByte()) {
                    case GETBUSYNESS:
                        handleBusynessRequest(socket);
                        break;
                    case ANNOUNCESERVER:
                        handleAnnounceServer(socket);
                        break;
                    case COMMUNICATE:
                        return new Communicator(socket);
                }
            } catch (IOException e) {
                try { if (socket != null) socket.close(); }
                catch (IOException e2) {}
                throw e;
            }
        }
    }

}


