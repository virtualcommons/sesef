package edu.asu.commons.net;


// import java.nio.channels.SocketChannel;

/**
 * $Id: Worker.java 1 2008-07-23 22:15:18Z alllee $
 * @author alllee
 * 
 * Interface for worker threads specific to subscribing NIO socket
 * channels.
 *
 * @version $Revision: 1 $
 */
public interface Worker<T> extends Runnable, Comparable<Worker<T>> {
    public Identifier process(T object);

    public int numberOfJobs();

    public void remove(T object);

    public void report();
//    public void remove(Object object);
//    public void isRunning();
//    public void setRunning(boolean isRunning);

    public void shutdown();
}
