package edu.asu.commons.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.net.event.DisconnectionRequest;

/**
 * $Id$
 * 
 * Abstract base class for ServerDispatchers.
 * 
 * 
 * @author Allen Lee
 * @version $Revision$
 */

public abstract class AbstractServerDispatcher extends AbstractDispatcher
        implements ServerDispatcher {

    private int listeningPort;
    private Thread dispatcherThread;
    private final List<Identifier> disconnectedClients = new ArrayList<Identifier>();
    private Logger logger = Logger.getLogger(getClass().getName());

    private boolean listening;

    public AbstractServerDispatcher(EventChannel channel) {
        super(channel);
        channel.add(this, new EventTypeProcessor<DisconnectionRequest>(DisconnectionRequest.class) {
            public void handle(DisconnectionRequest request) {
                logger.warning("disconnecting: " + request.getId() + request.getException());
                synchronized (disconnectedClients) {
                    disconnectedClients.add(request.getId());
                }
            }
        });
    }

    protected abstract void bind(int port) throws IOException;

    /**
     * This method executes within the Dispatcher's thread of execution.
     * 
     * @throws IOException
     */
    protected abstract void processIncomingConnections() throws IOException;

    protected abstract void cleanup();

    protected Logger getLogger() {
        return logger;
    }

    /**
     * Start a server on the given port
     */
    public synchronized void listen(int port) {
        if (listening) {
            // if we are already listening on a port, ignore the request to
            // listen again.
            dispatcherThread.interrupt();
            logger.warning(String.format(
                    "Trying to listen on port [%d] but already listening on port [%d]",
                    port, listeningPort));
            return;
        }
        listeningPort = port;
        dispatcherThread = createDispatcherThread();
        dispatcherThread.start();
    }

    /**
     * Shuts the Dispatcher down and disconnects all open connections.
     * 
     */
    public synchronized void shutdown() {
        // kill this Dispatcher's thread of execution, it loops based on the
        // state of the dispatcherThread instance variable.
        logger.info("Shutting down server on port: " + listeningPort);
        listening = false;
        dispatcherThread = null;
        // clear all data structures.
        disconnectedClients.clear();
        getLocalEventChannel().remove(this);
        // and finally perform custom subclass cleanup.
        cleanup();
    }

    private Thread createDispatcherThread() {
        return new Thread() {
            /**
             * Template method controlling the flow of execution for this Dispatcher.
             * Subclasses should implement bind(int port) and processIncomingConnections()
             * for their own custom processing.
             */
            @Override
            public void run() {
                int port = listeningPort;
                logger.info(getClass() + " listening on port:" + port);
                try {
                    bind(port);
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.severe(String.format("Couldn't bind to port %d due to exception [%s] - shutting down.", port, e));
                    shutdown();
                    return;
                }
                listening = true;
                while (listening) {
                    try {
                        processIncomingConnections();
                        // disconnect any pending disconnected clients
                        performConnectionMaintenance();
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.severe("IO Exception while processing incoming connections: " + e);
                    }

                }
            }
        };
    }

    public boolean isListening() {
        return listening;
    }

    private void performConnectionMaintenance() {
        synchronized (disconnectedClients) {
            for (Iterator<Identifier> iter = disconnectedClients.iterator(); iter.hasNext();) {
                Identifier id = iter.next();
                logger.info("Disconnecting client: " + id);
                disconnect(id);
                iter.remove();
            }
        }
    }

}
