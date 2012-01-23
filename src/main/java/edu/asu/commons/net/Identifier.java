package edu.asu.commons.net;

import java.io.Serializable;

/**
 * $Id$
 * 
 * Marker interface used to uniquely identify client connections within 
 * the framework.
 * 
 * @author alllee
 * @version $Revision$
 */
public interface Identifier extends Serializable {
    
//    public int getAssignedNumber();
    
//    public void setAssignedNumber(int assignedNumber);
    
    public String getChatHandle();
    public void setChatHandle(String handle);
    public String getSurveyId();
    public void setSurveyId(String surveyId);
    public String getStationId();

	public static final Identifier NULL = new SystemIdentifier() {
        private static final long serialVersionUID = 3451864583823314294L;
    };
    
    public static final Identifier ALL = new SystemIdentifier() {
        private static final long serialVersionUID = -2831336158562033508L;
        @Override
        public String toString() {
            return "All";
        }
        public String getChatHandle() {
            return toString();
        }

    };
    
    public abstract static class SystemIdentifier implements Identifier {
        private static final long serialVersionUID = 3504270875695958633L;
        public String getChatHandle() {
            return "System message";
        }
        public void setChatHandle(String chatHandle) {
            throw new UnsupportedOperationException("Cannot change system chat handle");
        }
        public String getSurveyId() {
            return toString();
        }
        public void setSurveyId(String uniqueId) {
            throw new UnsupportedOperationException("Tried to set survey id on the system identifier.");
        }
        public String getStationId() {
            return toString();
        }
        public String toString() {
            return "system identifier";
        }
        public boolean equals(Object a) {
            return (a instanceof Identifier) && toString().equals(a.toString());
        }
    }
    
    public static class Base<T extends Base<T>> implements Identifier, Comparable<T> {

        private static final long serialVersionUID = -722419864070305185L;

        private final String id;
        private String surveyId;

        private volatile static int ordinal = 0;

        private final int hash;
        
        private String chatHandle;

        public Base() {
            hash = ordinal++;
            id = new StringBuilder().append(System.currentTimeMillis()).append(hash).toString();
        }

        public String toString() {
            return String.format("uid: %s", id);
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
        
        public String getStationId() {
            return toString();
        }
        
        public String getChatHandle() {
            return chatHandle;
        }

        public void setChatHandle(String chatHandle) {
            this.chatHandle = chatHandle;
        }

        public String getSurveyId() {
            return surveyId;
        }

        public void setSurveyId(String surveyId) {
            this.surveyId = surveyId;
        }

    }
}
