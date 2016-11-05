package edu.asu.commons.event;

// import java.time.Instant;

/**
 * $Id$
 * 
 * Marker interface for persistable events in the system, i.e., events that
 * should get recorded by the persister.
 * 
 * @author <a href='alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface PersistableEvent extends Event {

    public void timestamp();

  //   public Instant getInstant();
}
