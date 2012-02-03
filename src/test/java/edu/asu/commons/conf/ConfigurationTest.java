package edu.asu.commons.conf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import edu.asu.commons.net.ServerDispatcher.Type;
import edu.asu.commons.util.Duration;
import static org.junit.Assert.*;

public class ConfigurationTest {

    private PropertiesConfiguration assistant;
    
    @Before
    public void setUp() {
        assistant = new PropertiesConfiguration();
    }
    
    @Test
    public void testConfigurationAssistant() {
        assertEquals(0.0d, assistant.getDoubleProperty("foo", 0.0d), 0.0d);
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
    public void testDefaultConfigurationDirectory() {
    
    }
    
    @Test
    public void testTemplate() {
        //ServerConfiguration serverConfiguration = new ServerConfiguration();

        
    }

    public static class MockServerConfiguration 
    extends ExperimentConfiguration.Base<MockRoundConfiguration> {

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
            return new MockRoundConfiguration();
        }

        @Override
        protected String getServerConfigurationFilename() {
            return CONFIGURATION_FILE_NAME;
        }
        
        private final static String[] PRIORITY_STRINGS = { "A", "B", "C", "D", "E" };

        public String toPriorityString(int clientPriority) {
            // bounds check
            if (clientPriority >= 0 && clientPriority < PRIORITY_STRINGS.length) {
                return PRIORITY_STRINGS[clientPriority];
            }
            return "Position not found";
        }

        public boolean isUndisruptedFlowRequired(){
            return getBooleanProperty("undisrupted-flow-required", false);
        }

        public String getUndisruptedFlowInstructions() {
            return getProperty("undisrupted-flow-instructions", "");
        }

        public double getShowUpPayment() {
            return getDoubleProperty("showup-payment", 5.0d);
        }

        public String getInitialInstructions() {
            String initialInstructions = getProperty("initial-instructions", "");
            if (initialInstructions.contains("%d")) {
                return String.format(initialInstructions, getChatDuration());
            }
            return initialInstructions;
        }
        
        public String getWelcomeInstructions() {
            return getProperty("welcome-instructions");
        }

        public Map<String, String> getQuizAnswers() {
            Properties properties = getProperties();
            Map<String, String> answers = new HashMap<String, String>();
            for (int i = 1; properties.containsKey("q" + i); i++) {
                String key = "q" + i;
                String answer = properties.getProperty(key);
                answers.put(key, answer);
                String quizExplanationKey = "explanation" + i;
                String quizExplanation = properties.getProperty(quizExplanationKey);
                answers.put(quizExplanationKey, quizExplanation);
                String descriptiveAnswerKey = "a" + i;
                answers.put(descriptiveAnswerKey, properties.getProperty(descriptiveAnswerKey));
            }
            return answers;
        }
        
        public String getQuizQuestion(int pageNumber) {
            return getProperty("general-instructionsq" + pageNumber);
        }

        public String getQuizPage(int pageNumber) {
            return getProperty("quiz-page"+pageNumber); 
        }
        
        public String getWaterCollectedToTokensTable() {
            return getProperty("water-collected-to-tokens-table");
        }

        public String getFinalInstructions() {
            return getProperty("final-instructions", "<b>The experiment is now over.  Thanks for participating!</b>");
        }
        
        public String getInvestmentInstructions() {
            return getProperty("investment-instructions");
        }
        
        public int getNumberOfQuizPages() {
            return getIntProperty("question-pages", 2);
        }
        
        public int getChatDuration() {
            return getIntProperty("chat-duration", 60);
        }

        public String getChatInstructions() {
            String chatInstructions = getProperty("chat-instructions", "");
            if (chatInstructions.contains("%d")) {
                return String.format(chatInstructions, getChatDuration());
            }
            return chatInstructions;
        }

        public String getGameScreenshotInstructions() {
            return getProperty("game-screenshot-instructions");
        }

        @Override
        public Type getServerDispatcherType() {
            return Type.SOCKET;
        }

    }
    
    public static class MockRoundConfiguration extends ExperimentRoundParameters.Base<MockServerConfiguration> {

        private static final long serialVersionUID = -5053624886508752562L;

        private static final float DEFAULT_DOLLARS_PER_TOKEN = .05f;

