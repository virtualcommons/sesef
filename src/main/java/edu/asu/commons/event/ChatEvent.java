package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * $Id$
 * 
 * Sent from the server to a client target (with Identifier getId()) from the client identified by getSource().
 * Essentially the same as a ChatRequest, but with permuted Identifier parameters.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public class ChatEvent extends AbstractEvent {

    private static final long serialVersionUID = 475300882222383637L;

    private final String message;

    private Identifier source;

    private boolean addressedToAll;

    public ChatEvent(Identifier target, String message, Identifier source) {
        super(target);
        this.message = message;
        this.source = source;
    }

    public ChatEvent(Identifier target, String message, Identifier source, boolean addressedToAll) {
        super(target);
        this.message = message;
        this.source = source;
        this.addressedToAll = addressedToAll;
    }

    public Identifier getSource() {
        return source;
    }

    public Identifier getTarget() {
        return isAddressedToAll() ? Identifier.ALL : super.id;
    }

    public String toString() {
        return message;
    }

    public boolean isAddressedToAll() {
        return addressedToAll;
    }
}
