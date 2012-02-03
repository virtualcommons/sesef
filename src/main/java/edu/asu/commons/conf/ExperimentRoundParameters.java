package edu.asu.commons.conf;

import java.io.PrintStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
        private final PropertiesConfiguration assistant = new PropertiesConfiguration();

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
        
        protected PropertiesConfiguration getConfigurationAssistant() {
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
        
        public int getDuration() {
            return assistant.getIntProperty("duration", getDefaultRoundDuration()); 
        }
        
        public Duration getRoundDuration() {
            return Duration.create(getDuration());
        }
        

        public Map<String, Object> toMap() {
        	return assistant.toMap(this);
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
            return assistant.getBooleanProperty(key, parentConfiguration.getBooleanProperty(key));
        }

        public double getDoubleProperty(String key, double defaultValue) {
            return assistant.getDoubleProperty(key, defaultValue);
        }

        public double getDoubleProperty(String key) {
            return assistant.getDoubleProperty(key, parentConfiguration.getDoubleProperty(key));
        }

        public int getIntProperty(String key, int defaultValue) {
            return assistant.getIntProperty(key, defaultValue);
        }

        public int getIntProperty(String key) {
            return assistant.getIntProperty(key, parentConfiguration.getIntProperty(key));
        }

        public Properties getProperties() {
            return assistant.getProperties();
        }

        public String getStringProperty(String key, String defaultValue) {
            return assistant.getStringProperty(key, defaultValue);
        }

        public String getStringProperty(String key) {
            return assistant.getStringProperty(key, parentConfiguration.getProperty(key));
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
            ST st = assistant.createStringTemplate(template);
            st.add("self", this);
            return st;
        }
        
        public long inMinutes(long seconds) {
            return TimeUnit.MINUTES.convert(seconds, TimeUnit.SECONDS);
        }
        
        public long inMinutes(Duration duration) {
            return inMinutes(duration.getTimeLeftInSeconds());
        }
        
        public String toCurrencyString(double amount) {
            return NumberFormat.getCurrencyInstance().format(amount);
        }

    }
}
