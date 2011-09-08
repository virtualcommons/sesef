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
    
    public String getChatHandle();

	public static final Identifier NULL = new Identifier() {
        private static final long serialVersionUID = 3451864583823314294L;
        @Override
        public String toString() {
            return "Null/System Identifier";
        }
        public String getChatHandle() {
            return "System message";
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
        public String getChatHandle() {
            return toString();
        }
    };
    
    public static class Base<T extends Base<T>> implements Identifier, Comparable<T> {

        private static final long serialVersionUID = -722419864070305185L;

        private final String id;

        private volatile static int ordinal = 0;

        private final int hash;
        
        private String chatHandle;

        public Base() {
            hash = ordinal++;
            id = new StringBuilder().append(System.currentTimeMillis()).append(hash).toString();
        }

        public String toString() {
            return "[unique identifier: " + id + "]";
        }
        
        public int index() {
            return hash;
        }

        public int hashCode() {
            return id.hashCode();
        }

        public int compareTo(T uid) {
            if (uid == null) {
                return 1;
            }
            if (this.id.equals(uid.id)) {
                return 0;
            }
            return hash - uid.hash;
        }
        
        public String getChatHandle() {
            return chatHandle;
        }

        public void setChatHandle(String chatHandle) {
            this.chatHandle = chatHandle;
        }
    }
}
