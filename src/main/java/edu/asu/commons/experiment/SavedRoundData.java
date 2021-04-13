package edu.asu.commons.experiment;

import com.thoughtworks.xstream.XStream;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.event.RoundStartedMarkerEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 
 * Struct-ish class for saved round data. Should see if we can somehow preserve type information here.
 * 
 * FIXME: how best to encode specific/concrete experiment types, like ServerConfiguration, RoundConfiguration, and ServerDataModel concrete subtypes
 * in such a way that the library (i.e., this class) can return objects of the appropriate type to a caller? Right now the getter methods for
 * this class just return the base interface type so casts are necessary to extract any round-specific data from the ExperimentRoundParameters or DataModel.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SavedRoundData implements Serializable {

    private static final long serialVersionUID = -2136359143854670064L;

    private final static Logger logger = Logger.getLogger(Persister.class.getName());

    private ExperimentRoundParameters roundParameters;
    private DataModel dataModel;
    private SortedSet<PersistableEvent> actions;
    private SortedSet<ChatRequest> chatRequests;
    private final String saveFilePath;

    private long roundStartTime;

    public SavedRoundData() {
        this("");
    }

    public SavedRoundData(String roundSaveFilePath) {
        this(roundSaveFilePath, null, null, null);
    }

    public SavedRoundData(String roundSaveFilePath, ExperimentRoundParameters roundParameters, DataModel dataModel, SortedSet<PersistableEvent> actions) {
        this.saveFilePath = roundSaveFilePath;
        this.roundParameters = roundParameters;
        this.dataModel = dataModel;
        setActions(actions);
    }

    public static SavedRoundData createFromXml(String roundSaveFilePath) {
        XStream xstream = new XStream();
        xstream.omitField(AbstractPersistableEvent.class, "instant");
        SavedRoundData savedRoundData = new SavedRoundData(roundSaveFilePath);
        try (ObjectInputStream stream = xstream.createObjectInputStream(new FileInputStream(roundSaveFilePath))) {
            return fromStream(stream, savedRoundData);
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.severe("Unable to load savefile from path: " + roundSaveFilePath + " exception: " + e);
            throw new RuntimeException(e);
        }
    }

    public static SavedRoundData fromStream(ObjectInputStream stream, SavedRoundData savedRoundData) {
        try {
            ExperimentRoundParameters roundParameters = (ExperimentRoundParameters) stream.readObject();
            savedRoundData.setRoundParameters(roundParameters);
            DataModel dataModel = (DataModel) stream.readObject();
            savedRoundData.setDataModel(dataModel);
            SortedSet<PersistableEvent> actions = (SortedSet<PersistableEvent>) stream.readObject();
            savedRoundData.setActions(actions);
            SortedSet<ChatRequest> chatRequests = (SortedSet<ChatRequest>) stream.readObject();
            savedRoundData.setChatRequests(chatRequests);
            return savedRoundData;
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.severe("Unable to read data model or actions from stream: " + e);
            throw new RuntimeException(e);
        }
    }

    public static SavedRoundData create(String roundSaveFilePath) {
        SavedRoundData savedRoundData = new SavedRoundData(roundSaveFilePath);
        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(roundSaveFilePath))) {
            return fromStream(stream, savedRoundData);
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.severe("Unable to load save file from path: " + roundSaveFilePath + " exception: " + e);
            throw new RuntimeException(e);
        }
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public long getElapsedTime(PersistableEvent event) {
        return event.getCreationTime() - roundStartTime;
    }

    public long getElapsedTimeInSeconds(PersistableEvent event) {
        return toSeconds(getElapsedTime(event));
    }

    public String toSecondString(PersistableEvent event) {
        return String.format("%3.3f", (double) getElapsedTime(event) / 1000.0);
    }

    public ExperimentRoundParameters getRoundParameters() {
        return roundParameters;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public SortedSet<PersistableEvent> getActions() {
        return actions;
    }

    public String getSaveFilePath() {
        return saveFilePath;
    }

    public static long toSeconds(long t) {
        return toSeconds(t, TimeUnit.MILLISECONDS);
    }

    public static long toSeconds(long t, TimeUnit timeUnit) {
        return TimeUnit.SECONDS.convert(t, timeUnit);
    }

    /**
     * Returns the number of milliseconds elapsed from the start of the day (midnight) to the given Event's
     * creation time. For example, if the event was created on November 7, 2016 at 19h54m49s this would return
     * 71689036
     * @param event
     * @return the number of milliseconds elapsed from the start of the day (midnight) to the given Event's creation
     * time.
     */
    public long getElapsedTimeRelativeToMidnight(PersistableEvent event) {
        return getElapsedTimeRelativeToMidnight(event.getCreationTime());
    }

    public long getElapsedTimeRelativeToMidnight(long millisFromEpoch) {
        Instant eventInstant = Instant.ofEpochMilli(millisFromEpoch);
        Instant eventInstantDay = eventInstant.truncatedTo(ChronoUnit.DAYS);
        long relative = eventInstantDay.until(eventInstant, ChronoUnit.MILLIS);
        return relative;
    }

    public void setRoundParameters(ExperimentRoundParameters roundParameters) {
        this.roundParameters = roundParameters;
    }

    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void setActions(SortedSet<PersistableEvent> actions) {
        this.actions = actions;
        setRoundStartTime();
    }

    public SortedSet<ChatRequest> getChatRequests() {
        return chatRequests;
    }

    public void setRoundStartTime() {
        if (actions != null && !actions.isEmpty()) {
            roundStartTime = actions.iterator().next().getCreationTime();
            for (PersistableEvent event : actions) {
                if (event instanceof RoundStartedMarkerEvent) {
                    RoundStartedMarkerEvent roundStartedEvent = (RoundStartedMarkerEvent) event;
                    roundStartTime = roundStartedEvent.getCreationTime();
                    return;
                }
            }
        }
    }

    public void setChatRequests(SortedSet<ChatRequest> chatRequests) {
        this.chatRequests = chatRequests;
    }

    /**
     * Given this SavedRoundData's save file path like `/code/experiment-data/t1/01-24-2019/17.21.54/round-9.save`
     * return a List ["01-24-2019, "17.21.54"].
     *
     * Always expects saveFilePath to be a String path with date time information encoded in two parent directories
     * above the save file.
     *
     * @return a List of two Strings, the first a date in "MM-DD-YYYY" and the second the time in "hh.mm.ss" format.
     */
    public List<String> extractDateTime() {
        Path path = Paths.get(saveFilePath);
        int numberOfElements = path.getNameCount();
        // always assumes a path with date time information encoded in the parent
        // directories, above the actual binary save file e.g., ../01-24-2019/17.21.54/round-X.save
        // List.of in JDK 11.. when we get there
        return Arrays.asList(path.getName(numberOfElements-3).toString(), path.getName(numberOfElements-2).toString());
    }
}
