package edu.asu.commons.event;

/**
 * $Id$
 *   
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class EventChannelFactory {
    
    public static EventChannel create() {
        return new EventTypeChannel();
    }

}
