package edu.asu.commons.net;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.EventChannel;

/**
 * $Id$
 * Factory for constructing the appropriate dispatcher.
 * 
 * @author Allen Lee
 * @version $Revision$
 * 
 */
public class DispatcherFactory {

    public final static DispatcherFactory INSTANCE = new DispatcherFactory();
    public final static int DEFAULT_WORKER_POOL_SIZE = 3;

    private DispatcherFactory() {
    }

    public static DispatcherFactory getInstance() {
        return INSTANCE;
    }

    public <C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>> ClientDispatcher createClientDispatcher(EventChannel channel,
            C serverConfiguration) {
        switch (serverConfiguration.getServerDispatcherType()) {
            case NIO:
                return new NioDispatcher(channel, 1);
//            case NETTY_NIO:
//                return new ClientNettyDispatcher(channel);
            case SOCKET:
            default:
                return new ClientSocketDispatcher(channel);
        }
    }

    public ServerDispatcher createServerDispatcher(EventChannel channel) {
        return createServerDispatcher(channel, DEFAULT_WORKER_POOL_SIZE, ServerDispatcher.Type.SOCKET);
    }

    public <C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>> ServerDispatcher createServerDispatcher(EventChannel channel,
            C serverConfiguration) {
        return createServerDispatcher(channel, serverConfiguration.getWorkerPoolSize(), serverConfiguration.getServerDispatcherType());
    }

    public ServerDispatcher createServerDispatcher(EventChannel channel, int workerPoolSize, ServerDispatcher.Type serverDispatcherType) {
        switch (serverDispatcherType) {
            case NIO:
                return new NioDispatcher(channel, workerPoolSize);
//            case NETTY_NIO:
//                return new NettyDispatcher(channel);
//                

            case SOCKET:
            	// default fall through is a socket dispatcher (safer)
            default:
                return new ServerSocketDispatcher(channel, workerPoolSize);

        }
    }
}
