package edu.asu.commons.experiment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.event.RoundEndedMarkerEvent;

/**
 * $Id$
 * 
 * Base class responsible for persistence strategies.
 * 
 * TODO: clean up path generation for the files associated with different persistence types.. currently
 * it is an ad-hoc mess.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 * @param <C>
 * @param <R>
 */

public abstract class Persister<C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>> implements IPersister<C, R> {

    private final static String ROUND_SAVE_DIRECTORY_FORMAT = "MM-dd-yyyy" + File.separator + "HH.mm.ss";
    private final static String DEFAULT_EXPERIMENT_CONFIGURATION_FILE = "experiment-configuration.save";
    private final static String DEFAULT_CHAT_LOG_FILE_NAME = "chat.log";

    private final static Logger logger = Logger.getLogger(Experiment.class.getName());

    private final Logger chatLogger = Logger.getLogger("chat.logger");
    private FileHandler chatLogFileHandler;

    private R roundConfiguration;

    private final SortedSet<PersistableEvent> actions = new TreeSet<PersistableEvent>();
    private final SortedSet<ChatRequest> chatRequests = new TreeSet<ChatRequest>();
    private String experimentSaveDirectory;
    private String persistenceDirectory;
    private EventChannel channel;

    private final XStream xstream = new XStream();
    private boolean xmlEnabled;

    // private boolean usingEventChannel;

