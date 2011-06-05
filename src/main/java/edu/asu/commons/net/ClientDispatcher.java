package edu.asu.commons.net;

import java.net.InetSocketAddress;

/**
 * $Id: ClientDispatcher.java 1 2008-07-23 22:15:18Z alllee $
 *
 * Client dispatchers provide the ability to connect to a given 
 * InetSocketAddress.
 * 
 * 
 * @author Allen Lee 
 * @version $Revision: 1 $
 */
public interface ClientDispatcher extends Dispatcher {

    /**
     * Attempts to connect to the specified host and port.  If successful, 
     * returns the server assigned Identifier that should be used to prefix
     * every transmitted Event.
     */
    public Identifier connect(InetSocketAddress address); 


}
