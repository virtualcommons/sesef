package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Used by the facilitator to signal that we want to reset the experiment to its initial state.
 * 
 * @author Allen Lee
 * @version $Revision$
 */

public class EndExperimentRequest extends AbstractEvent {

    private static final long serialVersionUID = -5738727407426476168L;

    public EndExperimentRequest(Identifier id) {
        super(id);
    }

    public String toString() {
        return "End experiment initiated by: " + id;
    }

}
