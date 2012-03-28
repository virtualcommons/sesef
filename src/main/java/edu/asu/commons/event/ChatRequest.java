package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id: ChatRequest.java 524 2010-08-06 00:53:30Z alllee $
 * 
 * Sent from a client to the server signaling that the client wants to talk to a different client identified by getTarget().
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 524 $
 */

public class ChatRequest extends AbstractPersistableEvent implements ClientRequest {

    private static final long serialVersionUID = 475300882222383637L;

    private final Identifier target;

    public ChatRequest(Identifier source, String message) {
        this(source, message, Identifier.ALL);
    }

    /**
     * A communication event with a target of Identifier.ALL is broadcast to all group participants.
     * 
     * @param source
     * @param message
     */
    public ChatRequest(Identifier source, String message, Identifier target) {
        super(source, message);
        this.target = target;
    }

    // copy constructor for server-side timestamping
    public ChatRequest(ChatRequest request) {
        this(request.getId(), request.toString(), request.getTarget());
    }

    public Identifier getSource() {
        return id;
    }

    public Identifier getTarget() {
        return target;
    }

    public String toString() {
        return message;
    }
}
