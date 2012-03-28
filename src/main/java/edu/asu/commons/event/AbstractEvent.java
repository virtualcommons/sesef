package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id: AbstractEvent.java 524 2010-08-06 00:53:30Z alllee $
 * 
 * Abstract base class providing convenience methods for accessing the creation
 * time and Identifier for an Event.
 * 
 * @author <a href='mailto:alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 524 $
 */

public abstract class AbstractEvent implements Event {

    private final static long serialVersionUID = -3443360054002127621L;

    protected Identifier id;
    protected long creationTime;
    protected String message;

    public AbstractEvent() {
        this(Identifier.NULL);
    }

    public AbstractEvent(Identifier id) {
        this(id, null);
    }

    public AbstractEvent(Identifier id, String message) {
        if (id == null) {
            this.id = Identifier.NULL;
        }
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.message = message;
    }

    /**
     * Returns the event's identifier
     */
    public Identifier getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        if (message == null) {
            return String.format("%s - id: %s, created on %d", getClass(), id, creationTime);
            // return getClass() + " id [" + id + "], creation time [ " + creationTime + "]";
        }
        return message;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
