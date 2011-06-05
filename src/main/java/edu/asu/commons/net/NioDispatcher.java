package edu.asu.commons.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.net.event.ConnectionEvent;
import edu.asu.commons.net.event.DisconnectionEvent;


/**
 * $Id: NioDispatcher.java 293 2009-10-10 01:18:43Z alllee $
 *
 * This class manages network connections via the java.nio package and can be 
 * used by both clients and servers.  Network connection descriptors (Identifiers)
 * are used to uniformly refer to the individual connections across the network.
 * The NioDispatcher is both a client and a server dispatcher, allowing p2p 
 * connections.
 * 
 * 
 * FIXME: replace WorkerPool implementation with 1.5 concurrency constructs
 * instead from java.util.concurrent.
 *
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 293 $
 */
public class NioDispatcher extends AbstractServerDispatcher implements ClientDispatcher {

	// FIXME: this is a potential source of bugs
    private final static int READ_BUFFER_SIZE = 32768;
    
    private final static int BYTES_PER_INT = 4;
    
    private ServerSocketChannel server;
    
    // either a single NioDispatcherWorker or an aggregate WorkerPool that
    // constructs NioDispatcherWorkers.
    private Worker<SocketChannel> worker;
    
    // we need to maintain a mapping between Identifiers -> SocketChannel as
    // well as SocketChannel -> Identifier due to the way nio inherently
    // works; when data is incoming across the network the Selector is woken
    // up and we don't have any Identifier information available, hence the
    // need for the reverse mapping to look up an Identifier based on a
    // SocketChannel.

    // bidirectional map of Identifier <-> SocketChannel
    private final Map<Identifier, SocketChannel> connections =
        new HashMap<Identifier, SocketChannel>();
    // maps Identifiers of clients that are in the process of
    // sending an Object across the stream but only got there halfway
    private Map<Identifier, PendingDataBuffer> pendingClients = 
        new HashMap<Identifier, PendingDataBuffer>();
    
    /**
     * package private to enforce access via the DispatcherFactory.
     */
    NioDispatcher(EventChannel channel, int workerPoolSize) {
        super(channel);
        initWorkerPool(workerPoolSize);
    }
    
    /**
     * package private to enforce access via the DispatcherFactory.
     */
    NioDispatcher(EventChannel channel) {
        this(channel, 10);
    }
    
    private void initWorkerPool(int size) {
        // XXX: special case worker pool of size 1 to just be a single Worker.
        if (size > 1) {
            WorkerFactory<SocketChannel> factory = new WorkerFactory<SocketChannel>() {
                public Worker<SocketChannel> create() {
                    return new NioDispatcherWorker();
                }
            };
            worker = new WorkerPool<SocketChannel>(size, factory);
        }
        else {
            worker = new NioDispatcherWorker();
        }
    }    

    public boolean isConnected(Identifier id) {
        return true;
    }
    
    public Identifier connect(String host, int port) {
        return connect(new InetSocketAddress(host, port));
    }
    
