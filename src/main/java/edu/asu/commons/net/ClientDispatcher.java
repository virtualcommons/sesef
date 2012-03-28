package edu.asu.commons.net;

import java.net.InetSocketAddress;

/**
 * $Id$
 * 
 * Client dispatchers provide the ability to connect to a given
 * InetSocketAddress.
 * 
 * 
 * @author Allen Lee
 * @version $Revision$
 */
public interface ClientDispatcher extends Dispatcher {

    /**
     * Attempts to connect to the specified host and port. If successful,
     * returns the server assigned Identifier that should be used to prefix
     * every transmitted Event.
     */
    public Identifier connect(InetSocketAddress address);

}
