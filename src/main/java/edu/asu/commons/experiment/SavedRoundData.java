package edu.asu.commons.experiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.event.RoundStartedMarkerEvent;

/**
 * $Id: SavedRoundData.java 521 2010-06-29 16:08:12Z alllee $
 * 
 * Struct-ish class for saved round data.  Should see if we can somehow preserve type information here.  
 * 
 * FIXME: how best to encode specific/concrete experiment types, like ServerConfiguration, RoundConfiguration, and ServerDataModel concrete subtypes 
 * in such a way that the library (i.e., this class) can return objects of the appropriate type to a caller?  Right now the getter methods for
 * this class just return the base interface type so casts are necessary to extract any round-specific data from the ExperimentRoundParameters or DataModel.    
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 521 $
 */
@SuppressWarnings("unchecked")
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
        ObjectInputStream stream = null;
        XStream xstream = new XStream();
        SavedRoundData savedRoundData = new SavedRoundData(roundSaveFilePath);
        try {       
            // FIXME: duplication across BinarySavedRoundData, refactor
            stream = xstream.createObjectInputStream(new FileInputStream(roundSaveFilePath));
            
            ExperimentRoundParameters roundParameters = (ExperimentRoundParameters) stream.readObject();
            logger.info("round parameters: " + roundParameters);
            savedRoundData.setRoundParameters(roundParameters);
            
            DataModel dataModel = (DataModel) stream.readObject();
            logger.info("dataModel: " + dataModel);
            savedRoundData.setDataModel(dataModel);
            SortedSet<PersistableEvent> actions = (SortedSet<PersistableEvent>) stream.readObject();
            logger.info("actions: " + actions);
            savedRoundData.setActions(actions);
            return savedRoundData;
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.severe("Unable to load savefile from path: " + roundSaveFilePath + " exception: " + e);
            throw new RuntimeException(e);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static SavedRoundData create(String roundSaveFilePath) {
        ObjectInputStream stream = null;
        SavedRoundData savedRoundData = new SavedRoundData(roundSaveFilePath);
        try {            
            stream = new ObjectInputStream(new FileInputStream(roundSaveFilePath));
            
            ExperimentRoundParameters roundParameters = (ExperimentRoundParameters) stream.readObject();
            savedRoundData.setRoundParameters(roundParameters);
            
            DataModel dataModel = (DataModel) stream.readObject();
            savedRoundData.setDataModel(dataModel);
            SortedSet<PersistableEvent> actions = (SortedSet<PersistableEvent>) stream.readObject();
            savedRoundData.setActions(actions);
            SortedSet<ChatRequest> chatRequests = (SortedSet<ChatRequest>) stream.readObject();
            savedRoundData.setChatRequests(chatRequests);
//            SavedRoundData savedRoundData = new SavedRoundData(roundSaveFilePath, roundParameters, dataModel, actions);
            return savedRoundData;
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.severe("Unable to load savefile from path: " + roundSaveFilePath + " exception: " + e);
            throw new RuntimeException(e);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
      
    public double getElapsedTimeDouble(PersistableEvent event) {
        return (double) getElapsedTime(event) / 1000000000.0;
    }
    
    public String toSecondString(PersistableEvent event) {
        return String.format("%3.3f", getElapsedTimeDouble(event));
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
        return toSeconds(t, TimeUnit.NANOSECONDS);
    }
    
    public static long toSeconds(long t, TimeUnit timeUnit) {
        return TimeUnit.SECONDS.convert(t, timeUnit);
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
        if (actions != null && ! actions.isEmpty()) {
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
}
