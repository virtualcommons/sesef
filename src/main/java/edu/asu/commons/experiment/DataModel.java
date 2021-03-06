package edu.asu.commons.experiment;

import java.io.Serializable;
import java.util.List;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Marker interface for data models
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface DataModel<C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>> extends Serializable {

    public C getExperimentConfiguration();

    public R getRoundConfiguration();

    public List<Identifier> getAllClientIdentifiers();

    public EventChannel getEventChannel();
}
