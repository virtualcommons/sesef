package edu.asu.commons.event;

/**
 * FIXME: refactor to use Java 8 functional constructs 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public interface EventChannel extends EventHandler<Event> {

    public void add(Object owner, EventProcessor<? extends Event> processor);
    
    public void remove(Object owner);

    public void dispatch(Event event);

}
