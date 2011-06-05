package edu.asu.commons.net;

import java.io.Serializable;

/**
 * $Id: Identifier.java 456 2010-02-04 05:20:33Z alllee $
 * 
 * Marker interface used to uniquely identify client connections within 
 * the framework.
 * 
 * @author alllee
 * @version $Revision: 456 $
 */
public interface Identifier extends Serializable {
    
//    public int getAssignedNumber();
    
//    public void setAssignedNumber(int assignedNumber);

	public static final Identifier NULL = new Identifier() {
        private static final long serialVersionUID = 3451864583823314294L;
        @Override
        public String toString() {
            return "Null/System Identifier";
        }
    };
    
    public static final Identifier ALL = new Identifier() {
        private static final long serialVersionUID = -2831336158562033508L;
        @Override
        public String toString() {
            return "All";
        }
        public boolean equals(Object a) {
            return (a instanceof Identifier)
                && ((Identifier) a).toString().equals(toString());
        }
    };
    
    public static class Base implements Identifier {

        private static final long serialVersionUID = -722419864070305185L;

        private final String id;

        private volatile static int ordinal = 0;

        private final int hash;

        public Base() {
            hash = ordinal++;
            id = new StringBuilder().append(System.currentTimeMillis()).append(hash).toString();
        }

        public String toString() {
            return "[unique identifier: " + id + "]";
        }

        public boolean equals(Object o) {
            return (o instanceof Base) && equals((Base) o);
        }

        public boolean equals(Base uid) {
            return (uid != null) && id.equals(uid.id);
        }
        
        public int index() {
            return hash;
        }

        public int hashCode() {
            return id.hashCode();
        }

        public int compareTo(Object o) {
            return this.compareTo((Base) o);
        }

        public int compareTo(Base uid) {
            if (uid == null) {
                return 1;
            }
            if (this.id.equals(uid.id)) {
                return 0;
            }
            return hash - uid.hash;
        }
    }
}
