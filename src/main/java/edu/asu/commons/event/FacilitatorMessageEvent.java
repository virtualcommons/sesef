package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Simple String message event used to notify the Facilitator of server-side goings-on.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class FacilitatorMessageEvent extends AbstractEvent {

    private static final long serialVersionUID = 2587410427529176360L;

    public FacilitatorMessageEvent(Identifier id, String message) {
        super(id, message);
    }

}
