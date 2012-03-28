package edu.asu.commons.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class EventTypeChannelTest {

    private EventTypeChannel eventTypeChannel;
    private int numberOfEvents = 50;
    private List<Event> events;

    private Object owner;

    @Before
    public void setUp() {
        owner = new Object();
        eventTypeChannel = new EventTypeChannel();
        events = new ArrayList<Event>();
        for (int i = 0; i < numberOfEvents; i++) {
            events.add(new MockEvent());
        }
    }

    @Test
    public void testSingleton() {
        for (int i = 0; i < 42; i++) {
            assertEquals(EventTypeChannel.getInstance(), EventTypeChannel.getInstance());
        }
    }

    @Test
    public void testMultipleSubscription() {
        final List<Event> firstListenerHandledEvents = new ArrayList<Event>();
        final List<Event> secondListenerHandledEvents = new ArrayList<Event>();
        eventTypeChannel.add(new EventTypeProcessor<MockEvent>(MockEvent.class) {
            public void handle(MockEvent event) {
                firstListenerHandledEvents.add(event);
            }
        });
        eventTypeChannel.add(new EventTypeProcessor<MockEvent>(MockEvent.class) {
            public void handle(MockEvent event) {
                secondListenerHandledEvents.add(event);
            }
        });
        eventTypeChannel.add(new EventTypeProcessor<PersistableEvent>(PersistableEvent.class) {
            public void handle(PersistableEvent event) {
                Assert.fail("Should not have handled a persistable event");
            }
        });
        for (Event event : events) {
            eventTypeChannel.handle(event);
        }
        assertEquals(firstListenerHandledEvents, secondListenerHandledEvents);
        assertTrue(firstListenerHandledEvents.containsAll(secondListenerHandledEvents));
        assertTrue(firstListenerHandledEvents.containsAll(events));
        assertTrue(secondListenerHandledEvents.containsAll(events));
    }

    @Test
    public void testUnsubscription() {
        final List<Event> handledEvents = new ArrayList<Event>();
        eventTypeChannel.add(this, new EventTypeProcessor<MockEvent>(MockEvent.class) {
            public void handle(MockEvent event) {
                handledEvents.add(event);
            }
        });
        for (Event event : events) {
            eventTypeChannel.handle(event);
        }
        assertTrue(handledEvents.containsAll(events));
        assertEquals(handledEvents, events);
        eventTypeChannel.remove(this);
        handledEvents.clear();
        assertTrue(handledEvents.isEmpty());
        for (Event event : events) {
            eventTypeChannel.handle(event);
        }
        assertTrue(handledEvents.isEmpty());
        assertEquals(eventTypeChannel.getNumberOfOwners(), 0);
        assertEquals(eventTypeChannel.getNumberOfRegisteredProcessors(), 0);
    }

    private Thread createEventHandlingThread() {
        return new Thread() {
            public void run() {
                eventTypeChannel.add(owner, new EventTypeProcessor<Event>(Event.class, true) {
                    public void handle(Event event) {
                        assertTrue(events.contains(event));
                    }
                });
            }
        };
    }

    private Thread createEventGeneratingThread() {
        return new Thread() {
            public void run() {
                generateEvents();
            }
        };
    }

    private void generateEvents() {
        for (Event event : events) {
            eventTypeChannel.handle(event);
        }
    }

    @Test
    public void testSynchronization() throws InterruptedException {
        Thread finalThread = createEventHandlingThread();
        Thread startThread = createEventHandlingThread();
        startThread.start();
        try {
            startThread.join();
        } catch (InterruptedException e) {
        }
        for (int i = 0; i < 100; i++) {
            new Thread() {
                public void run() {
                    eventTypeChannel.remove(owner);
                }
            }.start();
            createEventHandlingThread().start();
            createEventGeneratingThread().start();
        }
        finalThread.start();
        createEventGeneratingThread().start();
        // generateEvents();
        try {
            finalThread.join();
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void testSequentialSubscription() {
        final List<Event> handledEvents = new ArrayList<Event>();
        eventTypeChannel.add(new EventTypeProcessor<MockEvent>(MockEvent.class) {
            public void handle(MockEvent event) {
                handledEvents.add(event);
            }
        });
        for (Event event : events) {
            eventTypeChannel.handle(event);
        }
        assertTrue(handledEvents.containsAll(events));

    }

    @Test
    public void testThreadedSubscription() {
        final List<Event> handledEvents = new ArrayList<Event>();
        EventTypeChannel channel = new EventTypeChannel(true);
        channel.add(new EventTypeProcessor<MockEvent>(MockEvent.class) {
            public void handle(MockEvent event) {
                handledEvents.add(event);
            }
        });
        for (Event event : events) {
            channel.handle(event);
        }

        // this won't quite work.. have to wait until the thread completes...
        // assertTrue(handledEvents.containsAll(events));
    }

    @Test
    public void testAcceptsSubtypes() {
        final List<Event> handledEvents = new ArrayList<Event>();
        eventTypeChannel.add(new EventTypeProcessor<Event>(Event.class, true) {
            public void handle(Event event) {
                handledEvents.add(event);
            }
        });
        generateEvents();
        assertTrue(handledEvents.containsAll(events));
    }

    private static class MockEvent extends AbstractEvent {

        private static final long serialVersionUID = -625434701751262383L;

    }

}
