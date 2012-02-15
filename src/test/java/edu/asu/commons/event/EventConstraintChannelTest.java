package edu.asu.commons.event;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class EventConstraintChannelTest {
    
    private EventConstraintChannel eventChannelSingleton;
    private int numberOfEvents = 50;
    private List<Event> events;
    
    @Before
    public void setUp() {
        eventChannelSingleton = EventConstraintChannel.getInstance();
        events = new ArrayList<Event>();
        for (int i = 0; i < numberOfEvents; i++) {
            events.add(new MockEvent());
        }
    }
    
    
    @Test
    public void testSingleton() {
        eventChannelSingleton = EventConstraintChannel.getInstance();
        for (int i = 0; i < 16; i++) {
            assertEquals(EventConstraintChannel.getInstance(), eventChannelSingleton);
        }
    }
    
    @Test
    public void testSubscription() {
        final List<Event> handledEvents = new ArrayList<Event>();
        eventChannelSingleton.add(new EventTypeProcessor<Event>(Event.class) {
            public void handle(Event event) {
                handledEvents.add(event);
            }
        });
        for (Event event: events) {
            eventChannelSingleton.handleAndWait(event);
        }
        assertTrue(handledEvents.containsAll(events));

    }
    
    private static class MockEvent extends AbstractEvent {

        private static final long serialVersionUID = -625434701751262383L;
        
    }

}
