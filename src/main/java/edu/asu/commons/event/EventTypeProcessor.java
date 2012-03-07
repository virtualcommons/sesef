package edu.asu.commons.event;

import edu.asu.commons.command.Command;
import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.experiment.Experiment;


/**
 * $Id$
 *
 * An EventTypeProcessor encapsulates both an EventHandler and an EventInstanceofConstraint
 * within the same class for convenience sake.  
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public abstract class EventTypeProcessor<E extends Event> implements EventProcessor<E> {

    private final Class<E> eventClass;
    
    private final boolean acceptsSubtypes;
    
    @SuppressWarnings("rawtypes")
    private Experiment experiment;
    
    /**
     * FIXME: find a way to get around having to specify the type of the class
     * twice, if possible.  E.g., we have to do something like this currently:
     * new EventTypeProcessor<ConnectionEvent>(ConnectionEvent.class) { ... }
     * Ideally there should be a way to do just 
     * new EventTypeProcessor(ConnectionEvent.class)
     * 
     * Wish there was a way to infer the type variable for the entire class 
     * from the constructor.
     */ 
    public EventTypeProcessor(final Class<E> eventClass) {
        this(eventClass, false);
    }
    
    public EventTypeProcessor(final Class<E> eventClass, boolean acceptsSubtypes) {
        this.eventClass = eventClass;
        this.acceptsSubtypes = acceptsSubtypes;
    }
    
    /**
     * Override this method to handle in the same thread of execution invoking EventChannel.handle(someEvent);
     * Otherwise, override handleInExperimentThread() to queue a new command to be processed serially by the
     * experiment.
     * 
     */
    public void handle(final E event) {
    	experiment.schedule(new Command() {
    		public void execute() {
    			handleInExperimentThread(event);
    		}
    	});
    }
    
    public void handleWithNewThread(final E event) {
        new Thread() {
            public void run() {
                handle(event);
            }
        }.start();
    }
    
    /**
     * Override this method to schedule the custom handling of an event in the dedicated experiment thread.
     * Useful for synchronous actions occurring within an experimental round that need to be serialized
     * and don't want to have to explicitly synchronize on some data structure. 
     */
    public void handleInExperimentThread(E event) {
    	throw new UnsupportedOperationException("Override handleInExperimentThread and make sure you do NOT override handle(E) if you want to use single-threaded event handling.");
    }
    
    public Class<E> getEventClass() {
        return eventClass;
    }

    public boolean accepts(Event event) {
        return eventClass.isInstance(event);
    }
    
    public boolean acceptsSubtypes() {
        return acceptsSubtypes;
    }

	public <C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>, T extends Experiment<C, R>> void setExperiment(T experiment) {
		this.experiment = experiment;
	}
}
