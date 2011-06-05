package edu.asu.commons.event;


/**
 * $Id: AndEventConstraint.java 1 2008-07-23 22:15:18Z alllee $
 *
 * This class composes two event constraints using the logical AND operator.
 * It accepts all Events satisfying both the first event constraint and the
 * second, in that order and is short-circuiting.
 *
 * @author <a href='mailto:alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */

public class AndEventConstraint implements EventConstraint {

//    private final List<EventConstraint> constraints;
    
    private final EventConstraint first;
    private final EventConstraint second;

    public AndEventConstraint(EventConstraint first, EventConstraint second) {
        this.first = first;
        this.second = second;
    }
    
    public boolean accepts(Event event) {
        return first.accepts(event) && second.accepts(event);
    }

//    public AndEventConstraint(List<EventConstraint> constraints) {
//        if (constraints == null) {
//            throw new IllegalArgumentException("constraints shouldn't be null");
//        }
//        this.constraints = constraints;
//    }
//
//    public boolean accepts(Event event) {
//        for (EventConstraint constraint : constraints) {
//            if (! constraint.accepts(event)) {
//                return false;
//            }
//        }
//        return true;
//    }
}
