package edu.asu.commons.event;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.net.Identifier;


/**
 * $Id: RegistrationEvent.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * A standard framework Event that tells a client how long it will be
 * until the next Experiment round begins in addition to any 
 * special instructions to be displayed (specific to the next round).
 *
 * @author Allen Lee
 * @version $Revision: 1 $
 */
public class RegistrationEvent<T extends ExperimentRoundParameters<? extends ExperimentConfiguration<T>>> extends SetConfigurationEvent<T> {
    
    private static final long serialVersionUID = -3110349742813500785L;
    
    public RegistrationEvent(Identifier target, T parameters) {
        super(target, parameters);
    }
    
}