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
     * Returns a displayable round number for this round, 1-based.
     *
     * For practice rounds, returns 0.
     */
    public int getRoundNumber();

    /**
     * Returns a unique String label that can be used to identify the specific Round across repeated rounds.
     */
    public String getRoundIndexLabel();
    
    /**
     * Returns this round's 0-based integer index in the list of all round configurations maintained by its parent
     * configuration.
     */
    public int getRoundIndex();

    /**
     * Returns the number of times this RoundConfiguration should repeat before advancing to the next
     * RoundConfiguration.
     */
    public int getRepeat();

    /**
     * Returns true iff this RoundConfiguration should repeat.
     */
    public boolean isRepeatingRound();

    /**
     * Returns true iff this RoundConfiguration is the first out of a set of repeating rounds, e.g., if it's parent
     * configuration's current repeated round index is greater than 0.
     */
    public boolean isFirstRepeatingRound();
    
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

        /**
         * Returns a String representation of this round index, unique across repeated rounds. For non-repeated rounds,
         * this will simply be getRoundIndex() as a String. For repeated rounds this will be of the format n.m where n
         * is the round index, and m is the current repeated round index, e.g., 5.10 would be the 10th repeated round of
         * the 6th round configuration in the set of round configurations maintained by the parent experiment
         * configuration.
         */
        public String getRoundIndexLabel() {
            if (isRepeatingRound()) {
                return String.format("%d.%d", getRoundIndex(), parentConfiguration.getCurrentRepeatedRoundIndex());
            }
            else {
                return String.valueOf(getRoundIndex());
            }
        }

        public int getCurrentRoundNumber() {
            return parentConfiguration.getCurrentRoundNumber();
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
            return getIntProperty("duration", getParentConfiguration().getDefaultRoundDuration());
        }

        public boolean isFirstRepeatingRound() {
            return getParentConfiguration().getCurrentRepeatedRoundIndex() == 0;
        }

        public boolean isRepeatingRound() {
            return getRepeat() > 0;
        }

        public int getRepeat() {
            return getIntProperty("repeat", 0);
        }

        public Duration getRoundDuration() {
            return Duration.create(getDuration());
        }

        public Map<String, Object> toMap() {
            return toMap(this);
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
