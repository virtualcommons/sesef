package edu.asu.commons.experiment;

import edu.asu.commons.event.ChatEvent;
import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.net.Identifier;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.Assert.*;

/**
 * $Id$
 * 
 * @version $Rev: 477 $
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
public class SavedRoundDataTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testGetElapsedTimeRelativeToMidnight() {
        SavedRoundData data = new SavedRoundData();
        ChatEvent event = new ChatEvent(Identifier.NULL, "hello", Identifier.ALL);
        long elapsedTime = data.getElapsedTimeRelativeToMidnight(event);
        assertTrue(elapsedTime > 0);
    }


}
