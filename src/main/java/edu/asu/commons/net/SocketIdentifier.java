package edu.asu.commons.net;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * $Id$
 * 
 * Uniquely identifies a socket connection and provides translation methods to convert 
 * IP addresses of a certain format to "station numbers".  In order of preference:  
 * 
 * <ol>
 * <li>ostrom-lab-12.dhcp.asu.edu will have a station number of 12 and emit a toString() of "Station 12"
 * <li>if the hostname doesn't have any trailing numbers, it will try to use the last digits of the IP address to define the station number
 * </ol>
 *  
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class SocketIdentifier extends Identifier.Base<SocketIdentifier> {

    private final InetSocketAddress localSocketAddress;
    private final InetSocketAddress remoteSocketAddress;

    // FIXME: refactor, is this necessary if we're also maintaining the station number?  logic is convoluted.
    private boolean stationed;

    private Integer stationNumber;

    private String remoteHostName;
    
    private static final long serialVersionUID = 2371746759512286392L;

    public SocketIdentifier(Socket socket) {
        this(socket.getLocalAddress().getHostName(), socket.getLocalPort(),
                getRemoteHostName(socket), socket.getPort());
    }
    
    public SocketIdentifier(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        this.localSocketAddress = localAddress;
        this.remoteSocketAddress = remoteAddress;

    }

    public SocketIdentifier(String localHost, int localPort, String remoteHost, int remotePort) {
        this(new InetSocketAddress(localHost, localPort), new InetSocketAddress(remoteHost, remotePort));
    }

    private static String getRemoteHostName(Socket socket) {
        if (socket.getInetAddress() == null) {
            throw new IllegalArgumentException("socket not bound to a remote address");
        }
        return socket.getInetAddress().getHostName();
    }

    public InetSocketAddress getLocalAddress() {
        return localSocketAddress;
    }

    public String getLocalHostAddress() {
        return localSocketAddress.getAddress().getHostAddress();
    }

    public String getLocalHostName() {
        return getLocalAddress().getHostName();
    }

    public int getLocalPort() {
        return getLocalAddress().getPort();
    }

    public String getRemoteHostName() {
        return getRemoteAddress().getHostName();
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteSocketAddress;
    }

    public String getRemoteHostAddress() {
        return remoteSocketAddress.getAddress().getHostAddress();
    }

    public int getRemotePort() {
        return getRemoteAddress().getPort();
    }

    public boolean equals(Object id) {
        return (id instanceof SocketIdentifier)
            && equals((SocketIdentifier) id);
    }


    public boolean equals(SocketIdentifier id) {
        return (id == this) 
            || (id != null && localSocketAddress.equals(id.localSocketAddress) && remoteSocketAddress.equals(id.remoteSocketAddress));
    }

    public int hashCode() {
        return localSocketAddress.hashCode() ^ remoteSocketAddress.hashCode();
    }

    /**
     * A SocketIdentifier is semantically equal to another SocketIdentifier if
     * the localAddress and remoteAddresses are the same.  
     * XXX: no longer attempt to maintain symmetrical equality between SocketIdentifiers
     * generated on both endpoints (which would have the local socket address and remote socket
     * addresses switched) due to the inherent difficulties of dealing with home routers and
     * the like that utilize Network Address Translation to assign and route traffic to 
     * a series of internally distributed IP addresses of the form 10.0.1.x.  Instead, 
     * SocketIdentifiers are only generated on the server side and assigned to the client.   
     */

    public String toString() {
        int stationNumberInt = getStationNumber();
        return (stationed) ? String.format("Station %d (uid: %d)", stationNumberInt, index()) : remoteSocketAddress.toString();
    }
    
    /**
     * FIXME: this method only works for hostnames whose last two characters are 
     * numbers.  Furthermore, for clients originating from the same machine (or behind NAT) it 
     * will return the same client station number.
     * @return
     */
    public int getStationNumber() {
        if (stationNumber != null) {
            return stationNumber.intValue();
        }
        String remoteHostname = remoteSocketAddress.getHostName();
        StringTokenizer tokenizer = new StringTokenizer(remoteHostname, ".");
        String hostname = tokenizer.nextToken();

        System.err.println("remote host name: "+  getRemoteHostName());
        System.err.println("remote socket address: " + remoteSocketAddress);
        System.err.println("remote host address: " + getRemoteHostAddress());
        System.err.println("local host name: " + getLocalHostName());
        System.err.println("local host address: " + localSocketAddress);

        int startIndex = hostname.lastIndexOf('-');
        String station = hostname.substring(startIndex + 1, hostname.length());
        try {
            stationNumber = Math.abs(Integer.parseInt(station));
            stationed = true;
            return stationNumber;
        }
        catch (NumberFormatException e) {
            // try get station from ip 
            return getStationFromIP();
        }
    }

    private int getStationFromIP() {
        if (stationNumber != null) {
            return stationNumber.intValue();
        }
        String remoteHost = remoteSocketAddress.toString();
        System.err.println("remote host name: "+  getRemoteHostName());
        System.err.println("remote socket address: " + remoteSocketAddress);
        System.err.println("local host name: " + getLocalHostName());
        System.err.println("local host address: " + localSocketAddress);
        String lastIPDigits = remoteHost.substring(remoteHost.lastIndexOf('.') + 1, remoteHost.lastIndexOf(':'));
        try {
            stationNumber = Math.abs(Integer.parseInt(lastIPDigits));
            stationed = true;
            return stationNumber;
        }
        catch (NumberFormatException e) {
            stationed = false;
            return index();
        }
    }
    
    public String getStationId() {
        return "Station " + stationNumber;
    }

    public void setStationNumber(Integer stationNumber) {
        if (stationNumber != null) {
            this.stationNumber = stationNumber;
            stationed = true;
        }
    }

    public int compareTo(SocketIdentifier socketId) {
        int comparison = 0;
        if (stationNumber != null && socketId.stationNumber != null) {
            comparison = stationNumber.compareTo(socketId.stationNumber);
        }
        if (comparison == 0) {
            // if these two socket ids are .equals, comparison of 0 is fine.  Otherwise go by the id.
            if (! equals(socketId)) {
                comparison = Integer.valueOf(index()).compareTo(Integer.valueOf(socketId.index())); 
            }
        }
        return comparison;
    }
}
