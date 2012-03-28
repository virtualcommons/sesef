package edu.asu.commons.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.net.event.ConnectionEvent;
import edu.asu.commons.net.event.DisconnectionRequest;

/**
 * $Id$
 * 
 * The client dispatcher only implements the connecting part of the network business.
 * 
 * 
 * @author Allen Lee
 * @version $Revision$
 */
public class ClientSocketDispatcher extends AbstractDispatcher implements ClientDispatcher {

    private SocketDispatcherWorker worker;

    public ClientSocketDispatcher(EventChannel channel) {
        super(channel);
        channel.add(this, new EventTypeProcessor<DisconnectionRequest>(DisconnectionRequest.class) {
            public void handle(DisconnectionRequest event) {
                disconnect(event.getId());
            }
        });
    }

    private void error(String message) {
        System.err.println(message);
    }

    private void info(String message) {
        System.out.println(message);
    }

    public Identifier connect(InetSocketAddress inetSocketAddress) {
        info("SocketDispatcher connecting to: " + inetSocketAddress);
        try {
            Socket socket = new Socket();
            socket.connect(inetSocketAddress);
            // block while we wait for the ServerSocketDispatcher to assign an
            // Identifier to us. The construction of an ObjectInputStream blocks.
            worker = new SocketDispatcherWorker(this, socket);
            ConnectionEvent event = (ConnectionEvent) worker.readEvent();
            assert event instanceof ConnectionEvent;
            Identifier id = event.getId();
            worker.setId(id);
            worker.start();
            getLocalEventHandler().handle(event);
            return id;
        } catch (IOException e) {
            e.printStackTrace();
            error("connection refused: " + e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            error("Unable to find Event class: " + e);
        }
        return null;
    }

    public void disconnect(Identifier id) {
        if (worker.id().equals(id)) {
            worker.stop();
        }
    }

    public void transmit(Event event) {
        worker.write(event);
    }

    public void shutdown() {
        worker.stop();
    }
}
