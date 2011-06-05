package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;



/**
 * $Id: ClientErrorMessageEvent.java 1 2008-07-23 22:15:18Z alllee $
 *
 * A message from the server to the client indicating some kind of error occurred.  
 * There does not always need to be a wrapped Exception.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */

public class ClientErrorMessageEvent extends AbstractEvent {

    private static final long serialVersionUID = 3561105311719978543L;
    
    private final Throwable throwable;

    public ClientErrorMessageEvent(Identifier id, Throwable throwable) {
        super(id, throwable.getMessage());
        this.throwable = throwable;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    
}

