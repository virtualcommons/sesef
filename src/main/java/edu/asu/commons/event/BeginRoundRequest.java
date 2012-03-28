package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id: BeginRoundRequest.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * Used by the facilitator to signal that we are done collecting clients and
 * ready to start the next experiment.
 * 
 * 
 * @author Allen Lee
 * @version $Revision: 1 $
 */

public class BeginRoundRequest extends AbstractEvent {

    private static final long serialVersionUID = -1853850747915213143L;

    public BeginRoundRequest(Identifier id) {
        super(id);
    }

    public String toString() {
        return "Begin experiment initiated by: " + id;
    }

}
