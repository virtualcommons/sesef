package edu.asu.commons.net.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;



/**
 * $Id: DisconnectionRequest.java 293 2009-10-10 01:18:43Z alllee $ 
 *
 * Signifies that a disconnection should happen for the given identifier.
 * 
 * @author Allen Lee
 * @version $Revision: 293 $
 */
public class DisconnectionRequest extends AbstractEvent {
    
    private static final long serialVersionUID = 8135584626891675116L;
    
    private final Exception exception;       

    public DisconnectionRequest(Identifier id) {
        this(id, null);
    }
    
    public DisconnectionRequest(Identifier id, Exception exception) {
        super(id);
        this.exception = exception;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public String toString() {
        return "Disconnecting id " + id + " due to exception: " + exception;
    }
}
