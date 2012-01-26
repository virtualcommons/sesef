package edu.asu.commons.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class ShowExitInstructionsRequest extends AbstractEvent implements ShowRequest<ShowExitInstructionsRequest> {
	
	private static final long serialVersionUID = 3774308614796618926L;

	public ShowExitInstructionsRequest(Identifier id) {
        super(id);
    }
	
	@Override
	public ShowExitInstructionsRequest copy(Identifier id) {
	    return new ShowExitInstructionsRequest(id);
	}
	
}
