package edu.asu.commons.net.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id: ConnectionEvent.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * Used to signify that a successful connection was made.
 * 
 * @author Allen Lee
 * @version $Revision: 1 $
 */

public class ConnectionEvent extends AbstractEvent {

    private static final long serialVersionUID = -7374984013354707218L;

    public ConnectionEvent(Identifier id) {
        super(id);
    }
}
