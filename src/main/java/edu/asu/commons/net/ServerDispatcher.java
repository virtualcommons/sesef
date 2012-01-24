package edu.asu.commons.net;


/**
 * $Id$
 * 
 * A server dispatcher can listen for incoming requests on a particular port.
 * 
 * 
 * @author Allen Lee 
 * @version $Revision$
 */
public interface ServerDispatcher extends Dispatcher {
    
    enum Type {
        NIO, SOCKET, NETTY_NIO;
        public static Type fromString(String name) {
            try {
                return Type.valueOf(name);
            }
            catch (Exception exception) {
                return SOCKET;
            }
        }
    }

    /**
     * Attempts to determine if the connection identified by id
     * is 'connected'. This can mean different things depending
     * on the underlying mechanism used to connect.
     */
    public boolean isConnected(Identifier id);
    
    /**
     * Tells the Dispatcher to begin listening on the given port and spawns a 
     * new (single) thread of execution to handle incoming requests.
     */
    public void listen(int port);
}
