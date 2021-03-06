package edu.asu.commons.conf;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import edu.asu.commons.net.ServerDispatcher;

/**
 * $Id$
 * 
 * All experiment server configurations should follow this contract.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface ExperimentConfiguration<C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>>
        extends Configuration, Iterable<R> {

    public List<R> getAllParameters();

    public void setAllParameters(List<R> allParameters);

    public R getCurrentParameters();

    public int getNumberOfRounds();
    public int getTotalNumberOfRounds();
    
    public int getCurrentRoundIndex();

    public String getCurrentRoundLabel();

    /**
     * Advances to the next round.
     * 
     * @return
     */
    public R nextRound();

    /**
     * Returns the previous round's configuration without modification.
     */
    public R getPreviousRoundConfiguration();

    /**
     * Returns the next round configuration without modification.
     * 
     * @return
     */
    public R getNextRoundConfiguration();

    public int getRoundNumber(R roundConfiguration);
    
    public int getRoundIndex(R roundConfiguration);

    public int getCurrentRoundNumber();

    public String getServerName();

    public void setServerName(String serverName);

    public int getServerPort();

    public void setServerPort(int serverPort);

    public String getFacilitatorInstructions();

    public InetSocketAddress getServerAddress();

    public String getPersistenceDirectory();

    public String getProperty(String key);

    public String getProperty(String key, String defaultValue);

    public int getIntProperty(String key);

    public double getDoubleProperty(String key);

    public boolean getBooleanProperty(String key);

    public PersistenceType getPersistenceType();

    public boolean isFirstRound();

    public boolean isLastRound();

    public int getCurrentRepeatedRoundIndex();

    public ServerDispatcher.Type getServerDispatcherType();

    public int getWorkerPoolSize();

    public String getLogFileDestination();

    public Locale getLocale();

    public Properties getProperties();

    public int getDefaultRoundDuration();

    public void reset();

    public static abstract class Base<C extends ExperimentConfiguration<C, E>, E extends ExperimentRoundParameters<C, E>>
            extends Configuration.Base implements ExperimentConfiguration<C, E> {

        private static final String DEFAULT_LOGFILE_DESTINATION = "experiment-server.log";
        private final static long serialVersionUID = 8936075404166796486L;
        public static String defaultConfigurationDirectory;

        private final static String DEFAULT_CONF_DIR = "conf/";
        private final static String CONFIGURATION_DIRECTORY_KEY = "conf.dir";
        private final static String DEFAULT_SERVER_CONFIGURATION_FILENAME = "server.xml";

        static {
            try {
                String dir = System.getProperty(CONFIGURATION_DIRECTORY_KEY);
                if (dir == null || dir.equals("")) {
                    defaultConfigurationDirectory = DEFAULT_CONF_DIR;
                }
                else {
                    defaultConfigurationDirectory = dir;
                }
            } catch (SecurityException ignored) {
                defaultConfigurationDirectory = DEFAULT_CONF_DIR;
            }
        }

        private int currentRepeatedRoundIndex = 0; // repeated round counter, move to next round when it == repeat
        private int currentRoundIndex = 0; // index into the round parameter list
        private int currentRoundNumber = 0; // integer sequential round index, e.g., 1, 2, 3, 4, 5, 6, ...
        // protected final PropertiesConfiguration assistant;
        protected final List<E> allParameters = new ArrayList<>();
        protected String configurationDirectory;
        private Locale locale;
        private int numberOfPracticeRounds = -1;

        public Base() {
            this(defaultConfigurationDirectory);
        }

        public Base(String configurationDirectory) {
            if (configurationDirectory == null) {
                configurationDirectory = "";
            }
            else {
                if (!configurationDirectory.endsWith("/")) {
                    configurationDirectory = configurationDirectory.concat("/");
                }
            }
            this.configurationDirectory = configurationDirectory;
            loadServerProperties();
        }

        @Deprecated
        protected E createConfiguration(String roundConfigurationResource) {
            return createRoundConfiguration(roundConfigurationResource);
        }

        protected abstract E createRoundConfiguration(String roundConfigurationResource);

        /**
         * Override if you want to use a different server configuration file.
         */
        protected String getServerConfigurationFilename() {
            return DEFAULT_SERVER_CONFIGURATION_FILENAME;
        }

        private void loadServerProperties() {
            String configurationResource = getConfigurationDirectory() + getServerConfigurationFilename();
            loadProperties(configurationResource);
            loadParameters();
        }

        @SuppressWarnings("unchecked")
        private void loadParameters() {
            for (int roundNumber = 0; roundNumber < getNumberOfRounds(); roundNumber++) {
                String roundConfigurationResource = getConfigurationDirectory() + getRoundParametersFile(roundNumber);
                if (roundConfigurationResource == null || "".equals(roundConfigurationResource)) {
                    System.err.println("no round configuration available: " + roundNumber);
                    continue;
                }
                E configuration = createRoundConfiguration(roundConfigurationResource);
                configuration.setParentConfiguration((C) this);
                allParameters.add(configuration);
            }
        }

        public InetSocketAddress getServerAddress() {
            return new InetSocketAddress(getServerName(), getServerPort());
        }

        public int getServerPort() {
            return getIntProperty("port", 23732);
        }

        public String getServerName() {
            return getStringProperty("hostname", "localhost");
        }

        public void setServerName(String serverName) {
            setProperty("hostname", serverName);
        }

        public void setServerPort(int serverPort) {
            setProperty("port", String.valueOf(serverPort));
        }

        public String getRoundParametersFile(int roundNumber) {
            return getStringProperty("round" + roundNumber);
        }

        /**
         * Returns the number of round configuration files manually specified in this server.xml, not including repeated
         * rounds.
         */
        public int getNumberOfRounds() {
            return getIntProperty("number-of-rounds", 0);
        }

        /**
         * Returns the total number of rounds in this server.xml, including repeated rounds.
         */
        public int getTotalNumberOfRounds() {
            int numberOfRounds = getIntProperty("number-of-rounds", 0);
            for (E roundParameter: allParameters) {
                numberOfRounds += roundParameter.getRepeat();
            }
            return numberOfRounds;
        }

        /**
         * Returns a 1-based round number for the given round configuration, ignoring practice rounds.
         */
        public int getRoundNumber(E roundParameter) {
            return allParameters.indexOf(roundParameter) - getPracticeRoundOffset();
        }

        public String getCurrentRoundLabel() {
            return getCurrentParameters().getRoundIndexLabel();
        }
        
        public int getRoundIndex(E roundParameter) {
            return allParameters.indexOf(roundParameter);
        }

        public int getPracticeRoundOffset() {
            return getNumberOfPracticeRounds() - 1;
        }

        public int getNumberOfPracticeRounds() {
            if (numberOfPracticeRounds == -1) {
                numberOfPracticeRounds = 0;
                for (E roundParameter : allParameters) {
                    if (roundParameter.isPracticeRound()) {
                        numberOfPracticeRounds++;
                        if (roundParameter.isRepeatingRound()) {
                            numberOfPracticeRounds += roundParameter.getRepeat();
                        }
                    }
                }
            }
            return numberOfPracticeRounds;
        }

        public String getFacilitatorInstructions() {
            return render(getStringProperty("facilitator-instructions", "No facilitator instructions available."));
        }

        public List<E> getAllParameters() {
            return new ArrayList<E>(allParameters);
        }

        public void setAllParameters(List<E> incomingParameters) {
            allParameters.clear();
            allParameters.addAll(incomingParameters);
            currentRoundIndex = 0;
        }

        public int getCurrentRoundIndex() {
            return currentRoundIndex;
        }
        
        public int getCurrentRoundNumber() {
            return currentRoundNumber;
        }

        public E getCurrentParameters() {
            return allParameters.get(currentRoundIndex);
        }

        public boolean isFirstRound() {
            return currentRoundIndex == 0;
        }

        public boolean isLastRound() {
            if (currentRoundIndex + 1 == getNumberOfRounds()) {
                E currentParameters = getCurrentParameters();
                if (currentParameters.isRepeatingRound()) {
                    return getCurrentRepeatedRoundIndex() + 1 == currentParameters.getRepeat();
                }
                return true;
            }
            return false;
        }

        public Map<String, Object> toMap() {
            return toMap(this);
        }

        /**
         * Returns an Iterator over this ExperimentConfiguration's round parameters. Not thread-safe and currently only
         * one iterator can be safely active at a time for a given ExperimentConfiguration instance, due to
         * an unfortunate enclosing instance field dependencies on the iterator's enclosing ExperimentConfiguration.
         * Mainly this is so we can continue to ask the ExperimentConfiguration / ExperimentRoundParameters for an
         * appropriate round index label (e.g., 1.17 for repeating round 17 of round 1).
         * @return an Iterator over this ExperimentConfiguration's round parameters.
         */
        public Iterator<E> iterator() {
            return new ExperimentRoundParametersIterator();
        }

        /**
         * XXX: re-enable if dependencies between this and enclosing ExperimentConfiguration get cleaner. need to move
         * round label generation elsewhere
         */
        private class ExperimentRoundParametersIterator implements Iterator<E> {
            private int currentRoundIndex = 0;
            private int cursor = 0;
            private int numberOfRounds = getNumberOfRounds();
            private int size = getTotalNumberOfRounds();
            private E lastReturned = null;
            @Override
            public boolean hasNext() {
                return cursor < size;
            }
            @Override
            public E next() {
                E currentParameters = allParameters.get(currentRoundIndex);
                if (cursor >= size) {
                    throw new NoSuchElementException();
                }
                if (lastReturned != null) {
                    if (currentParameters.isRepeatingRound()) {
                        if ((currentRepeatedRoundIndex + 1) < currentParameters.getRepeat()) {
                            currentRepeatedRoundIndex++;
                        }
                        else {
                            currentRepeatedRoundIndex = 0;
                            currentRoundIndex++;
                            if (currentRoundIndex < numberOfRounds) {
                                currentParameters = allParameters.get(currentRoundIndex);
                            }
                        }
                    }
                    else {
                        currentRoundIndex++;
                    }
                }
                cursor++;
                lastReturned = currentParameters;
                return currentParameters;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove experiment round parameters via iteration");
            }
        }

        public E getPreviousRoundConfiguration() {
            return allParameters.get(Math.max(0, currentRoundIndex - 1));
        }

        public E getNextRoundConfiguration() {
            E currentParameters = getCurrentParameters();
            if (currentParameters.isRepeatingRound()) {
                if (currentRepeatedRoundIndex < currentParameters.getRepeat()) {
                    return currentParameters;
                }
            }
            if (isLastRound()) {
                return currentParameters;
            }
            return allParameters.get(currentRoundIndex + 1);
        }

        public int getCurrentRepeatedRoundIndex() {
            return currentRepeatedRoundIndex;
        }

        /**
         * Returns the next round configuration.
         * If we're at the last round, returns the last round configuration.
         */
        public synchronized E nextRound() {
            E currentParameters = getCurrentParameters();
            currentRoundNumber++;
            if (currentParameters.isRepeatingRound()) {
                if (currentRepeatedRoundIndex + 1 < currentParameters.getRepeat()) {
                    currentRepeatedRoundIndex++;
                    return currentParameters;
                }
            }
            if (isLastRound()) {
                return currentParameters;
            }
            currentRoundIndex++;
            currentRepeatedRoundIndex = 0;
            return allParameters.get(currentRoundIndex);
        }

        // resets all state variables, starting this ExperimentConfiguration over at the first round.
        public void reset() {
            currentRoundIndex = 0;
            currentRepeatedRoundIndex = 0;
        }

        public String getConfigurationDirectory() {
            return configurationDirectory;
        }

        public String getPersistenceDirectory() {
            return getStringProperty("save-dir", "experiment-data");
        }

        public PersistenceType getPersistenceType() {
            String persistenceType = getStringProperty("persistence-type", "ALL");
            return PersistenceType.valueOf(persistenceType);
        }

        public ServerDispatcher.Type getServerDispatcherType() {
            return ServerDispatcher.Type.fromString(getProperty("server-dispatcher-type", "SOCKET"));
        }

        public int getWorkerPoolSize() {
            return getIntProperty("worker-pool-size", 5);
        }

        public int getDefaultRoundDuration() {
            return getIntProperty("default-round-duration", 240);
        }

        public String getLogFileDestination() {
            return getStringProperty("log", getDefaultLogFileDestination());
        }

        /**
         * The default log file destination for a particular class of experiments (e.g., foraging-server.log, irrigation-server.log)
         * 
         * @return
         */
        protected String getDefaultLogFileDestination() {
            return DEFAULT_LOGFILE_DESTINATION;
        }

        public Locale getLocale() {
            if (locale == null) {
                locale = new Locale(getProperty("language", "en"), getProperty("country", "US"));
            }
            return locale;
        }
    }

}
