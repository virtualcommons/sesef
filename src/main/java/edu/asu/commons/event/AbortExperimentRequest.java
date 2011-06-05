package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;


/**
 * $Id: AbortExperimentRequest.java 1 2008-07-23 22:15:18Z alllee $
 *
 * Request used to signal the server that the game should be aborted.  Should 
 * employ some intelligent something or other on the server
 * side to prevent evul h4x0rs from aborting the game over and over.
 *
 * @author Allen Lee
 * @version $Revision: 1 $
 */

public class AbortExperimentRequest extends AbstractEvent {

    private static final long serialVersionUID = -7113964603025019120L;

    public AbortExperimentRequest(Identifier id) {
        super(id);
    }

    public String toString() {
        return "Abort experiment initiated by: " + id;
    }

}
