package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;
import edu.asu.commons.net.SocketIdentifier;

/**
 * $Id: FacilitatorRegistrationRequest.java 462 2010-02-05 04:50:21Z alllee $
 * 
 * Signal sent from a facilitator to the server.
 * 
 * @author <a href='anonymouslee@gmail.com'>Allen Lee</a>, Deepali
 * @version $Revision: 462 $
 */

public class FacilitatorRegistrationRequest extends AbstractEvent implements FacilitatorRequest {

    private static final long serialVersionUID = 8173056766508941532L;

    private Integer stationNumber;

    public FacilitatorRegistrationRequest(Identifier id) {
        super(id);
    }

    public FacilitatorRegistrationRequest(SocketIdentifier id) {
        super(id);
        this.stationNumber = id.getStationNumber();
    }

    public Integer getStationNumber() {
        return stationNumber;
    }

}
