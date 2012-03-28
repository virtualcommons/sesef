package edu.asu.commons.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * $Id: WorkerPool.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * Very basic pooled implementation for worker threads. The WorkerPool is a composite of
 * the Worker.
 * 
 * FIXME: replace with Java 1.5 thread pools, i.e., ThreadPoolExecutor
 * 
 * @author <a href='mailto:alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */

public class WorkerPool<T> implements Worker<T> {

    private final static int DEFAULT_SIZE = 8;

    private WorkerFactory<T> factory;

    // Set of Workers ordered such that the workers with the least number of
    // jobs are in the front of the Set.
    private final TreeSet<Worker<T>> workers = new TreeSet<Worker<T>>();

    private final Map<T, Worker<T>> objectToWorkerMap =
            new HashMap<T, Worker<T>>();

    private final boolean growable;

    public WorkerPool(WorkerFactory<T> factory) {
        this(DEFAULT_SIZE, factory);
    }

    public WorkerPool(int initialSize, WorkerFactory<T> factory) {
        this(initialSize, factory, false);
    }

    // FIXME: should really have GrowableWorkerPool and StaticWorkerPool,
    // Should support the thread model where there is one worker thread per
    // job (in plain old java Socket programming, for instance).
    public WorkerPool(int initialSize, WorkerFactory<T> factory, boolean growable) {
        this.growable = growable;
        for (int i = 0; i < initialSize; i++) {
            workers.add(factory.create());
        }
    }

    public synchronized void remove(T job) {
        Worker<T> worker = objectToWorkerMap.remove(job);
        if (worker == null) {
            return;
        }
        worker.remove(job);
    }

    // FIXME: mayhap this should be non-public. Also, figure out what to do
    // here.
    public synchronized void shutdown() {
        objectToWorkerMap.clear();
        for (Iterator<Worker<T>> iterator = workers.iterator(); iterator.hasNext();) {
            Worker<T> worker = iterator.next();
            // assumes that each worker has the ability to kill itself?
            worker.shutdown();
            iterator.remove();
        }
    }

    public synchronized Identifier process(T object) {
        Worker<T> worker = workers.first();
        // this should always grab the first worker out.
        if (growable) {
            if (worker.numberOfJobs() > 0) {
                worker = factory.create();
                workers.add(worker);
            }
        }
        objectToWorkerMap.put(object, worker);
        return worker.process(object);
    }

    public void report() {
        for (Worker<T> worker : workers) {
            worker.report();
        }
    }

    public int compareTo(Worker<T> worker) {
        return numberOfJobs() - worker.numberOfJobs();
    }

    public int numberOfJobs() {
        return objectToWorkerMap.size();
    }

    public void run() {
        for (Worker<T> worker : workers) {
            new Thread(worker).start();
        }
    }
}
