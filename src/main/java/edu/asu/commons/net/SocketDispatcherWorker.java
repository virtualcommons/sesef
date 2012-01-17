package edu.asu.commons.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;

import edu.asu.commons.event.Event;
import edu.asu.commons.net.event.DisconnectionRequest;

/**
 * $Id: SocketDispatcherWorker.java 305 2009-10-20 00:15:43Z alllee $
 *
 * This class is a basic Socket-based Runnable that helps Client and Server SocketDispatchers perform 
 * reading and writing of Events.  Will read Events from the socket via blocking I/O after this Runnable
 * is start()-ed.  Writes occur within caller's thread of execution.
 * 
 * @author <a href='allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 305 $
 */
class SocketDispatcherWorker implements Runnable {

    private final Dispatcher dispatcher;
    private final Socket socket;
    private Identifier id;
    private Thread workerThread;
    private boolean running;
    private ObjectOutputStream cachedOut;
    private ObjectInputStream cachedIn;
    
    protected SocketDispatcherWorker(Dispatcher dispatcher, Socket socket) throws IOException {
        this.dispatcher = dispatcher;
        this.socket = socket;
        cachedOut = new ObjectOutputStream(socket.getOutputStream());
        cachedOut.flush();
        cachedIn = new ObjectInputStream(socket.getInputStream());
    }
    
    protected SocketDispatcherWorker(Dispatcher dispatcher, Socket socket, Identifier id) throws IOException {
        this(dispatcher, socket);
        this.id = id;
    }
    
    public synchronized void start() {
        if (workerThread == null) {
            running = true;
            workerThread = new Thread(this);
            workerThread.start();
        }
    }
    
    /**
     * Shuts down this Worker's thread of execution and closes the Socket 
     * connection.  There is no way to recover from an invocation of this
     * method, invoking start() afterwards will result in unspecified 
     * behavior (should throw an IllegalStateException).
     */
    public void stop() {
        running = false;
        workerThread = null;
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void write(Event event) {
        try {
            synchronized (socket) {
                cachedOut.reset();
                cachedOut.writeObject(event);
                cachedOut.flush();
            }
            // XXX: don't close() the stream.  It closes the wrapped Socket 
            // OutputStream and all subsequent usage / reads will fail 
        }
        catch (IOException e) {
            e.printStackTrace();
            requestDisconnection(e);
        }
    }
    
    /**
     * Provides a static way to read an Event from a socket.  This method will
     * block if there's nothing coming down the stream.
     *   
     * @param socket
     * @return the next remote Event sent by a client.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Event readEvent(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        return (Event) in.readObject();
    }
    
    public synchronized Event readEvent() throws IOException, ClassNotFoundException {
//        return SocketDispatcherWorker.readEvent(socket);
        return (Event) cachedIn.readObject();
    }

    public Socket getSocket() {
        return socket;
    }
    
    public Identifier id() {
        return id;
    }
    
    private boolean isRunning() {
        return running && ! socket.isClosed();
    }
    
    private void requestDisconnection(Exception exception) {
        dispatcher.getLocalEventHandler().handleWithNewThread(new DisconnectionRequest(id, exception));
    }
    
    public void run() {
        try {
//            cachedIn = new ObjectInputStream(socket.getInputStream());
            while ( isRunning() ) {
                // try to read Events from the socket
                try {                                                             
                    dispatcher.getLocalEventHandler().handle( readEvent() );
                    // wake up all threads waiting on the dispatcher..
                    synchronized (dispatcher) {
                        dispatcher.notifyAll();
                    }
                }
                // XXX: try to recover from StreamCorruptedExceptions
                catch (StreamCorruptedException e) {
                    e.printStackTrace();
                    break;
                }
                catch (EOFException e) {
                    e.printStackTrace();
                    requestDisconnection(e);
                    // kills this thread.
                    stop();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    requestDisconnection(e);
                    stop();
                } 
                catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    requestDisconnection(e);
                    stop();
                }
            } 
        } 
        catch (Exception e) {
            // runtime unhandled exception.
            e.printStackTrace();
            requestDisconnection(e);
        }
    }       
    
    /**
     * Should only be invoked by the client after receiving its Identifier from the server.
     * @param id
     */
    void setId(Identifier id) {
        this.id = id;
    }
}
