package edu.asu.commons.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * $Id$
 * 
 * A utility class to collect static methods that don't really belong anywhere
 * else in the code base.
 * 
 * @author Allen Lee
 * @version $Revision$
 */

public final class Utils {

//    private final static Logger logger = Logger.getLogger(Utils.class.getName());

    // cannot be instantiated or subclassed.
    private Utils() { }
    
    public static void waitOn(final Object lock) {
        synchronized (lock) {
            try { lock.wait(); }
            catch (InterruptedException ignored) { }
        }
    }
    
    public static void notify(final Object lock) {
        synchronized (lock) {
            lock.notify();
        }
    }
    
    public static void sleep(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException ignored) { }
    }

    public static int getNumberOfBytes(Object object) {
        int numberOfBytes = 0;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
            byte[] byteArray = baos.toByteArray();
            numberOfBytes = byteArray.length;
            oos.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return numberOfBytes;
    }

    public static StringBuilder getTextResource(String resource) throws IOException {
        InputStream stream = ResourceLoader.toInputStream(resource);
        return getTextResource(stream);
    }

    private static StringBuilder getTextResource(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        while (reader.ready()) {
            builder.append(reader.readLine());
        }
        return builder;
    }

    public static String basename(String filename) {
        if (filename == null || "".equals(filename)){
            return "";
        }
        return filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
        // could also do
        // return new File(filename).getName();
    }
    
    public static String join(char delimiter, Collection<?> objects) {
        return join(false, delimiter, objects);
    }
    
    /*
     * It is unfortunate that the 'clean' way of doing this using Iterators/for-each
     * is inefficient.
     *
     * If the delimiter also occurs in the list of objects waiting to be
     * toString()-ed, this method does not make sure it escapes them.
     */
    public static String join(char delimiter, Object ... objects) {
        return join(false, delimiter, objects);
    }
    /*
     * It is unfortunate that the 'clean' way of doing this using Iterators/for-each
     * is inefficient.
     *
     * If the delimiter also occurs in the list of objects waiting to be
     * toString()-ed, this method does not make sure it escapes them.
     */
    public static String join(boolean shouldQuote, char delimiter, Object ... objects) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            addString(builder, objects[i].toString().trim(), shouldQuote);

            // append the delimiter until we reach the last element.
            if (i < objects.length - 1) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }
    
    public static String join(char delimiter, int[] ints) {
    	StringBuilder builder = new StringBuilder(ints.length * 2);
    	for (int i = 0; i < ints.length; i++) {
    		builder.append(String.valueOf(ints[i]));
    		if (i < ints.length - 1) {
    			builder.append(delimiter);
    		}
    	}
    	return builder.toString();
    	
    }
    
    private static void addString(StringBuilder builder, String string, boolean shouldQuote) {
        if (shouldQuote) { 
            if (string.contains("'")) {
                string = string.replaceAll("'", "\\'");
            }
            builder.append('\'').append(string).append('\'');
        }
        else {
            builder.append(string);
        }
    }
    
    /*
     * Returns a single String with everything in the List delimited
     * by the specified delimiter and ending in the newline.
     */
    public static String join(boolean shouldQuote, char delimiter, Collection<?> objects) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<?> iter = objects.iterator(); iter.hasNext(); ) {
            String data = iter.next().toString();
            addString(builder, data, shouldQuote);
            if ( iter.hasNext() ) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }
    
    public static List<Integer> iota(int end) {
        return iota(0, end);
    }
    
    public static List<Integer> iota(int start, int end) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = start; i < end; i++) {
            list.add(Integer.valueOf(i));
        }
        return list;
    }

    public static boolean isNullOrEmpty(String string) {
        return (string == null) || string.isEmpty();
    }
 
    public static <T, R> List<R> map(Collection<T> in, MapOp<T, R> op) {
        List<R> out = new ArrayList<R>(in.size());
        for (T t: in) {
            out.add(op.apply(t));
        }
        return out;
    }

    public interface MapOp<T, R> {
        public R apply(T t);
    }
}



