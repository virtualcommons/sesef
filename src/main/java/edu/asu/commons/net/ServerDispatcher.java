package edu.asu.commons.net;


/**
 * $Id: ServerDispatcher.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * A server dispatcher can listen for incoming requests on a particular port.
 * 
 * 
 * @author Allen Lee 
 * @version $Revision: 1 $
 */
public interface ServerDispatcher extends Dispatcher {

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
