package edu.asu.commons.conf;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Properties;

import org.stringtemplate.v4.ST;

import edu.asu.commons.util.Duration;

/**
 * $Id$
 *
 * Per-round experimental parameters for a given Experiment server instance.
 * 
 * FIXME: can we do mutually recursive generics properly here?
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@SuppressWarnings("rawtypes")
public interface ExperimentRoundParameters<T extends ExperimentConfiguration> extends Serializable {

    public String getInstructions();
    
    public T getParentConfiguration();
    public void setParentConfiguration(T configuration);
    
    public Duration getRoundDuration();
    
    public int getRoundNumber();
    
//    public Properties getProperties();
    
    public static abstract class Base<E extends ExperimentConfiguration> implements ExperimentRoundParameters<E> {
        private static final long serialVersionUID = -7904104481473406817L;
        private final ConfigurationAssistant assistant = new ConfigurationAssistant();
        private final String resource;
        private E parentConfiguration;
        
        public Base() {
            this.resource = null;
        }
        
        public Base(String resource) {
            if (resource == null || "".equals(resource)) {
                throw new IllegalArgumentException("null or empty resource name: " + resource);
            }
            this.resource = resource;
            assistant.loadProperties(resource);
        }
        
        public int getRoundNumber() {
            return getParentConfiguration().getAllParameters().indexOf(this);
        }
        
        protected ConfigurationAssistant getConfigurationAssistant() {
            return assistant;
        }
        
        public void report() {
            report(System.err);
        }
        
        public void report(PrintStream stream) {
            assistant.getProperties().list(stream);
        }
        
        public String getResourceFilename() {
            return resource;
        }

        public E getParentConfiguration() {
            return parentConfiguration;
        }
        
        public Duration getRoundDuration() {
            return Duration.create(assistant.getIntProperty("duration", getDefaultRoundDuration())); 
        }
        
        /**
         * Override to set up a different default round duration.
         * @return
         */
        protected int getDefaultRoundDuration() {
            return 240;
        }

        public void setParentConfiguration(E parentConfiguration) {
            this.parentConfiguration = parentConfiguration;
        }

        public boolean getBooleanProperty(String key, boolean defaultValue) {
            return assistant.getBooleanProperty(key, defaultValue);
        }

        public boolean getBooleanProperty(String key) {
            return assistant.getBooleanProperty(key);
        }

        public double getDoubleProperty(String key, double defaultValue) {
            return assistant.getDoubleProperty(key, defaultValue);
        }

        public double getDoubleProperty(String key) {
            return assistant.getDoubleProperty(key);
        }

        public int getIntProperty(String key, int defaultValue) {
            return assistant.getIntProperty(key, defaultValue);
        }

        public int getIntProperty(String key) {
            return assistant.getIntProperty(key);
        }

        public Properties getProperties() {
            return assistant.getProperties();
        }

        public String getStringProperty(String key, String defaultValue) {
            return assistant.getStringProperty(key, defaultValue);
        }

        public String getStringProperty(String key) {
            return assistant.getStringProperty(key);
        }

        public void setProperty(String key, String value) {
            assistant.setProperty(key, value);
        }

        public String getProperty(String key, String defaultValue) {
            return assistant.getProperty(key, defaultValue);
        }
        
        public String getProperty(String key) {
            return getProperty(key, parentConfiguration.getProperty(key));
        }
        public boolean isFirstRound() {
            return parentConfiguration.isFirstRound();
        }
        public boolean isLastRound() {
            return parentConfiguration.isLastRound();
        }
        
        public ST createStringTemplate(String template) {
            return assistant.createStringTemplate(template);
        }


    }
}
