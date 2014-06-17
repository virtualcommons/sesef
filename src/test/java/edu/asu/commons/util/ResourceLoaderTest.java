package edu.asu.commons.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import java.util.logging.Logger;

public class ResourceLoaderTest {
	
	private Logger logger = Logger.getLogger(getClass().getName());

	public void testGetClasspathJars() {
		Set<String> jars = ResourceLoader.getClasspathJars();
		logger.info("" + jars);
		assertNotNull(jars);
	}
	
	public void testBuildProperties() throws Exception {
		Properties properties = ResourceLoader.getBuildProperties();
		String jnlpJars = properties.getProperty("jnlp.jars"); 
		assertNotNull(jnlpJars);
		logger.info(jnlpJars);
		String[] jarfiles = jnlpJars.split(",");
		assertNotNull(jarfiles);
		logger.info(Arrays.asList(jarfiles).toString());
	}
	
	@Test
	public void testResourceLoaderString() throws Exception {
	    String readme = ResourceLoader.getResourceAsString("conf/README.txt");
	    assertNotNull(readme);
	    assertTrue(readme.length() > 0);
	    assertTrue(readme.contains("\n"));
	}

}
