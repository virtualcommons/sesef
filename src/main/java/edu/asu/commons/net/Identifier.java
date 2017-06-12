package edu.asu.commons.net;

import java.io.Serializable;
import java.util.UUID;

/**
 * Marker interface used to uniquely identify client connections within the
 * framework.
 * 
 * @author Allen Lee
 */
public interface Identifier extends Serializable {

    String getChatHandle();

    void setChatHandle(String handle);

    String getSurveyId();

    void setSurveyId(String surveyId);

    String getStationId();

    int getStationNumber();

    UUID getUUID();

    Identifier NULL = new SystemIdentifier() {
        private static final long serialVersionUID = 3451864583823314294L;
    };

    Identifier ALL = new SystemIdentifier() {
        private static final long serialVersionUID = -2831336158562033508L;

        @Override
        public String toString() {
            return "All";
        }

        @Override
        public String getChatHandle() {
            return toString();
        }

    };

    abstract class SystemIdentifier implements Identifier {
        private static final long serialVersionUID = 3504270875695958633L;

        private final UUID uuid = UUID.randomUUID();

        public String getChatHandle() {
            return "System message";
        }

        public void setChatHandle(String chatHandle) {
            throw new UnsupportedOperationException(
                    "Cannot change system chat handle");
        }

        public String getSurveyId() {
            return toString();
        }

        public void setSurveyId(String uniqueId) {
            throw new UnsupportedOperationException(
                    "Tried to set survey id on the system identifier.");
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getStationId() {
            return toString();
        }

        public int getStationNumber() {
            return -1;
        }

        public String toString() {
            return "system identifier";
        }

        public boolean equals(Object a) {
            return (a instanceof Identifier) && ((Identifier) a).getUUID().equals(getUUID());
        }
    }

    class Base<T extends Base<T>> implements Identifier, Comparable<T> {

        private static final long serialVersionUID = -722419864070305185L;

        private final String id;
        private final UUID uuid;
        private String surveyId;

        private volatile static int ordinal = 0;

        private final int hash;

        private String chatHandle;

        public Base() {
            hash = ordinal++;
            id = new StringBuilder().append(System.currentTimeMillis())
                    .append(hash).toString();
            uuid = UUID.randomUUID();
        }

        public String toString() {
            return String.format("uid: %s", id);
        }

        public int index() {
            return hash;
        }

        public int hashCode() {
            return uuid.hashCode();
        }
        
        public boolean equals(Object other) {
            if (other instanceof Identifier.Base) {
                return uuid.equals(((Identifier) other).getUUID());
            }
            return false;
        }

        public int compareTo(T other) {
            if (other == null) {
                throw new NullPointerException("Cannot compare " + this + " to null");
            }
            if (this == other) {
                return 0;
            }
            return Integer.valueOf(index()).compareTo(other.index());
        }

        public String getStationId() {
            return toString();
        }

        public int getStationNumber() {
            return hash;
        }

        public String getChatHandle() {
            return chatHandle;
        }

        public void setChatHandle(String chatHandle) {
            this.chatHandle = chatHandle;
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getSurveyId() {
            return surveyId;
        }

        public void setSurveyId(String surveyId) {
            this.surveyId = surveyId;
        }

    }

    class Mock extends Base<Mock> {
        private static final long serialVersionUID = 4306532617547585781L;
    }
}
