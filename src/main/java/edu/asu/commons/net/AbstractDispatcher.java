package edu.asu.commons.net;

import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventHandler;
import edu.asu.commons.net.event.DisconnectionRequest;




/**
 * $Id: AbstractDispatcher.java 1 2008-07-23 22:15:18Z alllee $
 *
 * Abstract base class for Dispatchers.
 * 
 * @author Allen Lee
 * @version $Revision: 1 $
 */

public abstract class AbstractDispatcher implements Dispatcher {
    
    private final EventChannel channel;
    
    public AbstractDispatcher(EventChannel channel) {
        if (channel == null) {
            throw new IllegalArgumentException(
                    "Dispatcher requires a non-null EventChannel");
        }
        this.channel = channel;
    }
    
    public EventHandler<Event> getLocalEventHandler() {
        return channel;
    }
    
    public EventChannel getLocalEventChannel() {
        return channel;
    }
    
    protected void requestDisconnection(Identifier id, Exception cause) {
        getLocalEventHandler().handle(new DisconnectionRequest(id, cause));
    }

}