    public Persister(C experimentConfiguration) {
        this.persistenceDirectory = experimentConfiguration.getPersistenceDirectory();
        // initialize persister with first round parameters
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ROUND_SAVE_DIRECTORY_FORMAT);
        this.experimentSaveDirectory = simpleDateFormat.format(new Date());
        initialize(experimentConfiguration.getCurrentParameters());
        saveExperimentConfiguration(experimentConfiguration);
        initializeChatLogFileHandler();
    }

    private String getXmlSaveFilePath() {
        return String.format("%s%s%d-round-save.xml", getDefaultSavePath(), File.separator, roundConfiguration.getRoundIndex());
        // return getDefaultSavePath() + File.separator + roundConfiguration.getRoundNumber() + ".save.xml";
    }

    public Persister(EventChannel channel, C experimentConfiguration) {
        this(experimentConfiguration);
        if (channel == null) {
            throw new NullPointerException("Cannot register persister with a null event channel");
        }
        this.channel = channel;
        // usingEventChannel = true;
        channel.add(this, new EventTypeProcessor<PersistableEvent>(PersistableEvent.class, true) {
            @Override
            public void handle(PersistableEvent event) {
                store(event);
            }
        });
    }

    private void initializeChatLogFileHandler() {
        // FIXME: make it so that this will try the failsafe path as well.
        String chatLogPath = getDefaultSavePath() + File.separator + DEFAULT_CHAT_LOG_FILE_NAME;
        try {
            chatLogFileHandler = new FileHandler(chatLogPath);
            chatLogger.addHandler(chatLogFileHandler);
            chatLogger.setLevel(Level.ALL);
        } catch (IOException recoverable) {
            logger.warning("Couldn't create chat log at: " + chatLogPath + " - trying fail safe save directory.");
            try {
                chatLogPath = getSavePath(getFailSafeSaveDirectory()) + File.separator + DEFAULT_CHAT_LOG_FILE_NAME;
                chatLogFileHandler = new FileHandler(chatLogPath);
                chatLogger.addHandler(chatLogFileHandler);
                chatLogger.setLevel(Level.ALL);
            } catch (IOException unrecoverable) {
                unrecoverable.printStackTrace();
                logger.severe("Unable to initialize chat log: " + chatLogPath);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.asu.commons.experiment.IPersister#stop()
     */
    @Override
    public void stop() {
        channel.remove(this);
        chatLogger.removeHandler(chatLogFileHandler);
        chatLogFileHandler.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.asu.commons.experiment.IPersister#getActions()
     */
    @Override
    public SortedSet<PersistableEvent> getActions() {
        return Collections.unmodifiableSortedSet(actions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.asu.commons.experiment.IPersister#store(edu.asu.commons.event.PersistableEvent)
     */
    @Override
    public void store(PersistableEvent event) {
        // XXX: timestamp before acquiring the lock to ensure
        // as-close-to-invoked-time-accuracy
        event.timestamp();
        synchronized (actions) {
            actions.add(event);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.asu.commons.experiment.IPersister#clearChatData()
     */
    @Override
    public void clearChatData() {
        chatRequests.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.asu.commons.experiment.IPersister#store(edu.asu.commons.event.ChatRequest)
     */
    @Override
    public void store(ChatRequest request) {
        request.timestamp();
        // FIXME: right now all cumulative chat requests are stored in each round file. Should either
        // switch to one set of chats per round, or a single chat file, as in the chatLogger. There
        // is some difficulty in figuring out when exactly to clear out all the old chat requests in a
        // flexible manner. Could probably do it when the BeginCommunicationRequest is handled, actually.
        synchronized (chatRequests) {
            chatRequests.add(request);
        }
        chatLogger.log(Level.ALL,
                String.format("%s, %s, %s, %s",
                        request.getCreationTime(),
                        request.getSource(),
                        request.getTarget(),
                        request.toString()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.asu.commons.experiment.IPersister#initialize(R)
     */
    @Override
    public final void initialize(R roundConfiguration) {
        clear();
        this.roundConfiguration = roundConfiguration;
        xmlEnabled = roundConfiguration.getParentConfiguration().getPersistenceType().isXmlEnabled();
    }

    /**
     * 
     * FIXME: should we also expose a SavedExperimentData object, consists of an ExperimentConfiguration and a List<SavedRoundData>?
     * 
     * @param directory
     * @param processors
     */
    public static void processSaveFiles(File directory, List<SaveFileProcessor> processors) {
        if (directory == null || !directory.isDirectory()) {
            logger.warning("Tried to restore a non-directory: " + directory);
            return;
        }
        File[] subdirectories = directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (subdirectories.length == 0) {
            processSaveDirectory(directory, processors);
        }
        else {
            for (File subdirectory : subdirectories) {
                // recur on the subdirectory.
                processSaveFiles(subdirectory, processors);
            }
        }
    }

    /**
     * Processes the actual directory containing the save files.
     * 
     * @param directory
     * @param processors
     */
    private static void processSaveDirectory(File directory, List<SaveFileProcessor> processors) {
        try {
            int numberOfRounds = restoreExperimentConfiguration(directory).getAllParameters().size();
            for (int roundNumber = 0; roundNumber < numberOfRounds; roundNumber++) {
                SavedRoundData savedRoundData = restoreSavedRoundData(directory, roundNumber);
                String roundSaveFilePath = savedRoundData.getSaveFilePath();
                try {
                    for (SaveFileProcessor processor : processors) {
                        processor.process(savedRoundData, roundSaveFilePath);
                    }
                } catch (RuntimeException e) {
                    logger.log(Level.SEVERE, "Error while processing [file:" + roundSaveFilePath + "]- ignoring", e);
                }
            }
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error while processing [save directory: " + directory + "] - ignoring.", e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static ExperimentConfiguration restoreExperimentConfiguration(File saveDirectory) {
        return restoreExperimentConfiguration(saveDirectory, DEFAULT_EXPERIMENT_CONFIGURATION_FILE);
    }

    @SuppressWarnings("rawtypes")
    public static ExperimentConfiguration restoreExperimentConfiguration(File saveDirectory, String experimentConfigurationFilename) {
        if (experimentConfigurationFilename == null || "".equals(experimentConfigurationFilename)) {
            experimentConfigurationFilename = DEFAULT_EXPERIMENT_CONFIGURATION_FILE;
        }
        ObjectInputStream stream = null;
        try {
            stream = new ObjectInputStream(new FileInputStream(saveDirectory + File.separator + experimentConfigurationFilename));
            return (ExperimentConfiguration) stream.readObject();
        } catch (InvalidClassException e) {
            e.printStackTrace();
            // write out a temporary file in the experiment configuration directory stating that the file
            // needs a different version?
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.severe("unable to find ExperimentConfiguration class: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.asu.commons.experiment.IPersister#persist(E)
     */
    @Override
    public final <E extends DataModel<C, R>> void persist(E serverDataModel) {
        // shouldn't need to synchronize but just to be sure / threadsafe
        synchronized (actions) {
            actions.add(new RoundEndedMarkerEvent());
        }
        save(serverDataModel);
    }

    private final <E extends DataModel<C, R>> void save(E serverDataModel) {
        try {
            saveRound(serverDataModel, persistenceDirectory);
        } catch (FileNotFoundException recoverable) {
            try {
                saveRound(serverDataModel, getFailSafeSaveDirectory());
            } catch (IOException unrecoverable) {
                // now really give up
                unrecoverable.printStackTrace();
                throw new RuntimeException(
                        "Unable to save to the failsafe directory: " + getFailSafeSaveDirectory(), unrecoverable);
            }

        } catch (IOException unrecoverable) {
            unrecoverable.printStackTrace();
            throw new RuntimeException("Couldn't save this round");
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.asu.commons.experiment.IPersister#clear()
     */
    @Override
    public final void clear() {
        synchronized (actions) {
            actions.clear();
        }
    }

    public static String getRoundSaveFilePath(String directory, int roundNumber) {
        return String.format("%s%sround-%d.save", directory, File.separator, roundNumber);
        // return String.format("%s%s%d-round-save.xml", directory, File.separator, roundNumber);
    }

    public static SavedRoundData restoreSavedRoundData(File directory, int roundNumber) {
        if (roundNumber < 0) {
            throw new IllegalArgumentException("Invalid round number: " + roundNumber);
        }
        if (!directory.exists()) {
            throw new IllegalArgumentException("Directory " + directory.getAbsolutePath() + " does not exist.");
        }
        String roundSaveFilePath = getRoundSaveFilePath(directory.getAbsolutePath(), roundNumber);
        // how best to flip between binary and XML? The round save file path is different for both..

        return SavedRoundData.create(roundSaveFilePath);
    }

    protected abstract String getFailSafeSaveDirectory();

    protected String getExperimentConfigurationSaveFileName() {
        return DEFAULT_EXPERIMENT_CONFIGURATION_FILE;
    }

    private void saveExperimentConfiguration(C experimentConfiguration) {
        try {
            doSaveExperimentConfiguration(experimentConfiguration, persistenceDirectory);
        } catch (FileNotFoundException recoverable) {
            // FIXME: this is duplicated across the regular save() as well,
            // see if we can extract the algorithm out.
            recoverable.printStackTrace();
            try {
                doSaveExperimentConfiguration(experimentConfiguration, getFailSafeSaveDirectory());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to save to failsafe directory: " + getFailSafeSaveDirectory(), e);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to save experiment configuration to persistenceDirectory:" + persistenceDirectory, e);
        }
    }

    private void doSaveExperimentConfiguration(C experimentConfiguration, String persistenceDirectory) throws IOException {
        ObjectOutputStream objectOutputStream = null;
        String savePath = getSavePath(persistenceDirectory);
        try {
            createDirectoryIfNeeded(savePath);
            String configurationSavePath = savePath + File.separator + getExperimentConfigurationSaveFileName();
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(configurationSavePath));
            objectOutputStream.writeObject(experimentConfiguration);
            objectOutputStream.flush();
            // save a copy as XML as well
            objectOutputStream = xstream.createObjectOutputStream(new FileOutputStream(configurationSavePath + ".xml"));
            objectOutputStream.writeObject(experimentConfiguration);
            objectOutputStream.flush();
        } finally {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
        }
    }

    private <E extends DataModel<C, R>> void saveRound(E serverDataModel, String persistenceDirectory) throws IOException {
        String saveDestination = getSavePath(persistenceDirectory);
        logger.info("saving to " + saveDestination);
        ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(getRoundSaveFilePath(saveDestination, roundConfiguration.getRoundNumber())));
        try {
            oos.writeObject(roundConfiguration);
            oos.writeObject(serverDataModel);
            synchronized (actions) {
                oos.writeObject(actions);
            }
            synchronized (chatRequests) {
                oos.writeObject(chatRequests);
            }
            oos.flush();
        } finally {
            if (oos != null)
                oos.close();
        }
        try {
            if (xmlEnabled) {
                oos = xstream.createObjectOutputStream(new FileOutputStream(getXmlSaveFilePath()));
                oos.writeObject(roundConfiguration);
                oos.writeObject(serverDataModel);
                synchronized (actions) {
                    oos.writeObject(actions);
                }
                synchronized (chatRequests) {
                    oos.writeObject(chatRequests);
                }
                oos.flush();
            }
        } finally {
            if (oos != null)
                oos.close();
        }
    }

    private void createDirectoryIfNeeded(String directoryName) {
        File file = new File(directoryName);
        // try to create the directory if it doesn't already exist.
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                file.mkdirs();
            }
        }
    }

    private String getDefaultSavePath() {
        return getSavePath(persistenceDirectory);
    }

    /**
     * Returns the experiment data directory concatenated with the current run's experiment directory.
     * 
     * @param persistenceDirectory
     * @return
     */
    private String getSavePath(String persistenceDirectory) {
        return persistenceDirectory + File.separator + experimentSaveDirectory;
    }
}
