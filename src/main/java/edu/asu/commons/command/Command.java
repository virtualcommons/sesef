package edu.asu.commons.command;

/**
 * $Id: Command.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * A serializable command. Intended for use with an AbstractExperiment that wants easier synchronization.
 * 
 * @version $Rev: 1 $
 * @author Allen Lee
 */
public interface Command {
    public void execute();
}
