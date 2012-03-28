package edu.asu.commons.event;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * A standard framework Event that tells a client how long it will be
 * until the next Experiment round begins in addition to any
 * special instructions to be displayed (specific to the next round).
 * 
 * @author Allen Lee
 * @version $Revision$
 */
public class RegistrationEvent<C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>> extends SetConfigurationEvent<C, R> {

    private static final long serialVersionUID = -3110349742813500785L;

    public RegistrationEvent(Identifier target, R parameters) {
        super(target, parameters);
    }

}