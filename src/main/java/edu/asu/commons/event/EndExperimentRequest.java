package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id: EndExperimentRequest.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * Used by the facilitator to signal that we want to reset the experiment to its initial state.
 * 
 * @author Allen Lee
 * @version $Revision: 1 $
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
