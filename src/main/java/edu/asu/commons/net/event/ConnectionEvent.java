package edu.asu.commons.net.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Used to signify that a successful connection was made.
 * 
 * @author Allen Lee
 * @version $Revision$
 */

public class ConnectionEvent extends AbstractEvent {

    private static final long serialVersionUID = -7374984013354707218L;

    public ConnectionEvent(Identifier id) {
        super(id);
    }
}
