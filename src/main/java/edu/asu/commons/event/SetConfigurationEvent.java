package edu.asu.commons.event;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.net.Identifier;

/**
 * $Id: SetConfigurationEvent.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * Notifies the client of the next round configuration to be used.
 *
 * @author Allen Lee
 * @version $Revision: 1 $
 */
public class SetConfigurationEvent<T extends ExperimentRoundParameters<? extends ExperimentConfiguration<T>>> extends AbstractEvent {
    
    private static final long serialVersionUID = 4221950887888018509L;
    private final T parameters;
    
    public SetConfigurationEvent(Identifier target, T parameters) {
        super(target);
        this.parameters = parameters;
    }
    
    public T getParameters() {
        return parameters;
    }
    
    public String getInstructions() {
        return parameters.getInstructions();
    }

}
