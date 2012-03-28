package edu.asu.commons.event;

/**
 * $Id$
 * 
 * An EventHandler is any object capable of handling Events.
 * 
 * @author alllee
 * @version $Revision$
 */
public interface EventHandler<E extends Event> {

    /**
     * Handles the given Event.
     * 
     * @param event
     */
    public void handle(E event);

    /**
     * Handles the given Event in a new thread of execution.
     * 
     * @param event
     * @param newThread
     */
    public void handleWithNewThread(E event);

}
