package edu.asu.commons.experiment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * $Id: SaveFileProcessor.java 471 2010-02-12 22:44:32Z alllee $
 *  
 * Concrete implementations of this interface are used as callback hooks that contain whatever custom savefile processing code is 
 * needed.  
 * 
 * A typical save file contains objects in this order:
 * <ol>
 * <li>RoundConfiguration
 * <li>DataModel
 * <li>SortedSet&lt;PersistableEvent&gt;
 * </ol>
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 471 $
 */
public interface SaveFileProcessor {

    /**
     * 
     * @param savedRoundData the restored round data in Java object form.  
     * @param stream an output stream for the processed round data file. 
     */
    public void process(SavedRoundData savedRoundData, OutputStream stream);
    /**
     * 
     * @param savedRoundData the restored round data in Java object form.
     * @param roundSaveFile the full path to the processed round data file (which will be created upon successful exit of this method).
     */
    public void process(SavedRoundData savedRoundData, String roundSaveFile);
    public String getOutputFileExtension();
    public String getOutputFileName();
    
    public abstract static class Base implements SaveFileProcessor {
        // FIXME: not thread safe for concurrent usage when reusing the same SaveFileProcessor in multiple threads 
        // on multiple save files.
    	
    	private long secondsPerInterval;
    	private long currentInterval = 0;
    	private long intervalEnd = 0;
    	
        private String roundSaveFile;
        
        public Base() {
        	this(60);
        }
        
        public Base(int secondsPerInterval) {
        	this.secondsPerInterval = secondsPerInterval;
        }
        
        public boolean isIntervalElapsed(long secondsElapsed) {
        	intervalEnd = (currentInterval + 1) * secondsPerInterval;
        	if (secondsElapsed >= intervalEnd) {
        		currentInterval++;
        		return true;
        	}
        	return false;
        }
        
        /**
         * Returns the total number of seconds that must elapse before the current interval ends (e.g., 60, 120, 180). 
         * @return the total number of seconds that must elapse before the current interval ends
         */
        protected long getIntervalEnd() {
        	return intervalEnd;
        }
        
        protected void resetCurrentInterval() {
        	currentInterval = 0;
        }
        
        public void setSecondsPerInterval(long secondsPerInterval) {
			this.secondsPerInterval = secondsPerInterval;
		}

		public void process(SavedRoundData savedRoundData, String roundSaveFile) {
            this.roundSaveFile = roundSaveFile;
            FileOutputStream defaultFileOutputStream = null;
            try {
                defaultFileOutputStream = new FileOutputStream( getOutputFileName() );
                currentInterval = 0;
                process(savedRoundData, defaultFileOutputStream);
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            finally {
                dispose();
                if (defaultFileOutputStream != null) {
                    try {
                        defaultFileOutputStream.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        
        public abstract void process(SavedRoundData savedRoundData, PrintWriter writer);
        
        public void process(SavedRoundData savedRoundData, OutputStream stream) {
            PrintWriter writer = new PrintWriter(stream); 
            process(savedRoundData, writer);
            writer.flush();
            writer.close();
        }

        /**
         * Returns an output file name for this save file processor based on the output file extension.
         */
        public String getOutputFileName() {
            return roundSaveFile + getOutputFileExtension();
        }
        
        public void dispose() { }
    }

}
