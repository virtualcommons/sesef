package edu.asu.commons.client;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventChannelFactory;
import edu.asu.commons.event.EventProcessor;
import edu.asu.commons.net.ClientDispatcher;
import edu.asu.commons.net.DispatcherFactory;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.net.event.DisconnectionRequest;

/**
 * $Id$
 *  
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public abstract class BaseClient<E extends ExperimentConfiguration<R>, R extends ExperimentRoundParameters<E>> {
    
    private final ClientDispatcher dispatcher;
    
    private final EventChannel channel;
    
    private Identifier id;
    
    private E serverConfiguration;
    
    public BaseClient(E configuration) {
        this(configuration, EventChannelFactory.create());
    }
    
    public BaseClient(E configuration, EventChannel channel) {
        this(configuration, channel, DispatcherFactory.getInstance().createClientDispatcher(channel, configuration));
    }
    
    public BaseClient(E configuration, EventChannel channel, ClientDispatcher dispatcher) {
        if (configuration == null) {
            throw new NullPointerException("Null experiment configuration disallowed");
        }
        if (channel == null) {
            throw new NullPointerException("Null event channel disallowed");
        }
        if (dispatcher == null) {
            throw new NullPointerException("Null client dispatcher disallowed");
        }
        this.serverConfiguration = configuration;
        this.channel = channel;
        this.dispatcher = dispatcher;
    }
    
    
    // FIXME: figure out a better way to copy over the configuration for integrating multiple clients..
    @Deprecated
    public BaseClient(BaseClient<E, R> client) {
        this(client.serverConfiguration, client.channel, client.dispatcher);
        this.id = client.id;
    }
    
    protected void initializeEventProcessors() { }
    protected void postConnect() { }
    
    public void connect() {
        initializeEventProcessors();
        this.id = dispatcher.connect(serverConfiguration.getServerAddress());
        if (id == null) {
            throw new RuntimeException("Could not connect to server: <"
                    + serverConfiguration.getServerAddress()
                    + "> is probably down.");
        }
        postConnect();
    }
    
    public void transmit(Event event) {
        dispatcher.transmit(event);
    }
    
    public void disconnect() {
        transmit(new DisconnectionRequest(id));
        channel.remove(this);
    }
    
    public ClientDispatcher getDispatcher() {
        return dispatcher;
    }
    
    public Identifier getId() {
        return id;
    }
    
    public EventChannel getEventChannel() {
        return channel;
    }
    
    protected void setId(Identifier id) {
        this.id = id;
    }
    
    protected void addEventProcessor(EventProcessor<? extends Event> processor) {
        channel.add(this, processor);
    }

}
