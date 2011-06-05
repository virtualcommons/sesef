package edu.asu.commons.util;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * $Id: Duration.java 450 2010-02-02 20:49:58Z alllee $
 *
 * Inspired/derived from the timeandmoney.sf.net project's API.
 * 
 * @author <a href='allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 450 $
 */
public class Duration implements Serializable {

    private static final long serialVersionUID = -3887670720204302418L;
    
    // FIXME: what's a better name for the amount of time a duration is supposed 
    // to take up?  TimeAndMoney uses 'quantity' I believe.
    // Delta is always specified in milliseconds.
    private final long delta;
    // number of times this Duration has been restart()-ed.
    private long startCount = 0;
    private long startTime;
    private long endTime;
           
    private Duration(long delta, TimeUnit timeUnit) {
        this.delta = TimeUnit.MILLISECONDS.convert(delta, timeUnit);
        initStartEndTime();
    }
    
    private void initStartEndTime() {
        startTime = currentTime();
        endTime = startTime + delta;
    }

    /**
     * FIXME: conversion between int/long seconds/millis is squirrelly and likely
     * to be confusing in the long run.
     * 
     * @param seconds
     * @return a Duration over the number of specified seconds.
     */
    public static Duration create(int seconds) {
        return new Duration(seconds, TimeUnit.SECONDS);
    }
    
    /**
     * FIXME: should just phase these out.
     * @param millis
     * @return a Duration over the number of specified milliseconds.
     */
    public static Duration create(long millis) {
        return new Duration(millis, TimeUnit.MILLISECONDS);
    }
    
    public static Duration create(long delta, TimeUnit timeUnit) {
        return new Duration(delta, timeUnit);
    }
    
    public static long toMillis(int seconds) {
//        return TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
        return seconds * 1000L;
    }
    
    public static int toSeconds(long milliseconds) {
//        return TimeUnit.SECONDS.convert(milliseconds, TimeUnit.MILLISECONDS);
        return (int) (milliseconds / 1000L);
    }
    
    public long getDelta() {
        return delta;
    }

    /**
     * Should split into TimeInterval, TimePoint, Duration?
     * @return
     */
    public Duration start() {
        startCount = 0;
        initStartEndTime();
        return this; 
    }
    public void expire() {
        stop();
    }
    public void stop() {
        endTime = currentTime();
    }
    
    public Duration restart() {
        startCount++;
        initStartEndTime();
        return this;
    }

    public long getStartCount() {
        return startCount;
    }
    
    /**
     * Returns a canonical Duration.  Invoking start() on the resulting Duration
     * will result in an instanced Duration. 
     * @param delta
     * @return
     */
    public Duration plus(long delta) {
        return create(this.delta + delta);
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public long getTimeLeft() {
        return endTime - currentTime();
    }

    public int getTimeLeftInSeconds() {
        return toSeconds( getTimeLeft() );
    }
    
    public long getElapsedTime() {
        return currentTime() - startTime;
    }
    
    public int getElapsedTimeInSeconds() {
        return toSeconds(getElapsedTime());
    }

    public String toString() {
        return String.format("Duration: %d, Time left: %d", delta, getTimeLeft());
    }
    
    public boolean hasExpired() {
        return (currentTime() > endTime);
    }
    
    // FIXME: replace with nanoTime() at some point..?
    private static long currentTime() {
        return System.currentTimeMillis();
    }
}
