package edu.asu.commons.net.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Signifies that a disconnection happened for the given id.
 * 
 * 
 * @author Allen Lee
 * @version $Revision$
 */
public class DisconnectionEvent extends AbstractEvent {

    private static final long serialVersionUID = 1116488025527784177L;

    public DisconnectionEvent(Identifier id) {
        super(id);
    }
}
