package edu.asu.commons.net.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;



/**
 * $Id$ 
 *
 * Signifies that a disconnection should happen for the given identifier.
 * 
 * @author Allen Lee
 * @version $Revision$
 */
public class DisconnectionRequest extends AbstractEvent {
    
    private static final long serialVersionUID = 8135584626891675116L;
    
    private final Throwable exception;       

    public DisconnectionRequest(Identifier id) {
        this(id, null);
    }
    
    public DisconnectionRequest(Identifier id, Throwable exception) {
        super(id);
        this.exception = exception;
    }
    
    public Throwable getException() {
        return exception;
    }
    
    public String toString() {
        return "Disconnecting id " + id + " due to exception: " + exception;
    }
}
