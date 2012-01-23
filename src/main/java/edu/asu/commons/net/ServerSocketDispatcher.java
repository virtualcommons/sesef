package edu.asu.commons.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.net.event.ConnectionEvent;
import edu.asu.commons.net.event.DisconnectionEvent;

/**
 * $Id$
 * 
 * This class uses traditional blocking I/O via java.net.Socket-S to implement 
 * the Dispatcher interface.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public class ServerSocketDispatcher extends AbstractServerDispatcher {
    
    private ServerSocket serverSocket;
    
    // Map<Identifier, SocketDispatcherWorker>
    private Map<Identifier, SocketDispatcherWorker> workers = new HashMap<Identifier, SocketDispatcherWorker>();
    
    // thread pooling currently unimplemented (should use 1.5 concurrent package anyways)
    ServerSocketDispatcher(EventChannel channel, int workerPoolSize) {
        super(channel);
    }
    
    public boolean isConnected(Identifier id) {
        Socket socket = getConnection(id);
        return socket != null 
            && socket.isBound() 
            && socket.isConnected() 
            && !socket.isClosed() 
            && ! socket.isInputShutdown() 
            && ! socket.isOutputShutdown();
    }   
    
    public void disconnect(Identifier id) {
        getLogger().info(String.format("disconnecting id [%s]", id.toString()));
        SocketDispatcherWorker worker = workers.remove(id);
        if (worker == null) {
            getLogger().warning("Tried to disconnect a nonexistent worker with id: " + id);
            return;
        }
        worker.stop();
        // notify anyone that the given Identifier has been disconnected. 
        getLocalEventHandler().handle(new DisconnectionEvent(id));
    }
    
    public void transmit(Event event) {
        Identifier id = event.getId();
        if (id == null || id == Identifier.NULL) {
            // transmit to all connected clients if the target identifier is
            // not specified.
            for (SocketDispatcherWorker worker: workers.values()) {
                worker.write(event);
            }
        }
        else {
            getWorker(id).write(event);
        }
    }

    private Socket getConnection(Identifier id) {
        return getWorker(id).getSocket();
    }
    
    private SocketDispatcherWorker getWorker(Identifier id) {
        return workers.get(id);
    }
    
    @Override
    protected void bind(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    
    @Override
    protected void processIncomingConnections() throws IOException {
        Socket incoming = serverSocket.accept();
        getLogger().info("incoming connection: " + incoming);
        incoming.setTcpNoDelay(true);
        Identifier id = new SocketIdentifier(incoming);
        SocketDispatcherWorker worker = new SocketDispatcherWorker(this, incoming, id);
        // immediately write a ConnectionEvent to the incoming connection.
        ConnectionEvent event = new ConnectionEvent(id);
        worker.write(event);
        workers.put(id, worker);
        worker.start();
        getLocalEventHandler().handle(event);
    }
    
    @Override
    protected void cleanup() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
