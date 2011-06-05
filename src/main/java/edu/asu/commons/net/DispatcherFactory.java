package edu.asu.commons.net;

import edu.asu.commons.event.EventChannel;


/**
 * $Id: DispatcherFactory.java 296 2009-10-13 17:09:51Z alllee $
 * Factory for constructing the appropriate dispatcher.
 * 
 * @author Allen Lee
 * @version $Revision: 296 $
 *
 */
public class DispatcherFactory {
    
    public final static DispatcherFactory INSTANCE = new DispatcherFactory();
    public final static int DEFAULT_WORKER_POOL_SIZE = 3;
    
    private DispatcherFactory() {}
    
    public static DispatcherFactory getInstance() {
        return INSTANCE;
    }
    
    public ClientDispatcher createClientDispatcher(EventChannel channel) {
        return new ClientSocketDispatcher(channel);
//    	return new NioDispatcher(channel, 1);
    }
    
    public ServerDispatcher createServerDispatcher(EventChannel channel) {
        return createServerDispatcher(channel, DEFAULT_WORKER_POOL_SIZE);
    }
          
    public ServerDispatcher createServerDispatcher(EventChannel channel, int workerPoolSize) {
//         return new NioDispatcher(channel, workerPoolSize);
        return new ServerSocketDispatcher(channel, workerPoolSize);
    }
}