    public Identifier connect(InetSocketAddress address) {
        try {
            SocketChannel connection = SocketChannel.open(address);
            connection.configureBlocking(true);
//          block until we've read the socket identifier from server.
            Identifier id = readConnectionEvent(connection);
            connection.configureBlocking(false);
            worker.process(connection);
            // XXX: we return an Identifier that's .equals() with the
            // Identifiers used on the Server side.
            addMapping(id, connection);
            return id;
        } 
        catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Connection refused: " + e);
            return null;
        }
    }

    private Identifier readConnectionEvent(SocketChannel connection) throws IOException {
        InputStream in = connection.socket().getInputStream();
        // read past the int header
        in.read();        in.read();        in.read();        in.read();
        ObjectInputStream ois = new ObjectInputStream(in);
        try {
            ConnectionEvent event = (ConnectionEvent) ois.readObject();
            return event.getId();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not read connection event", e);
        }
    }

    private void addMapping(Identifier id, SocketChannel channel) {
        connections.put(id, channel);
    }

    private SocketChannel getConnection(Identifier id) {
        return connections.get(id);
    }
    
    public void disconnect(Identifier id) {
        SocketChannel channel = getConnection(id);
        disconnect(id, channel);
    }
    
    private void disconnect(Identifier id, SocketChannel channel) {
        if (id == null || channel == null) {
            getLogger().warning("trying to disconnect [id: " + id + "] [channel: " + channel + "] (ignoring)");            
        }
        getLogger().info("disconnecting: " + id);
        connections.remove(id);
        pendingClients.remove(id);
        worker.remove(channel);
        // notify any interested subscribers that a disconnection event has occurred.
        getLocalEventHandler().handle(new DisconnectionEvent(id));
    }
    
    private byte[] marshal(Event event) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(event);
            oos.flush();
            return baos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
            requestDisconnection(event.getId(), e);
        }
        // FIXME: improve exception handling.
        throw new RuntimeException("Unable to convert event into raw byte data: " + event);
    }
    
    private Event unmarshal(Identifier id, byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Event event = (Event) ois.readObject();
            // should assert that id.equals(event.id())
            return event;
        }
        catch (IOException e) {
            e.printStackTrace();
            requestDisconnection(id, e);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            requestDisconnection(id, e);
        }
        // FIXME: improve exception handling.
        throw new RuntimeException("Unable to unmarshal raw byte data for the following Identifier: " + id);
    }

    public void transmit(Event event) {
        try {
            byte[] data = marshal(event);
            write(event.getId(), data);
        }
        catch (IOException e) {
            e.printStackTrace();
            requestDisconnection(event.getId(), e);
        }
    }    
        
