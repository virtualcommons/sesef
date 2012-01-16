package edu.asu.commons.conf;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.stringtemplate.v4.ST;

/**
 * $Id$
 * 
 * All experiment server configurations should follow this contract.
 * 
 * FIXME: properly genericize ExperimentRoundParameters circular types...
 * something like
 * ExperimentConfiguration<T extends ExperimentRoundParameters<this-type>>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@SuppressWarnings("rawtypes")
public interface ExperimentConfiguration<T extends ExperimentRoundParameters> extends Serializable, Iterable<T> {

    public List<T> getAllParameters();

    public void setAllParameters(List<T> allParameters);

    public T getCurrentParameters();

    public int getNumberOfRounds();

    /**
     * Advances to the next round.
     * @return
     */
    public T nextRound();
    
    /**
     * Returns the previous round's configuration without modification.
     */
    public T getPreviousRoundConfiguration();
    
    /**
     * Returns the next round configuration without modification.
     * @return
     */
    public T getNextRoundConfiguration();

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

    public static abstract class Base<E extends ExperimentRoundParameters> implements ExperimentConfiguration<E> {

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

        private int currentRoundIndex = 0;
        protected final ConfigurationAssistant assistant;
        protected final List<E> allParameters = new ArrayList<E>();
        protected String configurationDirectory;

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
            assistant = new ConfigurationAssistant();
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
            assistant.loadProperties(configurationResource);
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
                configuration.setParentConfiguration(this);
                allParameters.add(configuration);
            }
        }

        public InetSocketAddress getServerAddress() {
            return new InetSocketAddress(getServerName(), getServerPort());
        }

        public int getServerPort() {
            return assistant.getIntProperty("port", 23732);
        }

        public String getServerName() {
            return assistant.getStringProperty("hostname", "localhost");
        }

        public void setServerName(String serverName) {
            assistant.setProperty("hostname", serverName);
        }

        public void setServerPort(int serverPort) {
            assistant.setProperty("port", String.valueOf(serverPort));
        }

        public String getRoundParametersFile(int roundNumber) {
            return assistant.getStringProperty("round" + roundNumber);
        }

        public int getNumberOfRounds() {
            return assistant.getIntProperty("number-of-rounds", 0);
        }

        public String getFacilitatorInstructions() {
            return assistant.getStringProperty("facilitator-instructions", "No facilitator instructions available.");
        }

        public List<E> getAllParameters() {
            return allParameters;
        }

        public void setAllParameters(List<E> incomingParameters) {
            allParameters.clear();
            allParameters.addAll(incomingParameters);
            currentRoundIndex = 0;
        }

        public int getCurrentRoundNumber() {
            return currentRoundIndex;
        }

        public E getCurrentParameters() {
            return allParameters.get(currentRoundIndex);
        }

        public boolean isFirstRound() {
            return currentRoundIndex == 0;
        }

        public boolean isLastRound() {
            return currentRoundIndex == (getNumberOfRounds() - 1);
        }
        
        public Map<String, Object> toMap() {
        	return assistant.toMap(this);
        }
        
        public Iterator<E> iterator() {
            return allParameters.iterator();
        }
        
        public ListIterator<E> listIterator() {
            return allParameters.listIterator();
        }
        
        public E getPreviousRoundConfiguration() {
            return allParameters.get(Math.max(0, currentRoundIndex - 1));
        }
        
        public E getNextRoundConfiguration() {
            if (isLastRound()) {
                return getCurrentParameters();
            }
            return allParameters.get(currentRoundIndex + 1);
        }

        /**
         * Returns the next round configuration.
         * If we're at the last round, returns the last round configuration.
         */
        public E nextRound() {
            if (isLastRound()) {
                return getCurrentParameters();
            }
            return allParameters.get(++currentRoundIndex);
        }

        public String getConfigurationDirectory() {
            return configurationDirectory;
        }

        public String getPersistenceDirectory() {
            return assistant.getStringProperty("save-dir", "experiment-data");
        }

        public PersistenceType getPersistenceType() {
            String persistenceType = assistant.getStringProperty("persistence-type", "ALL");
            return PersistenceType.valueOf(persistenceType);
        }

        public String getProperty(String key) {
            return assistant.getProperty(key);
        }
        
        public String getProperty(String key, String defaultValue) {
            return assistant.getProperty(key, defaultValue);
        }
        
        public int getIntProperty(String key) {
            return assistant.getIntProperty(key);
        }
        
        public double getDoubleProperty(String key) {
            return assistant.getDoubleProperty(key);
        }
        
        public boolean getBooleanProperty(String key) {
            return assistant.getBooleanProperty(key);
        }
        
        public ST createStringTemplate(String template) {
            return assistant.createStringTemplate(template);
        }

    }

}
