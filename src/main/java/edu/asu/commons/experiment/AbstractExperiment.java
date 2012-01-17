package edu.asu.commons.experiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import edu.asu.commons.command.Command;
import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventChannelFactory;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.net.DispatcherFactory;
import edu.asu.commons.net.ServerDispatcher;

/**
 * $Id: AbstractExperiment.java 454 2010-02-04 04:17:29Z alllee $
 *
 * Abstract base class for Experiments, providing convenience methods for 
 * obtaining an ExperimentConfiguration of the appropriate type, registering
 * with an ExperimentService specified via Spring, 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a> 
 * @version $Revision: 454 $
 */
public abstract class AbstractExperiment<C extends ExperimentConfiguration<R>, R extends ExperimentRoundParameters<C>> implements Experiment<C, R> {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final EventChannel channel;
    private final ServerDispatcher dispatcher;
    
    private final List<Command> commands = new LinkedList<Command>();
    
    private Thread serverThread;

    private boolean running;
    
    private C configuration;
    
    private C defaultConfiguration;
    
    public AbstractExperiment() {
        this(EventChannelFactory.create());
    }
    
    public AbstractExperiment(EventChannel channel) {
        this(channel, DispatcherFactory.getInstance().createServerDispatcher(channel));
    }
    
    public AbstractExperiment(EventChannel channel, ServerDispatcher dispatcher) {
        this.channel = channel;
        this.dispatcher = dispatcher;
    }
    
    public C getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(C configuration) {
        if (configuration == null) {
            throw new NullPointerException("Invoked setConfiguration with a null configuration.");
        }
        if ( isRunning() ) {
            throw new UnsupportedOperationException("AbstractExperiment does " +
                    "not support reconfiguration while running yet");
        }
        this.configuration = configuration;
    }

    public C getDefaultConfiguration() {
        return defaultConfiguration;
    }

    public synchronized void start() {
        if ( isRunning() ) {
            getLogger().warning("Ignoring request to START an already running experiment.");
            Thread.dumpStack();
            // FIXME: could also start a new thread on a new port..?
            return;
        }
        getLogger().info("Starting " + toString());
        running = true;
        serverThread = createExperimentServerThread();
        serverThread.start();
        //eventHandlingThread.start();
    }
    
    public synchronized void stop() {
        getLogger().info("Trying to stop " + toString());
        if ( isRunning() ) {
            running = false;
            dispatcher.shutdown();
            serverThread = null;
        }
        else {
            getLogger().warning("Ignoring spurious request to STOP: " + toString());
        }
        getEventChannel().remove(this);
    }
    
    public int getServerPort() {
        if (configuration == null) {
            return -1;
        }
        return getConfiguration().getServerPort();
    }

    public Logger getLogger() {
        return logger;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public boolean isFull() {
        return false;
    }
    
    protected void transmit(Event event) {
        dispatcher.transmit(event);
    }   
    
    protected void transmitAndStore(PersistableEvent event) {
    	dispatcher.transmit(event);
    	getPersister().store(event);
    }
    
    /**
     * Asks getPersister() to store the given PersistableEvent.
     * @param event
     */
    protected void store(PersistableEvent event) {
    	getPersister().store(event);
    }
    
    protected EventChannel getEventChannel() {
        return channel;
    }
    
    /**
     * Subtypes should always use this to add an event processor in lieu of talking with the event channel
     * directly, as it ensures that the command queue can be used.  
     * 
     * @param processor
     */
    protected void addEventProcessor(EventTypeProcessor<? extends Event> processor) {
    	processor.setExperiment(this);
        channel.add(this, processor);
    }
    
    protected abstract StateMachine getStateMachine();
    
    public void schedule(Command command) {
    	synchronized (commands) {
            commands.add(command);
        }
    }
    
    protected void clearCommands() {
        synchronized (commands) {
            commands.clear();
        }
    }

    private Thread createExperimentServerThread() {
        return new Thread(new Runnable() {
            public void run() {
            	getLogger().info("Listening at: " + getConfiguration().getServerAddress());
                dispatcher.listen(getServerPort());
                getLogger().info("initializing state machine.");
                getStateMachine().initialize();
                getLogger().info("Starting state machine.");
                while ( isRunning() ) {
                    try {
                        // execute all queued up commands serially.
                        synchronized (commands) {
                            for (Iterator<Command> iterator = commands.iterator(); iterator.hasNext(); ) {
                                iterator.next().execute();
                                iterator.remove();
                            }
                        }
                        getStateMachine().execute(dispatcher);
                    }
                    catch (Exception exception) {
                        getLogger().severe("Attempting to recover from exception: " + exception);
                        getLogger().throwing(getClass().getName(), "experimentServerThread.run()", exception);
                        exception.printStackTrace();
                        continue;
                    }
                }
            }
        });
    }
    
    public void repl() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while (true) {
            System.out.print("> ");
            try {
                line = reader.readLine();
                // EOF gives null (Ctrl-D)
                if (line == null || "exit".equals(line.trim())) {
                    System.out.println("VC Server Console exiting.");
                    System.exit(0);
                }
                line = line.trim();
            } 
            catch (IOException e) {
                e.printStackTrace();
                line = "help";
            }
 
            if ("help".equals(line)) {
                System.out.println("\texit - exits");
                System.out.println("\tstart - start an experiment");
                System.out.println("\tstop - stop an experiment");
                System.out.println("\tclients - show connected clients");
                System.out.println("\tdump - dump current thread's stack");
                System.out.println("\tshow-threads - display all threads");
                System.out.println("\thelp - display this help");
                displayCustomHelp();
            } 
            else if ("dump".equals(line)) {
                Thread.dumpStack();
            }
            else if ("show-threads".equals(line)) {
                showThreads();
            }
            else if ("".equals(line)) {
                // ignore empty input
                continue;
            }
            else {
                processReplInput(line, reader);
            }
        }
    }
    
    private void showThreads() {
        // Find the root thread group
        ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        // Visit each thread group
        visitThreadGroup(root, 0);
    }

    // This method recursively visits all thread groups under `group'.
    private void visitThreadGroup(ThreadGroup group, int level) {
        // Get threads in `group'
        int numThreads = group.activeCount();
        Thread[] threads = new Thread[numThreads*2];
        numThreads = group.enumerate(threads, false);

        // Enumerate each thread in `group'
        for (Thread thread: threads) {
            if (thread == null) {
                continue;
            }
            for (StackTraceElement element : thread.getStackTrace()) {
                logger.info(thread.getName() + " - " + element);
            }
        }
        // Get thread subgroups of `group'
        int numGroups = group.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[numGroups*2];
        numGroups = group.enumerate(groups, false);
        for (ThreadGroup subGroup : groups) {
            visitThreadGroup(subGroup, level + 1);
        }
    }
    
    public void displayCustomHelp() {
        System.out.println("No more options available.");
    }
    
    public void processReplInput(String line, BufferedReader reader) {
        System.out.println("Unhandled input: [" + line + "] - overload processReplInput to handle these cases properly.");
    }
    
    public static void waitOn(final Object lock) {
        synchronized (lock) {
            try { 
                lock.wait(); 
            }
            catch (InterruptedException exception) { 
                exception.printStackTrace();
            }
        }
    }
    
    public static void notify(final Object lock) {
        synchronized (lock) {
            lock.notify();
        }
    }

    public static void sleep(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException ignored) { }
    }

}
