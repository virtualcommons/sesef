package edu.asu.commons.experiment;

import java.util.SortedSet;

import edu.asu.commons.conf.ExperimentConfiguration;
import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.event.ChatRequest;
import edu.asu.commons.event.PersistableEvent;

public interface IPersister<C extends ExperimentConfiguration<C, R>, R extends ExperimentRoundParameters<C, R>> {

    public void stop();

    public SortedSet<PersistableEvent> getActions();

    public void store(PersistableEvent event);

    public void store(ChatRequest request);

    public void clearChatData();

    public void initialize(R roundConfiguration);

    public <E extends DataModel<C, R>> void persist(E serverDataModel);

    public void clear();

}