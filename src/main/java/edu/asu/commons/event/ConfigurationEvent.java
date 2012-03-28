package edu.asu.commons.event;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * FIXME: Duplication between this and {@see SetConfigurationEvent}
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 * @param <C>
 * @param <R>
 */
public class ConfigurationEvent<C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>>
        extends AbstractEvent {

    private static final long serialVersionUID = 1153572405897631171L;

    private final C configuration;

    public ConfigurationEvent(Identifier id, C configuration) {
        super(id);
        this.configuration = configuration;
    }

    public C getConfiguration() {
        return configuration;
    }

}
