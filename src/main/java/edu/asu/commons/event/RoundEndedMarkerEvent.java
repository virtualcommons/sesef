package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Used to mark the end of a round in the save files.
 * 
 * @see RoundStartedMarkerEvent
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 524 $
 */
public class RoundEndedMarkerEvent extends AbstractPersistableEvent {

    private static final long serialVersionUID = -3877468632540175512L;

    public RoundEndedMarkerEvent() {
        super(Identifier.NULL);
    }

}
