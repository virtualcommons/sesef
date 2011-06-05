package edu.asu.commons.experiment;

import edu.asu.commons.net.Dispatcher;

/**
 * $Id: StateMachine.java 296 2009-10-13 17:09:51Z alllee $
 * 
 * Basic experiment state machine.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 296 $
 */
public interface StateMachine {
    public void initialize();
    public void execute(Dispatcher dispatcher);
//    public void execute();
}