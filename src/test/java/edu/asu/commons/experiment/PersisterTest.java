package edu.asu.commons.experiment;

import edu.asu.commons.conf.ConfigurationTest;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeChannel;
import edu.asu.commons.net.Identifier;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PersisterTest {

    static class MockPersister extends Persister<ConfigurationTest.MockServerConfiguration, ConfigurationTest.MockRoundConfiguration> {

        public MockPersister(ConfigurationTest.MockServerConfiguration experimentConfiguration) {
            super(experimentConfiguration);
        }

        @Override
        protected String getFailSafeSaveDirectory() {
            return "/tmp/mock-persister-test-saves/";
        }
    }

    static class MockDataModel implements DataModel<ConfigurationTest.MockServerConfiguration, ConfigurationTest.MockRoundConfiguration> {

        private ConfigurationTest.MockServerConfiguration serverConfiguration;

        public MockDataModel(ConfigurationTest.MockServerConfiguration serverConfiguration) {
            this.serverConfiguration = serverConfiguration;
        }

        @Override
        public ConfigurationTest.MockServerConfiguration getExperimentConfiguration() {
            return serverConfiguration;
        }

        @Override
        public ConfigurationTest.MockRoundConfiguration getRoundConfiguration() {
            return null;
        }

        @Override
        public List<Identifier> getAllClientIdentifiers() {
            return null;
        }

        @Override
        public EventChannel getEventChannel() {
            return EventTypeChannel.getInstance();
        }
    }

    private ConfigurationTest.MockServerConfiguration repeatedRoundConfiguration;
    private ConfigurationTest.MockServerConfiguration standardConfiguration;

    @Before
    public void setUp() {
        repeatedRoundConfiguration = ConfigurationTest.createRepeatedRoundConfiguration(10);
        standardConfiguration = new ConfigurationTest.MockServerConfiguration();
    }

    public File getTestSaveFileDirectory() {
        return new File(repeatedRoundConfiguration.getPersistenceDirectory());
    }

    @Test
    public void testRepeatedRoundPersister() {
        MockDataModel mockDataModel = new MockDataModel(repeatedRoundConfiguration);
        MockPersister repeatedRoundPersister = new MockPersister(repeatedRoundConfiguration);
        int numberOfMessages = 20;
        ConfigurationTest.MockRoundConfiguration firstRound = repeatedRoundConfiguration.getCurrentParameters();
        for (int roundNumber = 0; roundNumber < 2; roundNumber++) {
            for (int i = 0; i < repeatedRoundConfiguration.getCurrentParameters().getRepeat(); i++) {
                for (int j = 0; j < numberOfMessages; j++) {
                    repeatedRoundPersister.store(new ChatRequest(Identifier.ALL, "message #: " + numberOfMessages));
                }
                repeatedRoundPersister.persist(mockDataModel);
                String roundIndexLabel = roundNumber + "." + i;
                assertEquals(repeatedRoundConfiguration.getCurrentRoundLabel(), roundIndexLabel);
                String roundSaveFilePath = repeatedRoundPersister.getDefaultRoundSaveFilePath(roundNumber + "." + i);
                assertTrue(i + " ASSERT " + roundSaveFilePath + " exists", Files.exists(Paths.get(roundSaveFilePath)));
                ConfigurationTest.MockRoundConfiguration nextRound = repeatedRoundConfiguration.nextRound();
                System.err.println("next round: " + nextRound.getRoundIndexLabel());
                repeatedRoundPersister.initialize(nextRound);
            }
        }
    }

}
