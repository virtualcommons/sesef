package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id: RoundStartedMarkerEvent.java 462 2010-02-05 04:50:21Z alllee $
 * 
 * Used to mark the start of a round in the save files.
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 462 $
 */
public class RoundStartedMarkerEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = -8385615717108917487L;
    
    public RoundStartedMarkerEvent() {
        super(Identifier.NULL);
    }

}
