package edu.asu.commons.experiment;

import java.util.logging.Logger;

import edu.asu.commons.command.Command;
import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;

/**
 * $Id$
 * 
 * Contract interface for all Experiment subtypes.
 *   
 * 
 * @author Allen Lee
 * @version $Revision$
 */

public interface Experiment<C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>> {

    public Logger getLogger();

    public void start();
    public void stop();
    
    public int getServerPort();
    public boolean isFull();
    public boolean isRunning();
    
    public C getConfiguration();
    public void setConfiguration(C configuration);
    
    public void schedule(Command command);
    
    public IPersister<C, R> getPersister();
    
}
