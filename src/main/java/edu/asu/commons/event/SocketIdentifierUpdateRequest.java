package edu.asu.commons.event;

import edu.asu.commons.net.SocketIdentifier;

/**
 * $Id$
 * <p>
 * Update from the client notifying the server that the given socket identifier should be updated. Necessary to get the appropriate station identification
 * information in the COOR / CARL lab.
 * 
 * Only handled by specific servers that need it.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 460 $
 */
public class SocketIdentifierUpdateRequest extends AbstractEvent
        implements ClientRequest {

    private static final long serialVersionUID = -4890360782942202323L;

    private Integer stationNumber;

    public SocketIdentifierUpdateRequest(SocketIdentifier id, Integer stationNumber) {
        super(id);
        System.err.println("id's station number: " + id.getStationNumber());
        System.err.println("id's station number: " + stationNumber);
        this.stationNumber = stationNumber;
    }

    public SocketIdentifier getSocketIdentifier() {
        return (SocketIdentifier) getId();
    }

    public Integer getStationNumber() {
        return stationNumber;
    }
}
