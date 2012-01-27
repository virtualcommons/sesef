package edu.asu.commons.event;

import edu.asu.commons.experiment.DataModel;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@SuppressWarnings("rawtypes")
public class ShowExitInstructionsRequest extends AbstractEvent implements ShowRequest<ShowExitInstructionsRequest> {
	
	private static final long serialVersionUID = 3774308614796618926L;
	
	private DataModel dataModel;

	public ShowExitInstructionsRequest(Identifier id) {
        super(id);
    }
	
	public ShowExitInstructionsRequest(Identifier id, DataModel dataModel) {
	    super(id);
	    this.dataModel = dataModel;
	}
	
	@Override
	public ShowExitInstructionsRequest copy(Identifier id) {
	    return new ShowExitInstructionsRequest(id);
	}

    public DataModel getDataModel() {
        return dataModel;
    }

    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }
	
}
