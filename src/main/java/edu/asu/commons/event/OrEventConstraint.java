package edu.asu.commons.event;

import java.util.Arrays;
import java.util.List;

/**
 * $Id: OrEventConstraint.java 1 2008-07-23 22:15:18Z alllee $
 *
 * This class composes two event constraints using the logical OR operator.
 * It accepts all Events satisfying either the first event constraint or the
 * second, in that order and is short-circuiting.
 *
 * @author <a href='mailto:alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */

public class OrEventConstraint implements EventConstraint {

    private final List<EventConstraint> constraints;

    public OrEventConstraint(EventConstraint first, EventConstraint second) {
        this(Arrays.asList(new EventConstraint[] { first, second }));
    }

    public OrEventConstraint(List<EventConstraint> constraints) {
        if (constraints == null) {
            throw new IllegalArgumentException("constraints shouldn't be null");
        }
        this.constraints = constraints;
    }

    public boolean accepts(Event event) {
        for (EventConstraint constraint : constraints) {
            if (constraint.accepts(event)) {
                return true;
            }            
        }
        return false;
    }
}
