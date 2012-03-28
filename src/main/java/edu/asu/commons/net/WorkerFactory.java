package edu.asu.commons.net;

/**
 * $Id$
 * 
 * @author alllee
 * 
 * @version $Revision$
 */
public interface WorkerFactory<T> {

    public Worker<T> create();
}
