package edu.asu.commons.event;

/**
 * $Id: PersistableEvent.java 1 2008-07-23 22:15:18Z alllee $
 *
 * Marker interface for persistable events in the system, i.e., events that
 * should get recorded by the persister. 
 * 
 * @author <a href='alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */
public interface PersistableEvent extends Event {
    
    public void timestamp();
}
