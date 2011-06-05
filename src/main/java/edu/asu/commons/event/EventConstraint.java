package edu.asu.commons.event;
/**
 * $Id: EventConstraint.java 1 2008-07-23 22:15:18Z alllee $
 *
 * Constraints define a set of events.  For any given event, a constraint's
 * accepts() method should return true if that event is a member of its set,
 * and false otherwise.
 *
 * @author alllee
 * @version $Revision: 1 $
 */
public interface EventConstraint {
    /**
     * Matches all events.
     */
    public static final EventConstraint NONE = new EventConstraint() {
        public boolean accepts(Event defendant) {
            return true;
        }
    };
    /**
     * Returns true if the given event fits the set of events defined by
     * this constraint.
     */
    public boolean accepts(Event event);
}
