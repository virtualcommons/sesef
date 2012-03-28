package edu.asu.commons.event;

import java.io.Serializable;

import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Event marker interface, tags all semantic messages used locally within
 * the event channel or transmit remotely via the Dispatcher.
 * 
 * @author <a href='alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface Event extends Serializable {

    public Identifier getId();

    // FIXME: use TimePoints instead.
    public long getCreationTime();

}
