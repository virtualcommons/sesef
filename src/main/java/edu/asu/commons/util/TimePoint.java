package edu.asu.commons.util;

import java.io.Serializable;

/**
 * $Id: TimePoint.java 1 2008-07-23 22:15:18Z alllee $
 * 
 * Represents a snapshot in time. Inspired by the timeandmoney sf project,
 * simplified for this framework's use.
 * 
 * @author <a href='alllee@cs.indiana.edu'>Allen Lee</a>
 * @version $Revision: 1 $
 */
public class TimePoint implements Comparable<TimePoint>, Serializable {

    private static final long serialVersionUID = 931849455738064312L;
    private final long millisecondsSinceEpoch;

    public TimePoint() {
        this(System.currentTimeMillis());
    }

    public TimePoint(long millis) {
        this.millisecondsSinceEpoch = millis;
    }

    public static TimePoint currentTime() {
        return new TimePoint();
    }

    public long getMillisecondsSinceEpoch() {
        return millisecondsSinceEpoch;
    }

    public boolean isAfter(TimePoint point) {
        return millisecondsSinceEpoch > point.millisecondsSinceEpoch;
    }

    public boolean isBefore(TimePoint point) {
        return millisecondsSinceEpoch < point.millisecondsSinceEpoch;
    }

    public Duration minus(TimePoint point) {
        return Duration.create(millisecondsSinceEpoch - point.millisecondsSinceEpoch);
    }

    public boolean equals(Object object) {
        return (object == this)
                || ((object instanceof TimePoint)
                && ((TimePoint) object).getMillisecondsSinceEpoch() ==
                getMillisecondsSinceEpoch());
    }

    public int hashCode() {
        return (int) ((millisecondsSinceEpoch * 237) ^ millisecondsSinceEpoch);
    }

    public int compareTo(TimePoint point) {
        if (isBefore(point))
            return -1;
        if (isAfter(point))
            return 1;
        return 0;
        // FIXME: potentially unsafe int conversion.
        // return (int) (getMillisecondsSinceEpoch() - point.getMillisecondsSinceEpoch());
    }

}
