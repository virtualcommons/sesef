package edu.asu.commons.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * $Id$
 * 
 * Used to select data from a JEditorPane to copy over to a ClipboardService.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class HtmlSelection implements Transferable {

    private static DataFlavor[] htmlFlavors = new DataFlavor[3];
    private final String html;
    static {
        try {
            htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String");
            htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader");
            htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public HtmlSelection(String html) {
        this.html = html;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return htmlFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Arrays.asList(htmlFlavors).contains(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (String.class.equals(flavor.getRepresentationClass())) {
            return html;
        }
        else if (Reader.class.equals(flavor.getRepresentationClass())) {
            return new StringReader(html);
        }
        else if (InputStream.class.equals(flavor.getRepresentationClass())) {
            return new ByteArrayInputStream(html.getBytes());
        }
        throw new UnsupportedFlavorException(flavor);
    }

}