package edu.asu.commons.event;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Notifies the client of the next round configuration to be used.
 * 
 * @author Allen Lee
 * @version $Revision$
 */
public class SetConfigurationEvent<C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>>
        extends AbstractEvent {

    private static final long serialVersionUID = 4221950887888018509L;
    private final R parameters;

    public SetConfigurationEvent(Identifier target, R parameters) {
        super(target);
        this.parameters = parameters;
    }

    public R getParameters() {
        return parameters;
    }

    public String getInstructions() {
        return parameters.getInstructions();
    }

}
