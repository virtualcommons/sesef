package edu.asu.commons.event;

import java.io.Serializable;

import edu.asu.commons.net.Identifier;

/**
 * $Id: Event.java 1 2008-07-23 22:15:18Z alllee $
 *
 * Event marker interface, tags all semantic messages used locally within
 * the event channel or transmit remotely via the Dispatcher.
 *
 * @author <a href='alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */
public interface Event extends Serializable {

    public Identifier getId();
    // FIXME: use TimePoints instead.
    public long getCreationTime();

}
