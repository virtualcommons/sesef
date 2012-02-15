package edu.asu.commons.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.asu.commons.util.Utils.MapOp;
import static org.junit.Assert.*;

/**
 * $ Id: Exp $
 *  
 *  Exercises Utils class.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class UtilsTest {

    public void testGetTextResource() {
        fail("not implemented yet");
    }

    @Test
    public void testBasename() {
        String windowsPath = "C:\\Program Files\\Java\\jdk1.6.0_07\\bin\\javac.exe";
        String unixPath = "/foo/bar/baz/quux";
        System.err.println("what is: " + System.getProperty("os.name"));
        if (System.getProperty("os.name").contains("Windows")) {
            assertEquals("javac.exe", Utils.basename(windowsPath));
        }
        else {
            assertEquals("quux", Utils.basename(unixPath));
        }
    }

    @Test
    public void testJoin() {
        Integer[] ints = new Integer[] { 1, 2, 3 };
        String joinedInts = Utils.join(',', ints);
        assertEquals(joinedInts, "1,2,3");
        String[] strings = new String[] { "hey", "there", "you're" };
        assertEquals("hey,there,you're", Utils.join(',', strings));
        assertEquals("'hey','there','you\'re'", Utils.join(true, ',', strings));
        
    }

    @Test
    public void testMap() {
        final List<String> strings = new ArrayList<String>();
        for (int i = 0; i < 100; i++) {
            strings.add("" + i);
        }
        List<Integer> ints = Utils.map(strings, new MapOp<String, Integer>() {
            public Integer apply(String string) {
                assertTrue(strings.contains(string));
                return Integer.valueOf(string);
            }
        });
        for (Integer integer : ints) {
            assertTrue(strings.contains(integer.toString()));
        }
    }

}
