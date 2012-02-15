package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Marker interface for events that can be cloned with a different Identifier.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 * @param <T>
 */
public interface ClonableEvent<T extends ClonableEvent<T>> extends Event {
    public T clone(Identifier id);
}
