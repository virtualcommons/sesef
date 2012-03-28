package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * A message from the server to the client indicating some kind of error occurred.
 * There does not always need to be a wrapped Exception.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public class ClientMessageEvent extends AbstractEvent {

    private static final long serialVersionUID = -2213276291145127929L;

    public ClientMessageEvent(Identifier id, String message) {
        super(id, message);
    }

}
