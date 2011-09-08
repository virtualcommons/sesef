package edu.asu.commons.net;

import org.junit.Test;

import static org.junit.Assert.*;

public class SocketIdentifierTest {

    @Test
    public void testEqualsAndHashCode() {
        SocketIdentifier firstId = createSocketIdentifier("localhost", 1000);
        SocketIdentifier secondId = createSocketIdentifier("localhost", 10001);
        assertEquals(firstId.hashCode(), createSocketIdentifier("localhost", 1000).hashCode());
        
        assertEquals(secondId, createSocketIdentifier("localhost", 10001));
        assertEquals(secondId.hashCode(), createSocketIdentifier("localhost", 10001).hashCode());
        assertFalse(firstId.equals(secondId));
    }
    
    private SocketIdentifier createSocketIdentifier(String hostTemplate, int port) {
        return new SocketIdentifier(hostTemplate, port, hostTemplate + ".foo", port + 10);
    }

}
