package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

// import java.time.Clock;
// import java.time.Instant;

/**
 * $Id$
 * 
 * Base class that provides uniqueness with respect to the time-ordered stream
 * of Events originating from a single place.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public abstract class AbstractPersistableEvent extends AbstractEvent implements PersistableEvent, Comparable<AbstractPersistableEvent> {

    private static final long serialVersionUID = -8335415577272927846L;

    private volatile static long classCounter = 0;

    private final long ordinal;

    // private Instant instant = Instant.now(Clock.systemUTC());

    public AbstractPersistableEvent(Identifier id) {
        this(id, null);
    }

    public AbstractPersistableEvent(Identifier id, String message) {
        super(id, message);
        synchronized (AbstractPersistableEvent.class) {
            ordinal = classCounter++;
        }
    }

    /**
     * This method is used to establish a total ordering as per the Comparable
     * interface.
     */
    public int compareTo(AbstractPersistableEvent e) {
        if (e == null) {
            throw new IllegalArgumentException("Cannot compare to a null event");
        }
        // System.currentTimeMillis() is not fine-grained enough to distinguish
        // between two Event creation times occasionally, hence the use of the
        // ordinal to help impose a total ordering across all persistable events.
        int comparison = compare(getCreationTime(), e.getCreationTime());
        if (comparison == 0) {
            return compare(ordinal, e.ordinal);
        }
        return comparison;
    }

    private int compare(long a, long b) {
        return (a > b) ? 1 : (a == b) ? 0 : -1;
    }

    public final void timestamp() {
        if (creationTime == 0L) {
            creationTime = System.currentTimeMillis();
            // instant = Instant.now(Clock.systemUTC());
        }
    }

    /*
    public Instant getInstant() {
        return instant;
    }
    */
}
