

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Protocol implements AutoCloseable {

    private static final byte GETBUSYNESS = 0;
    private static final byte GETLIST = 1;
    private static final byte ANNOUNCESERVER = 2;
    private static final byte COMMUNICATE = 3;
    private Sigar sigar;

    private static Logger logger = LogManager.getLogger("Protocol");

    private void log (String msg) {
        SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSz");
	logger.info(fmt.format(new Date()).toString() + ": " + msg);
        // We can change this later to use log4j.
    }

    private class Host {
        public String hostname;
        public int port;
        public double busyness;
        public long lastpinged;
        public long lastsuccess;
        public boolean equals (Object _other) {
            if (!(_other instanceof Host)) return false;
            Host other = (Host) _other;
            try {
                return InetAddress.getByName(this.hostname).equals(
                    InetAddress.getByName(other.hostname)) &&
                    this.port == other.port;
            } catch (UnknownHostException e) {
                return false;
            }
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
        public String toString () {
            long now = System.currentTimeMillis();
            String h = hostname;
            try { h = InetAddress.getByName(hostname).getCanonicalHostName(); }
            catch (UnknownHostException e) {}
            return "{ host: (" + h +
                ", " + port +
                "), load: " + String.format("%.3f", busyness) +
                ", lastpinged: " + (now - lastpinged) +
                "ms ago, lastresponse: " + (now - lastsuccess) +
                "ms ago }";
        }
    }

    private class ProtocolHelper extends Thread implements AutoCloseable {
        private ArrayList<Host> servers;
        private boolean isserver = false;
        private boolean closed = false;
        private ServerSocket serversocket;
        private int port;
        public ProtocolHelper () {
            servers = new ArrayList<Host>();
            this.start();
        }
        /*
            Host host must already be in ArrayList<Host> servers.
        */
        private boolean ping (Host host) {
            host.lastpinged = System.currentTimeMillis();
            try (
                Socket socket = new Socket();
            ) {
                socket.connect(
                    new InetSocketAddress(host.hostname, host.port), 500);
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
                    synchronized (this) {
                        if (servers.contains(remote)) {
                            // If already have server
                            // update local copy of server information.
                            Host localcopy =
                                servers.get(servers.indexOf(remote));
                            while (servers.contains(localcopy))
                                servers.remove(localcopy);
                            if (localcopy.lastsuccess < remote.lastsuccess) {
                                localcopy.lastsuccess = remote.lastsuccess;
                                localcopy.busyness = remote.busyness;
                            }
                            if (localcopy.lastpinged < remote.lastpinged)
                                localcopy.lastpinged = remote.lastpinged;
                            servers.add(localcopy);
                        } else if (!probablyDead(remote)) // Allow dead servers
                            servers.add(remote); // to disappear.
                    }
                }
                host.lastsuccess = host.lastpinged;
                log("succeeded to ping " + host);
                return true;
            } catch (IOException e) {
                log("failed to ping " + host);
                return false;
            }
        }
        private void announceServer () {
            synchronized (this) {
                Collections.shuffle(servers, new Random()); // Randomize order.
            }
            for (int i = 0; ; ++i) {
                Host server = null;
                synchronized (this) {
                    if (!(i < servers.size())) break;
                    server = servers.get(i);
                }
                try (
                    Socket socket = new Socket();
                ) {
                    socket.connect(
                        new InetSocketAddress(server.hostname, server.port),
                        500);
                    DataOutputStream ds =
                        new DataOutputStream(socket.getOutputStream());
                    ds.writeByte(ANNOUNCESERVER);
                    ds.writeInt(port);
                    ds.writeDouble(getBusyness());
                } catch (IOException e) {
                    continue;
                }
                return;
            }
        }
        private boolean probablyDead (Host host) {
            return host.lastpinged - host.lastsuccess > 10000;
        }
        public void run () {
            try {
                boolean succeeded = true;
                boolean lastsucceeded = false;
                for (;;) {
                    synchronized (this) {
                        wait(1000);
                        if (servers.size() == 0) {
                            log("No servers found yet.");
                            continue;
                        } else {
                            String serverlist = "server list: {\n";
                            for (int i = 0; i < servers.size(); ++i) {
                                serverlist += "    " + servers.get(i) + "\n";
                            }
                            serverlist += "}";
                            log(serverlist);
                        }
                    }
                    if (this.closed)
                    	break;
                    if (this.isserver)
                        announceServer();
                    Host host = getServer();
                    if (host == null) continue;
                    if (ping(host)) {
                        lastsucceeded = true;
                    } else {
                        if (lastsucceeded && // Never remove two in a row in
                            probablyDead(host)) // case we lost our connection.
                            synchronized (this) {
                                servers.remove(host);
                            }
                        lastsucceeded = false;
                    }
                }
            } catch (InterruptedException e) { return; }
        }
        public synchronized void findAll (Host remote) {
            if (servers.contains(remote)) return;
            servers.add(remote);
            notifyAll();
        }
        public void handleBusynessRequest (Socket socket)
            throws IOException {
            DataOutputStream dos =
                new DataOutputStream(socket.getOutputStream());
            DataInputStream dis =
                new DataInputStream(socket.getInputStream());
            dos.writeDouble(getBusyness());
            dis.readByte(); // Ignore this (just GETLIST for time syncing).
            long toffset = System.currentTimeMillis();
            synchronized (this) {
                dos.writeInt(servers.size());
                for (int i = 0; i < servers.size(); ++i)
                    servers.get(i).toDataStream(dos, toffset);
            }
            socket.close();
        }
        public void handleAnnounceServer (Socket socket)
            throws IOException {
            DataInputStream ds = new DataInputStream(socket.getInputStream());
            Host host = new Host();
            host.hostname = socket.getInetAddress().getHostAddress();
            host.port = ds.readInt();
            host.busyness = ds.readDouble();
            host.lastpinged = host.lastsuccess = System.currentTimeMillis();
            synchronized (this) {
                servers.remove(host);
                servers.add(host);
            }
            socket.close();
        }
        public synchronized Host getServer () {
            if (servers.size() == 0) return null;
            double total = 0.0;
            for (int i = 0; i < servers.size(); ++i)
                total += 1 - servers.get(i).busyness;
            double value = total * Math.random();
            total = 0.0;
            for (int i = 0; i < servers.size(); ++i) {
                total += 1 - servers.get(i).busyness;
                if (total > value) return servers.get(i);
            }
            return servers.get(servers.size() - 1); // Should be unreachable.
        }
        public void setupServer (int port) throws IOException {
            this.port = port;
            serversocket = new ServerSocket(port);
            this.isserver = true;
        }
        public Socket accept () throws IOException {
            return serversocket.accept();
        }
        public void close () {
            if (this.isserver)
                try {
                    serversocket.close();
                } catch (IOException e) {}
            closed = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    private double getBusyness () {
        try {
            double[] loads = sigar.getLoadAverage();
            for (int i = 0; i < loads.length; i++) {
                loads[i] = Math.min(loads[i], 1);
            }
            return (2*loads[0] + 3*loads[1] + loads[2])/6;
        } catch (SigarException e) { return 0.5; }
    }

    private ProtocolHelper ph;

    public Protocol () {
        ph = new ProtocolHelper();
        sigar = new Sigar();
    }

    public void addServer (String hostname, int port) {
        Host host = new Host();
        host.hostname = hostname;
        host.port = port;
        host.lastpinged = host.lastsuccess = System.currentTimeMillis();
        host.busyness = 1.0;
        ph.findAll(host);
    }

    public void setupServer (int port) throws IOException {
        ph.setupServer(port);
    }

    public Communicator serveOnce () throws IOException {
        Socket socket = null;
        for (;;) try {
            socket = ph.accept(); // Must not use try-with-resource
            // since we are returning an object that relies on socket
            // still being alive.
            DataInputStream dis =
                new DataInputStream(socket.getInputStream());
            switch (dis.readByte()) {
                case GETBUSYNESS:
                    log("received ping");
                    ph.handleBusynessRequest(socket);
                    break;
                case ANNOUNCESERVER:
                    log("received announcement");
                    ph.handleAnnounceServer(socket);
                    break;
                case COMMUNICATE:
                    log("received connection");
                    return new Communicator(socket);
                default:
                    break;
            }
        } catch (IOException e) {
            try { if (socket != null) socket.close(); }
            catch (IOException e2) {}
            throw e;
        }
    }

    public Communicator communicate () throws IOException {
        Host host = ph.getServer();
        log("communicating with " + host);
        if (host == null) throw new IOException();
        Socket socket = null;
        try {
            socket = new Socket(host.hostname, host.port);
            DataOutputStream ds =
                        new DataOutputStream(socket.getOutputStream());
            ds.writeByte(COMMUNICATE);
            return new Communicator(socket);
        } catch (IOException e) {
            try { if (socket != null) socket.close(); }
            catch (IOException e2) {}
            throw e;
        }
    }

    public void close () {
        ph.close();
    }

}


