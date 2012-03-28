package edu.asu.commons.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationTest {

    private PropertiesConfiguration assistant;

    @Before
    public void setUp() {
        assistant = new PropertiesConfiguration();
    }

    @Test
    public void testPropertiesConfiguration() {
        assertEquals(0.0d, assistant.getDoubleProperty("unbound", 0.0d), 0.0d);
        assertTrue(assistant.getBooleanProperty("unbound", true));
        assertFalse(assistant.getBooleanProperty("unbound", false));
        assertEquals("snowflake", assistant.getStringProperty("unbound", "snowflake"));
        assertEquals(10271, assistant.getIntProperty("unbound", 10271));
    }

    @Test
    public void testAsBeanPropertyMap() {
        MockServerConfiguration configuration = new MockServerConfiguration();
        MockRoundConfiguration roundConfiguration = configuration.getCurrentParameters();
        Map<String, Object> roundConfigAsMap = roundConfiguration.toMap();
        assertEquals(roundConfiguration.getDuration(), roundConfigAsMap.get("duration"));
        assertEquals(roundConfiguration.getClientsPerGroup(), roundConfigAsMap.get("clientsPerGroup"));
        assertEquals(roundConfiguration.getInstructions(), roundConfigAsMap.get("instructions"));
    }

    @Test
    public void testRoundNumbering() {
        MockServerConfiguration serverConfiguration = new MockServerConfiguration();
        MockRoundConfiguration roundConfiguration = serverConfiguration.getCurrentParameters();
        assertEquals(0, roundConfiguration.getRoundNumber());
        assertTrue(roundConfiguration.isPracticeRound());
        roundConfiguration = serverConfiguration.nextRound();
        assertEquals(1, roundConfiguration.getRoundNumber());
        assertFalse(roundConfiguration.isPracticeRound());
    }

    @Test
    public void testMultiplePracticeRounds() {
        MockServerConfiguration serverConfiguration = new MockServerConfiguration();
        MockRoundConfiguration roundConfiguration = serverConfiguration.getCurrentParameters();
        assertEquals(0, roundConfiguration.getRoundNumber());
        assertTrue(roundConfiguration.isPracticeRound());
        roundConfiguration = serverConfiguration.nextRound();
        roundConfiguration.setPracticeRound(true);
        assertEquals(0, roundConfiguration.getRoundNumber());
        assertTrue(roundConfiguration.isPracticeRound());
        roundConfiguration = serverConfiguration.nextRound();
        assertEquals(1, roundConfiguration.getRoundNumber());
        assertFalse(roundConfiguration.isPracticeRound());

    }

    public static class MockServerConfiguration
            extends ExperimentConfiguration.Base<MockServerConfiguration, MockRoundConfiguration> {

        private static final long serialVersionUID = 7867208205942476733L;

        private final static String CONFIGURATION_FILE_NAME = "server.xml";

        public MockServerConfiguration() {
            super();
        }

        public MockServerConfiguration(String configurationDirectory) {
            super(configurationDirectory);
        }

        public String getLogFileDestination() {
            return getStringProperty("log");
        }

        public String getPersistenceDirectory() {
            return getStringProperty("save-dir", "data");
        }

        public boolean shouldUpdateFacilitator() {
            return getBooleanProperty("update-facilitator");
        }

        @Override
        protected MockRoundConfiguration createRoundConfiguration(String roundConfigurationFile) {
            return new MockRoundConfiguration(roundConfigurationFile);
        }

        @Override
        protected String getServerConfigurationFilename() {
            return CONFIGURATION_FILE_NAME;
        }

        public double getShowUpPayment() {
            return getDoubleProperty("showup-payment", 5.0d);
        }

    }

    public static class MockRoundConfiguration extends ExperimentRoundParameters.Base<MockServerConfiguration, MockRoundConfiguration> {

        private static final long serialVersionUID = -5053624886508752562L;

        private boolean practiceRound;

        public MockRoundConfiguration(String filename) {
            super(filename);
        }

        @Override
        public boolean isPracticeRound() {
            return super.isPracticeRound() || practiceRound;
        }

        @Override
        public String getInstructions() {
            return "Mock round instructions";
        }

        public int getClientsPerGroup() {
            return 5;
        }

        /**
         * @param practiceRound
         *            the practiceRound to set
         */
        public void setPracticeRound(boolean practiceRound) {
            this.practiceRound = practiceRound;
        }

    }

}