        public static int getTokensEarned(int waterCollected) {
            if (waterCollected < 150) {
                return 0;
            }
            else if (waterCollected < 200) {
                return 1;
            }
            else if (waterCollected < 250) {
                return 4;
            }
            else if (waterCollected < 300) {
                return 10;
            }
            else if (waterCollected < 350) {
                return 15;
            }
            else if (waterCollected < 400) {
                return 18;
            }
            else if (waterCollected < 500) {
                return 19;
            }
            else if (waterCollected < 550) {
                return 20;
            }
            else if (waterCollected < 650) {
                return 19;
            }
            else if (waterCollected < 700) {
                return 18;
            }
            else if (waterCollected < 750) {
                return 15;
            }
            else if (waterCollected < 800) {
                return 10;
            }
            else if (waterCollected < 850) {
                return 4;
            }
            else if (waterCollected < 900) {
                return 1;
            }
            else {
                return 0;
            }
        }

        public int getMaximumClientFlowCapacity() {
            return getIntProperty("max-client-flow-capacity", 25);
        }

        public int getInitialInfrastructureEfficiency() {
            return getIntProperty("initial-infrastructure-efficiency", 75);
        }

        public int getInfrastructureDegradationFactor() {
            return getIntProperty("infrastructure-degradation-factor", 25);
        }

        public int getWaterSupplyCapacity() {
            return getIntProperty("max-canal-flow-capacity", 30);
        }

        public int getMaximumTokenInvestment() {
            return getIntProperty("max-token-investment", 10);
        }

        /**
         * returns maximum number of tokens that could have been contributed
         * @return
         */
        public int getMaximumTotalInvestedTokens() {
            return getMaximumTokenInvestment() * getClientsPerGroup();
        }

        public int getMaximumInfrastructureEfficiency() {
            return getIntProperty("max-infrastructure-efficiency", 100);
        }

        public boolean isPracticeRound() {
            return getBooleanProperty("practice-round");
        }
        
        public String getPracticeRoundPaymentInstructions() {
            return getProperty("practice-round-payment-instructions", 
                    "This is a practice round so the earnings mentioned are only for illustrative purposes and <b>will not count towards your actual earnings</b>.");
        }
        
        public int getClientsPerGroup() {
            return getIntProperty("clients-per-group", 5);
        }

        /**
         * Returns the dollars/token exchange rate.  $1 = 1, 50 cents = $.50, 1 penny per token = .01, etc.
         * 
         * FIXME: this should be a ServerConfiguration parameter unless we change it so
         * the client keeps track of total dollars earned per round instead of total tokens earned per round. 
         * 
         * @return
         */
        public double getDollarsPerToken() {
            return getDoubleProperty("dollars-per-token", DEFAULT_DOLLARS_PER_TOKEN); 
        }

        /**
         * for debugging purposes
         */
        public void report() {
            getProperties().list(System.err);
        }
        
        public boolean shouldResetInfrastructureEfficiency() {
            return isFirstRound() || getBooleanProperty("reset-infrastructure-efficiency", false);
        }

        public String getInstructions() {
            return getStringProperty("instructions", 
                    "<b>No instructions available for this round.</b>");
        }

        public boolean shouldDisplayGroupTokens() {
            return getBooleanProperty("display-group-tokens");
        }

        public boolean isQuizEnabled() {
            return getBooleanProperty("quiz");
        }

        public String getQuizPage() {
            return getStringProperty("quiz-page");
        }

        public Map<String, String> getQuizAnswers() {
            if (isQuizEnabled()) {
                Properties properties = getProperties();
                Map<String, String> answers = new HashMap<String, String>();
                for (int i = 1; properties.containsKey("q" + i); i++) {
                    String key = "q" + i;
                    String answer = properties.getProperty(key);
                    answers.put(key, answer);
                }
                return answers;
            }
            return Collections.emptyMap();
        }

        /**
         * Returns true if the current round should have a communication session for
         * getChatDuration() seconds before the round begins.
         * 
         * @return
         */
        public boolean isChatEnabledBeforeRound() {
            return getBooleanProperty("chat-enabled-before-round", true);
        }


        /**
         * Returns the duration of the round in seconds.  Set to default of 50 seconds per round.
         */
        @Override
        public Duration getRoundDuration() {
            return Duration.create(getRoundDurationInSeconds());
        }
        
        public int getRoundDurationInSeconds() {
            return getIntProperty("round-duration", 50);
        }

        public boolean shouldRandomizeGroup() {
            return getBooleanProperty("randomize-groups", false);
        }

    }


}
