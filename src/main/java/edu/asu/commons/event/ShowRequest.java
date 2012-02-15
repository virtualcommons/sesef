package edu.asu.commons.event;


/**
 * $Id$
 * 
 * Marker interface for facilitator requests that should just pass through directly to the client.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public interface ShowRequest<T extends ShowRequest<T>> extends FacilitatorRequest, ClonableEvent<T> {
}
