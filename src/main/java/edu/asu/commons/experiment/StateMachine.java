package edu.asu.commons.experiment;

import edu.asu.commons.net.Dispatcher;

/**
 * $Id$
 * 
 * Basic experiment state machine.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface StateMachine {
    public void initialize();

    public void execute(Dispatcher dispatcher);
    // public void execute();
}