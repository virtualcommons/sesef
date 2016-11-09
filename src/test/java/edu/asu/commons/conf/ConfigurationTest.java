package edu.asu.commons.conf;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Uses configuration files stored in src/test/resources/conf to build MockServerConfiguration / MockRoundConfiguration.
 */
public class ConfigurationTest {

    private PropertiesConfiguration assistant;
    private MockServerConfiguration serverConfiguration;
    private Logger logger = Logger.getLogger(getClass().getName());

    @Before
    public void setUp() {
        assistant = new PropertiesConfiguration();
        serverConfiguration = new MockServerConfiguration();
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
        MockRoundConfiguration roundConfiguration = serverConfiguration.getCurrentParameters();
        Map<String, Object> roundConfigAsMap = roundConfiguration.toMap();
        assertEquals(roundConfiguration.getDuration(), roundConfigAsMap.get("duration"));
        assertEquals(roundConfiguration.getClientsPerGroup(), roundConfigAsMap.get("clientsPerGroup"));
        assertEquals(roundConfiguration.getInstructions(), roundConfigAsMap.get("instructions"));
    }

    @Test
    public void testRoundNumbering() {
        MockRoundConfiguration roundConfiguration = serverConfiguration.getCurrentParameters();
        assertEquals(0, roundConfiguration.getRoundNumber());
        assertTrue(roundConfiguration.isPracticeRound());
        roundConfiguration = serverConfiguration.nextRound();
        assertEquals(1, roundConfiguration.getRoundNumber());
        assertFalse(roundConfiguration.isPracticeRound());
    }

