package edu.asu.commons.experiment;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * $Id$
 * 
 * @version $Rev: 477 $
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
public class SaveFileProcessorTest {

    private static class IntervalProcessor extends SaveFileProcessor.Base {

        @Override
        public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        }

        @Override
        public String getOutputFileExtension() {
            return "-test-interval.txt";
        }
    }

    private IntervalProcessor processor;

    @Before
    public void setUp() {
        processor = new IntervalProcessor();
    }

    @Test
    public void testIntervals() {
        long secondsPerInterval = 60;
        processor.setSecondsPerInterval(secondsPerInterval);
        for (long i = 0; i < secondsPerInterval; i++) {
            assertFalse("interval of 60 seconds should return false for isIntervalElapsed from 0-59", processor.isIntervalElapsed(i));
        }
        assertTrue("interval of 60 seconds should return true for isIntervalElapsed(60)", processor.isIntervalElapsed(60));
        assertFalse("should now return false for 60 seconds", processor.isIntervalElapsed(60));
        for (long i = 0; i < secondsPerInterval * 2; i++) {
            assertFalse("interval of 60 seconds should return false for isIntervalElapsed from 0-119", processor.isIntervalElapsed(i));
        }
        assertTrue("interval of 60 seconds should return true for isIntervalElapsed(120)", processor.isIntervalElapsed(120));
        assertFalse("interval has already elapsed for isIntervalElapsed(120)", processor.isIntervalElapsed(120));
        processor.resetCurrentInterval();
        for (long i = 0; i < secondsPerInterval; i++) {
            assertFalse("after reset, interval of 60 seconds should return false for isIntervalElapsed from 0-60", processor.isIntervalElapsed(i));
        }
    }
    
    @Test
    public void testPrintln() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        processor.println(writer, "hey", "there", 1, 2, 3);
        writer.flush();
        String line = new String(out.toByteArray());
        assertEquals("hey,there,1,2,3\n", line);
    }

}
