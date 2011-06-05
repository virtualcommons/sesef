package edu.asu.commons.event;

/**
 * Examines whether or not the type of the <code>Event</code> parameterized in
 * the constructor is the same as or a superinterface/superclass of the
 * <code>Class</code> of a defendant <code>Event</code>.
 *
 * @author <a href='alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */
public class EventInstanceofConstraint implements EventConstraint {
    /**
     * The targetClass type.
     */
    private final Class<? extends Event> targetClass;
    
    /**
     * Creates a new EventInstanceofConstraint matching all Events that pass the
     * (event instanceof targetClass) check.
     *
     * @param targetClass The type to targetClass.
     * @throws IllegalArgumentException if targetClass is null or targetClass is not an
     * Event class.
     * @see java.lang.Class#isAssignableFrom(Class)
     */
    public EventInstanceofConstraint(Class<? extends Event> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Given a null class");
        }
        this.targetClass = targetClass;
    }

    /**
     * Returns true if the Event is an instance of the targetClass used to 
     * instantiate this constraint.
     *
     * @param candidate An Event to check.
     *
     * @return Whether the given event is of the right type.
     * @see java.lang.Class#isInstance(Object)
     */
    public boolean accepts(Event candidate) {
        return targetClass.isInstance(candidate);
    }
}