//    private Duration duration = Duration.create(1).start();
    /**
     * Writes the given byte array to the SocketChannel identified by this id.
     */
    private void write(Identifier id, byte[] data) throws IOException {
        SocketChannel channel = getConnection(id);
        if (channel == null) {
            // FIXME: schedule a sink.dropConnection for later, so as to avoid
            // ConcurrentModificationExceptions in the ForagerServer.  Also,
            // this really should be an IllegalArgumentException...?
            disconnect(id);
            throw new IllegalArgumentException("Attempting to write with an Identifier that doesn't exist: " + id);
        }
        synchronized (channel) {
            final int objectSize = data.length;
//            if (duration.hasExpired()) {
//            	System.err.println("object size: " + objectSize);
//            	duration.restart();
//            }
            // FIXME: could be a performance bottle-neck in the future, allocation
            // can be expensive.
            // allocate enough space for the object and the int header.  
            // ints in Java are defined by the language spec to always 
            // be 32-bits (4 bytes)
            ByteBuffer buffer = ByteBuffer.allocate(objectSize + BYTES_PER_INT);
            // int header specifying how big the object is is needed so that the
            // other side can know how much data to expect to read.
            buffer.putInt(objectSize);
            buffer.put(data);
            buffer.flip();
            channel.write(buffer);
        }
    }
    
    /**
     * Used by NioDispatcherWorker to send keys with ready ops to process
     */
    protected synchronized void readData(SelectionKey key, ByteBuffer buffer) {	
        try {
            // 1. read an int that tells us how big the object is
            // 2. read that many bytes, if we don't have that many, wait to
            // read it some more.
            // 3. once we've read that many bytes, reset.
            // FIXME: problems still exist with the hard-coded read buffer
            // size allocation.  For instance, what happens when we send
            // something larger than the set READ_BUFFER_SIZE?  We have to 
            // deal with chunking that up.
            SocketChannel channel = (SocketChannel) key.channel();
            Identifier id = getIdentifier(key);
            
            // clear out the original buffer.
            buffer.clear();
            channel.read(buffer);
            buffer.flip();
            
            if (buffer.remaining() == 0) {
                getLogger().warning("buffer of size 0 for id: " + id + " - disconnecting!");
                disconnect(id, channel);
                return;
            }
            // check to see if we're waiting for more stuff from this guy.
            synchronized (pendingClients) {
                if ( pendingClients.containsKey(id) ) {
                    handlePendingRequest(id, buffer);
                }
                else {
                    handleRequest(id, buffer);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            requestDisconnection(getIdentifier(key), e);            
        }
    }
    
    private Identifier getIdentifier(SelectionKey key) {
        return (Identifier) key.attachment();
    }

    // XXX: assumes that pendingClients has been locked
    private void handleRequest(Identifier id, ByteBuffer buffer) {
        // get the size of the Object so we know how much data we expect to
        // receive.  
        // FIXME: assumes that the int itself won't get interrupted!
        int expectedSize = buffer.getInt();
        int actualSize = buffer.remaining();
//        System.err.println("expected size: " + expectedSize);
        byte[] data = new byte[expectedSize];
        
        if (actualSize >= expectedSize) {
            // grab as much data as we need to fill up our Object.
            buffer.get(data, 0, expectedSize);
            getLocalEventHandler().handle( unmarshal(id, data) );
            if (actualSize > expectedSize) {
                // overflow! get the next object later.
                // FIXME: can we really re-use the same old buffer?
                handleRequest(id, buffer);
            }
        }
        else {
            // underflow, read only the amount that was sent.
            buffer.get(data, 0, actualSize);
            // send this byte array into limbo until we have the entire
            // Object. 
            // XXX: we are guaranteed that the pending clients doesn't
            // contain the id already, otherwise handleRequest wouldn't have
            // gotten called.
//            System.err.println("creating new pending data buffer for " + id + " with actual size " + actualSize);
            pendingClients.put(id, new PendingDataBuffer(actualSize, data));
        }
    }
    
    // XXX: assumes that pendingClients has been locked
    private void handlePendingRequest(Identifier id, ByteBuffer buffer) {
        PendingDataBuffer pendingDataBuffer = pendingClients.get(id);
        pendingDataBuffer.fillFrom(buffer);
        if ( pendingDataBuffer.isFinished() ) {
            pendingClients.remove(id);
            getLocalEventHandler().handle( unmarshal(id, pendingDataBuffer.data) );
            if (buffer.remaining() > 0) {
                getLogger().info("buffer for client: " + id + " still had crap in it: " 
                        + buffer.remaining());
                handleRequest(id, buffer);
            }
        }
    }
    
    private static class PendingDataBuffer {
        final byte[] data;
        // amount of usable data that is actually in the byte array, in other
        // words an index/offset into the free space of the data array.
        int dataIndex;
        PendingDataBuffer(int dataIndex, byte[] data) {
            this.dataIndex = dataIndex;
            this.data = data;
        }
        boolean isFinished() {
            return data.length == dataIndex;
        }
        void fillFrom(ByteBuffer buffer) {
            // clamp bufferSize 
            int readLength = getReadLength(buffer);
            buffer.get(data, dataIndex, readLength);
            dataIndex += readLength;
        }
        private int getReadLength(ByteBuffer buffer) {
            return ( buffer.remaining() > amountNeeded() ) 
                ? amountNeeded()
                : buffer.remaining();
        }
        private int amountNeeded() {
            return data.length - dataIndex;
        }
    }
    
    @Override
    protected void cleanup() {
        worker.shutdown();
        try {
            if (server != null) {
                server.close();
                server = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("unable to close server socket:" + e);
        }
    }
    
    @Override
    protected void bind(int port) throws IOException {
        server = ServerSocketChannel.open();
        server.configureBlocking(true);
        server.socket().bind(new InetSocketAddress(port));
    }
    
    @Override
    protected void processIncomingConnections() throws IOException {
        final SocketChannel incoming = server.accept();
        getLogger().info("incoming connection: " + incoming);
        new Thread() {
            @Override
            public void run() {
                Identifier id = worker.process(incoming);
//                System.err.println("generated id" + id);
                addMapping(id, incoming);
                // send the newly generated Identifier to the client dispatcher, 
                // which should be blocked, waiting for it.
                ConnectionEvent connectionEvent = new ConnectionEvent(id);
                transmit(connectionEvent);
                // notify any interested parties that a new connection has been
                // made with the given Identifier.
                getLocalEventHandler().handle(connectionEvent);
            }
        }.start();
    }
    
    private class NioDispatcherWorker implements Worker<SocketChannel> {
        //private final static int SLEEP_TIME = 200;
        private boolean running;
        private final Selector selector;
        // incoming socket channels.
        private final LinkedList<SocketChannel> channels = 
            new LinkedList<SocketChannel>();
        
        // Each Worker maintains a copy of its own ByteBuffer.  
        // FIXME: The read buffer should be dynamic to support variable size objects.
        private final ByteBuffer buffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
        
        public NioDispatcherWorker() {
            try {
                selector = Selector.open();
                new Thread(this).start();
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        
        public int numberOfJobs() {
            return selector.keys().size();
        }
        
        /**
         * Adds the incoming socket channel to the queue of channels to be registered
         * and then blocks the currently executing thread.  This call will block until
         * the incoming SocketChannel has successfully registered with the Selector.                   
         */

        public Identifier process(SocketChannel incoming) {
            synchronized (channels) {
            	try {
            		selector.selectNow();
            	}
            	catch (IOException exception) {
            		exception.printStackTrace();
            	}
                channels.add(incoming);
            }
            synchronized (incoming) {
                selector.wakeup();
                try {
                    incoming.wait();
                } catch (InterruptedException e) {}
            }
            Identifier id = new SocketIdentifier(incoming.socket());
            incoming.keyFor(selector).attach(id);
            return id;
        }
        
        public void remove(SocketChannel channel) {
            if (channel == null) {
                getLogger().warning("trying to remove a null channel");
                return;                
            }
            SelectionKey k = channel.keyFor(selector);
            if (k == null) {
                getLogger().warning("No selection key available for channel: " + channel);
                return;
            }
            k.cancel();
            try {
                channel.socket().shutdownInput();
                channel.socket().shutdownOutput();
                channel.socket().close();
                channel.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }            
        }
        
        public void report() {
            StringBuilder msg = new StringBuilder();
            msg.append(this + " has " + selector.keys().size() + " channels\n");
            for (SocketChannel channel : channels) {
                msg.append("  ");
                msg.append(channel.toString());
                msg.append('\n');
            }
            getLogger().info(msg.toString());
        }
        
        public int compareTo(Worker<SocketChannel> worker) {
            return numberOfJobs() - worker.numberOfJobs();
        }
        
        public void shutdown() {
            // This loop is for all channels that are currently connected to
            // the Dispatcher and being actively I/O-ed.
            for (SelectionKey key : selector.keys()) {
                remove((SocketChannel) key.channel());
            }
            // remove all channels that are pending (still waiting to be
            // connected).
            for (Iterator<SocketChannel> iter = channels.iterator(); iter.hasNext(); ) {
                remove( iter.next() );
                iter.remove();
            }
            running = false;
        }
        
        public void run() {
            // assumes that we don't need to worry about
            // multiple threads starting this, otherwise we need to lock
            // on the test-and-set here.
            if (running) return;
            running = true;
            while (running) {
                // check to see if we have any new incoming channels.
                synchronized (channels) {
                    for (Iterator<SocketChannel> iter = channels.iterator(); iter.hasNext(); )
                    {
                        SocketChannel incoming = iter.next();
                        iter.remove();
                        if (incoming.isOpen()) {
                            try {
                                incoming.configureBlocking(false);
                                incoming.register(selector, SelectionKey.OP_READ);
                            }
                            catch (IOException e) {
                                // recoverable, incoming connection was broken, 
                                // just ignore it and move on.
                                e.printStackTrace();
                                continue;
                            }
                            finally {
                                synchronized (incoming) {
                                    incoming.notifyAll();
                                }
                            }
                        }
                    }
                }
                // handle incoming data.
                try {
                    if ( selector.select() >= 0 ) {
                        for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) 
                        {
                            SelectionKey key = iterator.next();
                            NioDispatcher.this.readData(key, buffer);
                            iterator.remove();
                        }
                    }
                    else {
                        getLogger().warning("select returned < 0");
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
