package edu.asu.commons.conf;

import java.io.Serializable;

/**
 * Marker interface for all configuration types
 * 
 * @author Allen Lee
 */
public interface Configuration extends Serializable {
	
	public abstract static class Base extends PropertiesConfiguration implements Configuration {
		private static final long serialVersionUID = 1106469234922728625L;
	}
	    
}
