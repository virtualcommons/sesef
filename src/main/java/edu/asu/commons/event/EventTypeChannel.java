package edu.asu.commons.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides an event channel that only handles subscription via event type.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class EventTypeChannel implements EventChannel {

    private final Map<Class, List<EventProcessor>> equalTypesEventProcessorMap = new HashMap<Class, List<EventProcessor>>();
    private final List<EventProcessor> acceptsSubtypesEventProcessors = new LinkedList<EventProcessor>();

    // ReadWriteLocks for the two collections above, so we can allow any number
    // of concurrent reads, but give exclusive access to the thread that has
    // obtained the write lock.
    private final ReentrantReadWriteLock equalTypesEventProcessorMapLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock acceptsSubtypesEventProcessorsLock = new ReentrantReadWriteLock();

    private final Map<Object, List<EventProcessor>> owners = new HashMap<Object, List<EventProcessor>>();

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
        } else {
            defaultDispatcher = sequentialDispatcher;
        }
    }

    public <E extends Event> void add(EventProcessor<E> eventProcessor) {
        Lock lock;

        if (eventProcessor.acceptsSubtypes()) {
            lock = acceptsSubtypesEventProcessorsLock.writeLock();
            lock.lock();
            try {
                acceptsSubtypesEventProcessors.add(eventProcessor);
            } finally {
                lock.unlock();
            }
            return;
        }

        Class<E> eventClass = eventProcessor.getEventClass();

        lock = equalTypesEventProcessorMapLock.writeLock();
        lock.lock();
        try {
            List<EventProcessor> handlers = equalTypesEventProcessorMap.get(eventClass);
            if (handlers == null) {
                handlers = new ArrayList<EventProcessor>();
                equalTypesEventProcessorMap.put(eventClass, handlers);
            }
            handlers.add(eventProcessor);
        } finally {
            lock.unlock();
        }
    }

    public void add(Object owner, EventProcessor<? extends Event> eventProcessor) {
        add(eventProcessor);
        synchronized (owners) {
            List<EventProcessor> processors = owners.get(owner);
            if (processors == null) {
                processors = new ArrayList<>();
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

    public <E extends Event> boolean remove(EventProcessor<E> handler) {
        Lock lock = acceptsSubtypesEventProcessorsLock.writeLock();
        lock.lock();
        try {
            if (acceptsSubtypesEventProcessors.contains(handler)) {
                return acceptsSubtypesEventProcessors.remove(handler);
            }
        } finally {
            lock.unlock();
        }

        lock = equalTypesEventProcessorMapLock.writeLock();
        lock.lock();
        try {
            if (equalTypesEventProcessorMap.containsKey(handler.getEventClass())) {
                return equalTypesEventProcessorMap.get(handler.getEventClass()).remove(handler);
            }
        } finally {
            lock.unlock();
        }

        return false;
    }

    public void remove(Object owner) {
        synchronized (owners) {
            if (owners.containsKey(owner)) {
                List<EventProcessor> processors = owners.get(owner);
                for (EventProcessor processor : processors) {
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
        for (List<EventProcessor> processors : equalTypesEventProcessorMap.values()) {
            numberOfProcessors += processors.size();
        }
        numberOfProcessors += acceptsSubtypesEventProcessors.size();
        return numberOfProcessors;
    }

    public void handle(Event event) {
        if (event == null)
            return;
        defaultDispatcher.dispatch(event);
    }

    public void handleWithNewThread(Event event) {
        if (event == null)
            return;
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
            Lock lock;

            // first check handlers that want this and only this event type.
            lock = equalTypesEventProcessorMapLock.readLock();
            lock.lock();
            try {
                List<EventProcessor> handlers = equalTypesEventProcessorMap.get(eventClass);
                if (handlers != null) {
                    for (final EventProcessor<Event> handler : handlers) {
                        handler.handle(event);
                    }
                }
            } finally {
                lock.unlock();
            }

            // next, check to see if this event should be processed by the subtype processors.
            lock = acceptsSubtypesEventProcessorsLock.readLock();
            lock.lock();
            try {
                for (final EventProcessor<Event> handler : acceptsSubtypesEventProcessors) {
                    if (handler.getEventClass().isInstance(event)) {
                        handler.handle(event);
                    }
                }
            } finally {
                lock.unlock();
            }

        }
    }

    private class ThreadedDispatcher implements EventDispatcher {
        public void dispatch(final Event event) {
            final Class<? extends Event> eventClass = event.getClass();
            Lock lock;

            // first check handlers that want this and only this event type.
            lock = equalTypesEventProcessorMapLock.readLock();
            lock.lock();
            try {
                List<EventProcessor> handlers = equalTypesEventProcessorMap.get(eventClass);
                if (handlers != null) {
                    for (final EventProcessor<Event> handler : handlers) {
                        new Thread() {
                            public void run() {
                                handler.handle(event);
                            }
                        }.start();
                    }
                }
            } finally {
                lock.unlock();
            }

            // next, check to see if this event should be processed by the subtype processors.
            lock = acceptsSubtypesEventProcessorsLock.readLock();
            lock.lock();
            try {
                for (final EventProcessor<Event> handler : acceptsSubtypesEventProcessors) {
                    if (handler.getEventClass().isInstance(event)) {
                        new Thread() {
                            public void run() {
                                handler.handle(event);
                            }
                        }.start();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
