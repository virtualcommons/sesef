package edu.asu.commons.event;

/**
 * $Id: EventHandler.java 304 2009-10-19 21:03:43Z alllee $
 * 
 * An EventHandler is any object capable of handling Events.
 * 
 * @author alllee
 * @version $Revision: 304 $
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
