package edu.asu.commons.conf;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.stringtemplate.v4.ST;

import edu.asu.commons.util.Duration;
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
public class PropertiesConfiguration implements Serializable {

    private static final long serialVersionUID = -9093022080387404606L;

    private final Properties properties;

    private final Map<String, Object> cachedPropertyMap = new HashMap<String, Object>();

    public PropertiesConfiguration() {
        this(new Properties());
    }

    public PropertiesConfiguration(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void loadProperties(String resource) {
        InputStream stream = ResourceLoader.toInputStream(resource);
        try {
            properties.loadFromXML(stream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Couldn't loadFromXML using resource: " + resource + " and stream: " + stream, e);
        }
    }

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
            } catch (NumberFormatException fallthrough) {
            }
        }
        return defaultValue;
    }

    public boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        if (properties.containsKey(key)) {
            return "true".equalsIgnoreCase(properties.getProperty(key));
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
            } catch (NumberFormatException fallthrough) {
                // FIXME: should log a warning
            }
        }
        return defaultValue;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public long inMinutes(long seconds) {
        return TimeUnit.MINUTES.convert(seconds, TimeUnit.SECONDS);
    }

    public long inMinutes(Duration duration) {
        return inMinutes(duration.getTimeLeftInSeconds());
    }

    public String toCurrencyString(double amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    /**
     * Returns a stringtemplate ST instance using {} as delimiters.
     * 
     * @param template
     * @return
     */
    public ST createStringTemplate(String template) {
        return createStringTemplate(template, '{', '}', this);
    }

    public ST createStringTemplate(String template, char startDelimiter, char endDelimiter, Object templateModel) {
        ST st = new ST(template, startDelimiter, endDelimiter);
        st.add("self", templateModel);
        return st;
    }

    public String render(String template) {
        return createStringTemplate(template).render();
    }

    public Map<String, Object> toMap(Object configuration) {
        if (!cachedPropertyMap.isEmpty()) {
            return cachedPropertyMap;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(configuration.getClass());
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                Object value = descriptor.getReadMethod().invoke(configuration);
                cachedPropertyMap.put(descriptor.getName(), value);
            }
            return cachedPropertyMap;
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }
}
