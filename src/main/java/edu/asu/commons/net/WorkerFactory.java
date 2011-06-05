package edu.asu.commons.net;

/**
 * $Id: WorkerFactory.java 1 2008-07-23 22:15:18Z alllee $
 * @author alllee
 *
 * @version $Revision: 1 $
 */
public interface WorkerFactory<T> {

    public Worker<T> create();
}

