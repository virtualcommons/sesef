package edu.asu.commons.event;

/**
 * $Id: EventChannel.java 1 2008-07-23 22:15:18Z alllee $
 *  
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */

public interface EventChannel extends EventHandler<Event> {
    
    public void add(Object owner, EventProcessor<? extends Event> processor);
    public void remove(Object owner);
    public void dispatch(Event event);

}