    @Test
    public void testMultiplePracticeRounds() {
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

    @Test
    public void testFirstAndLastRound() {
        MockRoundConfiguration roundConfiguration = serverConfiguration.getCurrentParameters();
        assertTrue(serverConfiguration.isFirstRound());
        while (!serverConfiguration.isLastRound()) {
            roundConfiguration = serverConfiguration.nextRound();
            assertNotNull(roundConfiguration);
        }
        // next round always returns the last round configuration once it's reached
        assertEquals(roundConfiguration, serverConfiguration.nextRound());
        assertFalse(serverConfiguration.isFirstRound());
        assertTrue(serverConfiguration.isLastRound());
        // test reset
        serverConfiguration.reset();
        roundConfiguration = serverConfiguration.getCurrentParameters();
        assertTrue(serverConfiguration.isFirstRound());
        assertFalse(serverConfiguration.isLastRound());
        for (int i = 0; i < serverConfiguration.getTotalNumberOfRounds(); i++) {
            assertNotEquals(roundConfiguration, serverConfiguration.nextRound());
        }
        assertFalse(serverConfiguration.isFirstRound());
        assertTrue(serverConfiguration.isLastRound());
    }

    @Test
    public void testIterator() {
        int totalNumberOfRounds = 0;
        for (MockRoundConfiguration c: serverConfiguration) {
            assertFalse(c.isRepeatingRound());
            assertNotNull(c);
            totalNumberOfRounds++;
        }
        assertEquals(serverConfiguration.getTotalNumberOfRounds(), totalNumberOfRounds);
        assertFalse(serverConfiguration.isLastRound());
        assertTrue(serverConfiguration.isFirstRound());
    }

    @Test
    public void testRepeatedRoundIterator() {
        int numberOfRepeatedRounds = 20;
        serverConfiguration = createRepeatedRoundConfiguration(numberOfRepeatedRounds);
        assertTrue(serverConfiguration.isFirstRound());
        assertFalse(serverConfiguration.isLastRound());
        assertEquals(numberOfRepeatedRounds * serverConfiguration.getNumberOfRounds(), serverConfiguration.getTotalNumberOfRounds());
        int roundCount = 0;
        for (MockRoundConfiguration c: serverConfiguration) {
            assertTrue(c.isRepeatingRound());
            logger.info(c.getRoundIndexLabel());
            roundCount++;
            assertNotNull(c);
        }
        assertEquals(serverConfiguration.getTotalNumberOfRounds(), roundCount);
        serverConfiguration.reset();
        assertTrue("reset server configuration should be on the first round", serverConfiguration.isFirstRound());
        assertFalse("Server configuration should not be on last round", serverConfiguration.isLastRound());
    }

    @Test
    public void testRepeatingLastRound() {
        serverConfiguration = createRepeatedRoundConfiguration(10);
        int totalNumberOfRounds = 0;
        for (int i = 0; i < serverConfiguration.getTotalNumberOfRounds(); i++) {
            String currentRoundLabel = serverConfiguration.getCurrentRoundLabel();
            assertTrue("current round label " + currentRoundLabel + " didn't end with " + i % 10,
                    currentRoundLabel.endsWith(String.valueOf(i % 10)));
            totalNumberOfRounds++;
            serverConfiguration.nextRound();
        }
        assertEquals(serverConfiguration.getTotalNumberOfRounds(), totalNumberOfRounds);
        assertTrue(serverConfiguration.isLastRound());
        assertFalse(serverConfiguration.isFirstRound());

    }

    @Test
    public void testRepeatingRounds() {
        MockRoundConfiguration firstRoundConfiguration = serverConfiguration.getCurrentParameters();
        // do this before setting the repeats, otherwise we will be getting the same round configuration until
        // repeat has expired
        firstRoundConfiguration.setPracticeRound(false);
        // check number of rounds before setting repeat
        assertEquals(7, serverConfiguration.getTotalNumberOfRounds());
        assertEquals(7, serverConfiguration.getNumberOfRounds());
        MockRoundConfiguration secondRoundConfiguration = serverConfiguration.getNextRoundConfiguration();
        int numberOfRepeats = 10;
        firstRoundConfiguration.setRepeat(numberOfRepeats);
        secondRoundConfiguration.setRepeat(numberOfRepeats);
        // after setting repeats, check number of rounds should be 7 + 10 + 10
        assertEquals(27, serverConfiguration.getTotalNumberOfRounds());
        assertEquals(7, serverConfiguration.getNumberOfRounds());
        assertTrue(firstRoundConfiguration.isRepeatingRound());
        assertEquals(0, serverConfiguration.getCurrentRepeatedRoundIndex());
        assertEquals(0, serverConfiguration.getCurrentRoundIndex());
        MockRoundConfiguration nextRound = serverConfiguration.getCurrentParameters();
        // first round
        for (int idx = 0; idx < numberOfRepeats; idx++) {
            assertEquals(idx, serverConfiguration.getCurrentRepeatedRoundIndex());
            assertEquals(0, serverConfiguration.getCurrentRoundIndex());
            assertEquals(nextRound, firstRoundConfiguration);
            assertEquals("0." + idx, nextRound.getRoundIndexLabel());
            nextRound = serverConfiguration.nextRound();
        }
        assertEquals(0, serverConfiguration.getCurrentRepeatedRoundIndex());
        assertEquals(1, serverConfiguration.getCurrentRoundIndex());
        assertEquals(secondRoundConfiguration, nextRound);
        // second round
        for (int idx = 0; idx < numberOfRepeats; idx++) {
            assertEquals(idx, serverConfiguration.getCurrentRepeatedRoundIndex());
            assertEquals(1, serverConfiguration.getCurrentRoundIndex());
            assertEquals(nextRound, secondRoundConfiguration);
            assertEquals("1." + idx, nextRound.getRoundIndexLabel());
            nextRound = serverConfiguration.nextRound();
        }
        assertFalse(nextRound.isRepeatingRound());
        assertEquals(0, nextRound.getRepeat());
        assertFalse(nextRound.equals(serverConfiguration.getNextRoundConfiguration()));
    }

    public static MockServerConfiguration createRepeatedRoundConfiguration(final int numberOfRepeats) {
        MockServerConfiguration serverConfiguration = new MockServerConfiguration() {
            @Override
            public int getNumberOfRounds() {
                return 3;
            }
            @Override
            public int getTotalNumberOfRounds() {
                return getNumberOfRounds() * numberOfRepeats;
            }
        };

        for (MockRoundConfiguration c: serverConfiguration.getAllParameters()) {
            c.setRepeat(numberOfRepeats);
        }
        return serverConfiguration;
    }

    public static class MockServerConfiguration extends ExperimentConfiguration.Base<MockServerConfiguration, MockRoundConfiguration> {

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
            return getStringProperty("save-dir", "data/test");
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

        private Boolean practiceRound;

        private int repeat;

        public MockRoundConfiguration(String filename) {
            super(filename);
        }

        @Override
        public boolean isPracticeRound() {
            if (practiceRound == null) {
                return super.isPracticeRound();
            }
            return practiceRound.booleanValue();
        }

        @Override
        public String getInstructions() {
            return "Mock round instructions";
        }

        @Override
        public int getRepeat() {
            return repeat;
        }

        public int getClientsPerGroup() {
            return 5;
        }

        public void setPracticeRound(boolean practiceRound) {
            this.practiceRound = practiceRound;
        }

        public void setRepeat(int repeat) {
            this.repeat = repeat;
        }

    }

}
