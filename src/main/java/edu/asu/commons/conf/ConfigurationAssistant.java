package edu.asu.commons.conf;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.stringtemplate.v4.ST;

import edu.asu.commons.util.ResourceLoader;

/**
 * $Id$
 * 
 * Provides convenience methods to read configuration properties from a java.util.Properties
 * instance.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class ConfigurationAssistant implements Serializable {
    
    private static final long serialVersionUID = -9093022080387404606L;
    
    private final Properties properties;
    
    public ConfigurationAssistant() {
        this(new Properties());
    }
    
    public ConfigurationAssistant(Properties properties) {
        this.properties = properties;
//        this.freemarkerConfiguration = new Configuration();
//        this.templateLoader = new StringTemplateLoader();
//        freemarkerConfiguration.setObjectWrapper(new DefaultObjectWrapper());
//        freemarkerConfiguration.setTemplateLoader(templateLoader);
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void loadProperties(String resource) {
        InputStream stream = ResourceLoader.toInputStream(resource);
        try {
            properties.loadFromXML(stream);
        } 
        catch (IOException e) {
            // FIXME: try to load them manually, not via loadFromXML?
            e.printStackTrace();
            throw new IllegalArgumentException("Couldn't load properties via loadFromXML - resource: "
                    + resource + " - stream: " + stream, e);
        }
    }
    
//    public void addTemplate(String templateName, String templateSource) {
//        templateLoader.putTemplate(templateName, templateSource);
//    }
//    
//    public String transform(String templateName, Object data) {
//        try {
//            Template template = freemarkerConfiguration.getTemplate(templateName);
//            StringWriter writer = new StringWriter();
//            template.process(data, writer);
//            return writer.toString();
//        }
//        catch (IOException exception) {
//            exception.printStackTrace();
//        }
//        catch (TemplateException exception) {
//            exception.printStackTrace();
//        }
//        return "";
//    }

    
    public String getProperty(String key) {
        return getStringProperty(key, "");
    }
    
    public String getProperty(String key, String defaultValue) {
        return getStringProperty(key, defaultValue);
    }
    
    public String getStringProperty(String key) {
        return getStringProperty(key, "");
    }
    
    public String getStringProperty(String key, String defaultValue) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        return defaultValue;
    }
    
    public int getIntProperty(String key) {
        return getIntProperty(key, 0);
    }
    
    public int getIntProperty(String key, int defaultValue) {
        if (properties.containsKey(key)) {
            try {
                return Integer.parseInt(properties.getProperty(key));
            }
            catch (NumberFormatException fallthrough) {}
        }
        return defaultValue;
    }
    
    public boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        if (properties.containsKey(key)) {
            return "true".equals(properties.getProperty(key));
        }
        return defaultValue;
    }
    
    public double getDoubleProperty(String key) {
        return getDoubleProperty(key, 0.0d);
    }
    
    public double getDoubleProperty(String key, double defaultValue) {
        if (properties.containsKey(key)) {
            try {
                return Double.parseDouble(properties.getProperty(key));
            }
            catch (NumberFormatException fallthrough) {
                // FIXME: should log a warning
            }
        }
        return defaultValue;
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Returns a stringtemplate ST instance using {} as delimiters.
     * @param template
     * @return
     */
    public ST createStringTemplate(String template) {
        return new ST(template, '{', '}');
    }
    
    public ST templatize(String template, char startDelimiter, char endDelimiter) {
        return new ST(template, startDelimiter, endDelimiter);
    }
}
