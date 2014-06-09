package edu.asu.commons.event;

/**
 * $Id$
 * 
 * This class composes two event constraints using the logical AND operator.
 * It accepts all Events satisfying both the first event constraint and the
 * second, in that order and is short-circuiting.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public class AndEventConstraint implements EventConstraint {

    // private final List<EventConstraint> constraints;

    private final EventConstraint first;
    private final EventConstraint second;

    public AndEventConstraint(EventConstraint first, EventConstraint second) {
        this.first = first;
        this.second = second;
    }

    public boolean accepts(Event event) {
        return first.accepts(event) && second.accepts(event);
    }

}
