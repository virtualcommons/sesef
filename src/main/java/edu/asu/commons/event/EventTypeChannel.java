package edu.asu.commons.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * $Id: EventTypeChannel.java 454 2010-02-04 04:17:29Z alllee $
 * <p>
 * Provides an event channel that only handles subscription via event type.  
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 454 $
 */
@SuppressWarnings(value={"unchecked", "rawtypes"})
public class EventTypeChannel implements EventChannel {

    private final Map<Class, List<EventProcessor>> equalTypesEventProcessorMap = 
        new HashMap< Class, List<EventProcessor>>();

    private final List<EventProcessor> acceptsSubtypesEventProcessors =
        new LinkedList<EventProcessor>();

    private final Map<Object, List<EventProcessor>> owners = 
        new HashMap<Object, List<EventProcessor>>();

    private final EventDispatcher defaultDispatcher;
    
    private final ThreadedDispatcher threadedDispatcher = new ThreadedDispatcher();
    private final SequentialDispatcher sequentialDispatcher = new SequentialDispatcher();

    public final static EventTypeChannel INSTANCE = new EventTypeChannel();

    public EventTypeChannel() {
        this(false);
    }

    public static EventTypeChannel getInstance() {
        return INSTANCE;
    }

    public EventTypeChannel(boolean shouldThread) {
        if (shouldThread) {
            defaultDispatcher = threadedDispatcher;
        }
        else {
            defaultDispatcher = sequentialDispatcher;
        }
    }
    
    public <E extends Event> void add(EventProcessor<E> eventProcessor) {
        if (eventProcessor.acceptsSubtypes()) {
            synchronized (acceptsSubtypesEventProcessors) {
                acceptsSubtypesEventProcessors.add(eventProcessor);
            }
            return;
        }
        Class<E> eventClass = eventProcessor.getEventClass();
        synchronized (equalTypesEventProcessorMap) {
            List<EventProcessor> handlers = equalTypesEventProcessorMap.get(eventClass);
            if (handlers == null) {
                handlers = new ArrayList<EventProcessor>();
                equalTypesEventProcessorMap.put(eventClass, handlers);
            }
            handlers.add(eventProcessor);
        }
    }

    public void add(Object owner, EventProcessor<? extends Event> eventProcessor) {
        add(eventProcessor);
        synchronized (owners) {
            List<EventProcessor> processors = owners.get(owner);
            if (processors == null) {
                processors = new ArrayList<EventProcessor>();
                owners.put(owner, processors);
            }
            processors.add(eventProcessor);
        }
    }

    public <E extends Event> void subscribe(EventProcessor<E> handler) {
        add(handler);
    }
    
    public <E extends Event> boolean unsubscribe(EventProcessor<E> handler) {
        return remove(handler);
    }

    public <E extends Event> boolean remove(EventProcessor<E>  handler) {
        synchronized (acceptsSubtypesEventProcessors) {
            if (acceptsSubtypesEventProcessors.contains(handler)) {
                return acceptsSubtypesEventProcessors.remove(handler);
            }
        }
        synchronized (equalTypesEventProcessorMap) {
            if (equalTypesEventProcessorMap.containsKey(handler.getEventClass())) {
                return equalTypesEventProcessorMap.get(handler.getEventClass()).remove(handler);            
            }
        }
        return false;
    }

    public void remove(Object owner) {
        synchronized (owners) {
            if (owners.containsKey(owner)) {
                List<EventProcessor> processors = owners.get(owner);
                for (EventProcessor processor: processors) {
                    remove(processor);
                }
                owners.remove(owner);
            }
        }
    }

    int getNumberOfOwners() {
        return owners.size();
    }

    int getNumberOfRegisteredProcessors() {
        int numberOfProcessors = 0;
        for (List<EventProcessor> processors: equalTypesEventProcessorMap.values()) {
            numberOfProcessors += processors.size();
        }
        numberOfProcessors += acceptsSubtypesEventProcessors.size();
        return numberOfProcessors;
    }

    public void handle(Event event) {
        if (event == null) return;
        defaultDispatcher.dispatch(event);
    }

    public void handleWithNewThread(Event event) {
        if (event == null) return;
        threadedDispatcher.dispatch(event);
    }

    public void dispatch(Event event) {
        defaultDispatcher.dispatch(event);
    }

    private interface EventDispatcher {
        public void dispatch(Event event);
    }

    private class SequentialDispatcher implements EventDispatcher {
        public void dispatch(Event event) {
            final Class<? extends Event> eventClass = event.getClass();
            // first check handlers that want this and only this event type.
            synchronized (equalTypesEventProcessorMap) {
                List<EventProcessor> handlers = equalTypesEventProcessorMap.get(eventClass);
                if (handlers != null) {
                    for (final EventProcessor<Event> handler: handlers) {
                        handler.handle(event);
                    }
                }
            }
            // next, check to see if this event should be processed by the subtype processors.
            synchronized (acceptsSubtypesEventProcessors) {
                for (final EventProcessor<Event> handler: acceptsSubtypesEventProcessors) {
                    if (handler.getEventClass().isInstance(event)) {
                        handler.handle(event);
                    }
                }
            }
        }
    }

    private class ThreadedDispatcher implements EventDispatcher {
        public void dispatch(final Event event) {
            final Class<? extends Event> eventClass = event.getClass();
            // first check handlers that want this and only this event type.
            synchronized (equalTypesEventProcessorMap) {
                List<EventProcessor> handlers = equalTypesEventProcessorMap.get(eventClass);
                if (handlers != null) {
                    for (final EventProcessor<Event> handler: handlers) {
                        new Thread() {
                            public void run() {
                                handler.handle(event);                                
                            }
                        }.start();
                    }
                }
            }
            // next, check to see if this event should be processed by the subtype processors.
            synchronized (acceptsSubtypesEventProcessors) {
                for (final EventProcessor<Event> handler: acceptsSubtypesEventProcessors) {
                    if (handler.getEventClass().isInstance(event)) {
                        new Thread() {
                            public void run() {
                                handler.handle(event);                                
                            }
                        }.start();
                    }
                }
            }

        }
    }
}
