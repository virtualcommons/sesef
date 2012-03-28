package edu.asu.commons.command;

/**
 * $Id$
 * 
 * A serializable command. Intended for use with an AbstractExperiment that wants easier synchronization.
 * 
 * @version $Rev: 1 $
 * @author Allen Lee
 */
public interface Command {
    public void execute();
}
