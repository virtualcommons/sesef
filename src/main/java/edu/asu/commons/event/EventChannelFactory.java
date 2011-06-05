package edu.asu.commons.event;

/**
 * $Id: EventChannelFactory.java 1 2008-07-23 22:15:18Z alllee $
 *   
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */
public class EventChannelFactory {
    
    public static EventChannel create() {
        return new EventTypeChannel();
    }

}
