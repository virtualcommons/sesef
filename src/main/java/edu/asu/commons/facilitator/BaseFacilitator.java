package edu.asu.commons.facilitator;


import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventChannelFactory;
import edu.asu.commons.event.EventProcessor;
import edu.asu.commons.event.FacilitatorRegistrationRequest;
import edu.asu.commons.net.ClientDispatcher;
import edu.asu.commons.net.DispatcherFactory;
import edu.asu.commons.net.Identifier;

/**
 * $Id: BaseFacilitator.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 * @param <E> the concrete ExperimentConfiguration class
 */
public abstract class BaseFacilitator<E extends ExperimentConfiguration<? extends ExperimentRoundParameters<E>>> {

    private final EventChannel channel;
    
    private final ClientDispatcher dispatcher;
    
    private E serverConfiguration;
    
    private Identifier id;
    
    public BaseFacilitator(E serverConfiguration) {
        this(serverConfiguration, EventChannelFactory.create());
    }
    
    public BaseFacilitator(E serverConfiguration, EventChannel channel) {
        this(serverConfiguration, channel, DispatcherFactory.getInstance().createClientDispatcher(channel));
    }
    
    public BaseFacilitator(E serverConfiguration, EventChannel channel, ClientDispatcher dispatcher) {
        this.serverConfiguration = serverConfiguration;
        this.channel = channel;
        this.dispatcher = dispatcher;
    }
    
    public void transmit(Event event) {
        dispatcher.transmit(event);
    }
    
    /*
     * Connects facilitator to the server and registers with the server as a facilitator.
     * 
     * If the connection was successful, configures the FacilitatorWindow to manage experiments,
     * otherwise configures the FacilitatorWindow to replay experiments and view configuration 
     * for those experiments.
     */
    public void connect() {
        id = dispatcher.connect(serverConfiguration.getServerAddress());
        if (id != null) transmit(new FacilitatorRegistrationRequest(id));
    }
    
    public void setServerConfiguration(E configuration) {
        if (configuration == null) {
            System.err.println("attempt to setConfiguration with null, ignoring");
            return;
        } else {
            this.serverConfiguration = configuration;
        }
    }
    
    public Identifier getId() {
        return id;
    }
    
    public E getServerConfiguration() {
        return serverConfiguration;
    }
    
    public EventChannel getEventChannel() {
        return channel;
    }
    
    public void addEventProcessor(EventProcessor<? extends Event> processor) {
        channel.add(this, processor);
    }
}
