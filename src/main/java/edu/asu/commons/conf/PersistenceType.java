package edu.asu.commons.conf;

public enum PersistenceType {
    ALL, XML, BINARY;

    public boolean isXmlEnabled() {
        return this == ALL || this == XML;
    }
}
