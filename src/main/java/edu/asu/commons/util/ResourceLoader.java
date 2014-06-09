package edu.asu.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * $Id$
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class ResourceLoader {
	
	public static Properties getBuildProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(toInputStream("build.properties"));
		return properties;
	}
	
	public static Set<String> getClasspathJars() {
		URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();
		HashSet<String> filenames = new HashSet<>();
		for (URL url: cl.getURLs()) {
			String filename = url.getFile();
			if (filename.endsWith(".jar")) {
				filenames.add(filename.substring(filename.lastIndexOf('/') + 1));	
			}
		}
		cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		for (URL url: cl.getURLs()) {
			String filename = url.getFile();
			if (filename.endsWith(".jar")) {
				filenames.add(filename.substring(filename.lastIndexOf('/') + 1));	
			}
		}
		return filenames;
	}

	/**
	 * Returns an InputStream for the given path by searching the filesystem and the classpath, in that order.
	 * The caller must close the returned InputStream after it is read. 
	 */
    @SuppressWarnings("resource")
	public static InputStream toInputStream(String path) {
        // first try to read it as a file
        InputStream stream = null;
        try {
            File file = new File(path);
            if (file.isFile()) {
                stream = new FileInputStream(file);
            }
            else {
                stream = getResourceAsStream(path);
            }
        } catch (AccessControlException e) {
            stream = getResourceAsStream(path);
        } catch (FileNotFoundException e) {
            stream = getResourceAsStream(path);
        }
        return stream;
    }

    public static InputStream getResourceAsStream(String path) {
        InputStream stream = ResourceLoader.class.getResourceAsStream(path);
        if (stream == null) {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        }
        if (stream == null) {
            stream = ClassLoader.getSystemResourceAsStream(path);
        }
        if (stream == null) {
            throw new RuntimeException("Couldn't load resource: " + path);
        }
        return stream;
    }

    public static ByteArrayOutputStream getByteArrayOutputStream(String fileName) throws IOException {
        InputStream stream = ResourceLoader.toInputStream(fileName);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            while (stream.available() > 0) {
                byteStream.write(stream.read());
            }
            return byteStream;
        } finally {
            stream.close();
        }
    }

    public static URL getResourceAsUrl(String resource) {
        URL url = ResourceLoader.class.getResource(resource);
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(resource);
        }
        if (url == null) {
            throw new RuntimeException("Could not load resource via ClassLoader: " + resource);
        }
        return url;
    }

}
