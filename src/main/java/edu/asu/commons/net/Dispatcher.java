package edu.asu.commons.net;


import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventHandler;

/**
 * 
 * A dispatcher has client and server capabilities.  Servers use a dispatcher to
 * listen on a specific port, handling incoming connections in addition to the 
 * transmission and receipt of Events across the network.  In other words, a 
 * dispatcher is a network-capable event channel.  
 * <p>
 * The Dispatcher could in theory be an EventHandler/EventGenerator as well but
 * to avoid confusion between local and remote messaging we use a transmit() 
 * method to dispatch outgoing Events across the network and keep a handle on an 
 * explicit EventHandler that receives and dispatches incoming Events 
 * from across the network locally (these locally-bound Events would be read 
 * from the Socket).
 * 
 * @author Allen Lee
 * @version $Revision: 1 $
 */
public interface Dispatcher {

    public void disconnect(Identifier id);

    /**
     * Transmits the given event across the network.  This has different 
     * ramifications depending on whether this dispatcher being used as a 
     * server or as a client.  In the server case, the event's Identifier is
     * used to determine which client to write the given Event to.  If 
     * Identifier.NULL is used, the event is sent to all connected clients.
     * In the client case, the event's Identifier is the client's server-assigned
     * Identifier. 
     */
    public void transmit(Event event);

    /**
     * Shuts the Dispatcher down, disconnecting all open connections.
     */
    public void shutdown();
    
    /**
     * The dispatcher pumps Events that it receives across the network into this
     * local event handler.  
     * @return
     */
    public EventHandler<Event> getLocalEventHandler();
    
}
