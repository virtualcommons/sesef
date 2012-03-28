package edu.asu.commons.conf;

import java.io.PrintStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import edu.asu.commons.util.Duration;

/**
 * $Id$
 * 
 * Per-round experimental parameters for a given Experiment server instance.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface ExperimentRoundParameters<T extends ExperimentConfiguration<T, R>, R extends ExperimentRoundParameters<T, R>>
        extends Serializable {

    public String getInstructions();

    public T getParentConfiguration();

    public void setParentConfiguration(T configuration);

    public Duration getRoundDuration();

    /**
     * Returns the displayable round number for this set of round parameters.
     * FIXME: what to do about practice rounds?
     * @return
     */
    public int getRoundNumber();
    
    /**
     * Returns the internal index for the given round.
     * @return
     */
    public int getRoundIndex();
    
    public Properties getProperties();

    public boolean isPracticeRound();

    public static abstract class Base<E extends ExperimentConfiguration<E, P>, P extends ExperimentRoundParameters<E, P>>
            extends Configuration.Base implements ExperimentRoundParameters<E, P> {
        private static final long serialVersionUID = -7904104481473406817L;

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
            loadProperties(resource);
        }

        @SuppressWarnings("unchecked")
        public int getRoundNumber() {
            if (isPracticeRound()) {
                return 0;
            }
            return parentConfiguration.getRoundNumber((P) this);
        }
        
        @SuppressWarnings("unchecked")
        public int getRoundIndex() {
            return parentConfiguration.getRoundIndex((P) this);
        }

        @Override
        public String getInstructions() {
            return render(getProperty("instructions"));
        }

        public void report() {
            report(System.err);
        }

        public void report(PrintStream stream) {
            getProperties().list(stream);
        }

        public String getResourceFilename() {
            return resource;
        }

        public E getParentConfiguration() {
            return parentConfiguration;
        }

        public int getDuration() {
            return getIntProperty("duration", getDefaultRoundDuration());
        }

        public Duration getRoundDuration() {
            return Duration.create(getDuration());
        }

        public Map<String, Object> toMap() {
            return toMap(this);
        }

        /**
         * Override to set up a different default round duration.
         * 
         * @return
         */
        protected int getDefaultRoundDuration() {
            return 240;
        }

        public void setParentConfiguration(E parentConfiguration) {
            this.parentConfiguration = parentConfiguration;
        }

        public boolean getBooleanProperty(String key) {
            return getBooleanProperty(key, parentConfiguration.getBooleanProperty(key));
        }

        public double getDoubleProperty(String key) {
            return getDoubleProperty(key, parentConfiguration.getDoubleProperty(key));
        }

        public int getIntProperty(String key) {
            return getIntProperty(key, parentConfiguration.getIntProperty(key));
        }

        public String getStringProperty(String key) {
            return getStringProperty(key, parentConfiguration.getProperty(key));
        }

        public void setProperty(String key, String value) {
            super.setProperty(key, value);
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

        public boolean isPracticeRound() {
            return getBooleanProperty("practice-round", false);
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
