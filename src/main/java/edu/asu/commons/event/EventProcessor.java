package edu.asu.commons.event;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.experiment.Experiment;

/**
 * Support interface for processing events, encapsulating both an EventHandler and an EventConstraint
 * in the same class/type.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */

public interface EventProcessor<E extends Event> extends EventHandler<E>, EventConstraint {

    public boolean acceptsSubtypes();

    public Class<E> getEventClass();

    public void handleInExperimentThread(E event);

    public <C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>, T extends Experiment<C, R>> void setExperiment(T experiment);

}
