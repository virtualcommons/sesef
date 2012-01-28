package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id: ClientMessageEvent.java 1 2008-07-23 22:15:18Z alllee $
 *
 * Client message to the server (ultimately passed on to the facilitator) signifying that 
 * the client is ready to move on. 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */

public class ClientReadyEvent extends AbstractEvent implements ClientRequest {

	private static final long serialVersionUID = -4570877808942064189L;
	
	public ClientReadyEvent(Identifier id) {
		this(id, id + " is ready.");
	}

	public ClientReadyEvent(Identifier id, String message) {
        super(id, message);
    }
    
}

