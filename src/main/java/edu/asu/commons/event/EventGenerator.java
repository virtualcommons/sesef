package edu.asu.commons.event;

/**
 * $Id$
 * 
 * Marks all classes capable of generating Events and dispatching them to
 * interested subscribers.
 * 
 * @author alllee
 * @version $Revision$
 */
public interface EventGenerator {
    /**
     * Subscribes the given EventHandler to this EventGenerator, signifying
     * that all events generated from this EventGenerator should be conveyed to
     * the given EventHandler.
     */
    public void subscribe(EventHandler<Event> handler);

    /**
     * Subscribes the given EventHandler to this EventGenerator, signifying
     * that all events generated from this EventGenerator satisfying the
     * given EventConstraint should be conveyed to the given EventHandler.
     */
    public void subscribe(EventHandler<Event> handler, EventConstraint constraint);

    /**
     * Unsubscribes the given EventHandler from this EventGenerator.
     */
    public void unsubscribe(EventHandler<Event> handler);
}
