package edu.asu.commons.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * $Id$
 * Provides a generic event channel dispatching events to interested
 * subscribers. Subscribers specify interest in a particular subset of Events
 * by passing in an additional EventConstraint to the subscribe() method.
 * 
 * @author Allen Lee
 * @version $Revision$
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class EventConstraintChannel implements EventGenerator, EventChannel {

    private final static EventConstraintChannel INSTANCE = new EventConstraintChannel();

    private final Map<EventHandler<Event>, EventConstraint> eventListeners =
            new HashMap<EventHandler<Event>, EventConstraint>();

    // Maps a 'module' owner to a list of event processors (which encapsulate both EventHandling
    // and an EventConstraint).
    private final Map<Object, List<EventProcessor>> owners =
            new HashMap<Object, List<EventProcessor>>();

    /**
     * Provides access to a singleton version of the EventChannel.
     * 
     * @return the EventChannel singleton
     */
    public static EventConstraintChannel getInstance() {
        return INSTANCE;
    }

    /**
     * Adds an event to the EventChannel which then propagates the event to
     * all interested subscribers. EventHandler.handle(Event) is invoked in a
     * single separate thread of execution.
     * 
     * @param event
     *            The event to distribute via this EventChannel.
     */
    public void handle(final Event event) {
        synchronized (eventListeners) {
            new Thread() {
                public void run() {
                    for (final Map.Entry<EventHandler<Event>, EventConstraint> entry : eventListeners.entrySet()) {
                        if (entry.getValue().accepts(event)) {
                            entry.getKey().handle(event);
                        }
                    }
                }
            }.start();
        }
    }

    public void handleWithNewThread(final Event event) {
        synchronized (eventListeners) {
            for (final Map.Entry<EventHandler<Event>, EventConstraint> entry : eventListeners.entrySet()) {
                if (entry.getValue().accepts(event)) {
                    new Thread() {
                        public void run() {
                            entry.getKey().handle(event);
                        }
                    }.start();
                }
            }
        }
    }

    public void dispatch(final Event event) {
        handle(event);
    }

    // handles without the thread for better performance and/or sequential processing.
    public void handleAndWait(final Event event) {
        synchronized (eventListeners) {
            for (final Map.Entry<EventHandler<Event>, EventConstraint> entry : eventListeners.entrySet()) {
                if (entry.getValue().accepts(event)) {
                    entry.getKey().handle(event);
                }
            }
        }
    }

    public boolean acceptsSubtypes() {
        return true;
    }

    /**
     * An EventTypeProcessor is a convenience type for subscribing to the pool and
     * provides localized event handling for a given constraint.
     * 
     * @param processor
     */
    public void add(EventProcessor processor) {
        subscribe(processor, processor);
    }

    /**
     * An EventTypeProcessor is a convenience type for subscribing to the pool and
     * provides localized event handling for a given constraint.
     * 
     * @param processor
     */
    public void add(Object owner, EventProcessor processor) {
        getEventProcessorsFor(owner).add(processor);
    }

    private List<EventProcessor> getEventProcessorsFor(Object owner) {
        synchronized (owners) {
            List<EventProcessor> processors = owners.get(owner);
            if (processors == null) {
                processors = new EventProcessorList();
                owners.put(owner, processors);
            }
            return processors;
        }

    }

    public List<EventProcessor> register(Object owner) {
        return getEventProcessorsFor(owner);
    }

    public void unregister(Object owner) {
        List<EventProcessor> processors = null;
        synchronized (owners) {
            processors = owners.remove(owner);
        }
        if (processors != null) {
            for (EventProcessor processor : processors) {
                unsubscribe(processor);
            }
        }
    }

    public void remove(Object owner) {
        unregister(owner);
    }

    /**
     * The given handler will receive all Events received by this EventChannel
     * accept()-ed by the given EventConstraint.
     * 
     * @param handler
     *            The EventHandler to subscribe.
     * @param constraint
     *            The constraint with which to filter events.
     */
    public void subscribe(EventHandler<Event> handler, EventConstraint constraint) {
        if (handler == this) {
            throw new IllegalArgumentException(
                    "Cannot subscribe the event channel to itself.");
        }
        synchronized (eventListeners) {
            eventListeners.put(handler, constraint);
        }
    }

    public void subscribe(EventHandler<Event> handler) {
        subscribe(handler, EventConstraint.NONE);
    }

    /**
     * Unsubscribes an EventHandler from this EventChannel, stopping it from receiving any
     * events from this EventChannel.
     * 
     * @param handler
     *            The EventHandler to unsubscribe.
     */
    public void unsubscribe(EventHandler<Event> handler) {
        synchronized (eventListeners) {
            eventListeners.remove(handler);
        }
    }

    /**
     * 
     * FIXME: this class should support automatic subscription/unsubscription with all methods that
     * modify this List. Right now only basic add/remove methods are supported.
     */
    private class EventProcessorList extends ArrayList<EventProcessor> {
        private static final long serialVersionUID = -4756601346604320046L;

        @Override
        public boolean add(EventProcessor processor) {
            subscribe(processor, processor);
            return super.add(processor);
        }

        @Override
        public void add(int index, EventProcessor processor) {
            subscribe(processor, processor);
            super.add(index, processor);
        }

        @Override
        public boolean addAll(Collection<? extends EventProcessor> c) {
            for (EventProcessor processor : c) {
                subscribe(processor, processor);
            }
            return super.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends EventProcessor> c) {
            for (EventProcessor processor : c) {
                subscribe(processor, processor);
            }
            return super.addAll(index, c);
        }

        @Override
        public EventProcessor remove(int index) {
            EventProcessor removed = super.remove(index);
            unsubscribe(removed);
            return removed;
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof EventProcessor) {
                unsubscribe((EventProcessor) o);
            }
            return super.remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            for (Object o : c) {
                if (o instanceof EventProcessor) {
                    unsubscribe((EventProcessor) o);
                }
            }
            return super.removeAll(c);
        }

    }

    public Class<Event> getEventClass() {
        return Event.class;
    }
}
