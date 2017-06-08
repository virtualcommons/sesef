package edu.asu.commons.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;

import javax.jnlp.ClipboardService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

/**
 * Static utility class for common UI methods and establish consistent look & feel.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
public final class UserInterfaceUtils {

    public static final Font DEFAULT_PLAIN_FONT = new Font(getDefaultFont().getFamily(), Font.PLAIN, 16);
    public static final Font DEFAULT_BOLD_FONT = new Font(getDefaultFont().getFamily(), Font.BOLD, 16);

    /** A very dark red color. */
    public static final Color VERY_DARK_RED = new Color(0x80, 0x00, 0x00);

    /** A dark red color. */
    public static final Color DARK_RED = new Color(0xc0, 0x00, 0x00);

    /** A light red color. */
    public static final Color LIGHT_RED = new Color(0xFF, 0x40, 0x40);

    /** A very light red color. */
    public static final Color VERY_LIGHT_RED = new Color(0xFF, 0x80, 0x80);

    /** A very dark yellow color. */
    public static final Color VERY_DARK_YELLOW = new Color(0x80, 0x80, 0x00);

    /** A dark yellow color. */
    public static final Color DARK_YELLOW = new Color(0xC0, 0xC0, 0x00);

    /** A light yellow color. */
    public static final Color LIGHT_YELLOW = new Color(0xFF, 0xFF, 0x40);

    /** A very light yellow color. */
    public static final Color VERY_LIGHT_YELLOW = new Color(0xFF, 0xFF, 0x80);

    /** A very dark green color. */
    public static final Color VERY_DARK_GREEN = new Color(0x00, 0x80, 0x00);

    /** A dark green color. */
    public static final Color DARK_GREEN = new Color(0x00, 0xC0, 0x00);

    /** A light green color. */
    public static final Color LIGHT_GREEN = new Color(0x40, 0xFF, 0x40);

    /** A very light green color. */
    public static final Color VERY_LIGHT_GREEN = new Color(0x80, 0xFF, 0x80);

    /** A very dark cyan color. */
    public static final Color VERY_DARK_CYAN = new Color(0x00, 0x80, 0x80);

    /** A dark cyan color. */
    public static final Color DARK_CYAN = new Color(0x00, 0xC0, 0xC0);

    /** A light cyan color. */
    public static final Color LIGHT_CYAN = new Color(0x40, 0xFF, 0xFF);

    /** Aa very light cyan color. */
    public static final Color VERY_LIGHT_CYAN = new Color(0x80, 0xFF, 0xFF);

    /** A very dark blue color. */
    public static final Color VERY_DARK_BLUE = new Color(0x00, 0x00, 0x80);

    /** A dark blue color. */
    public static final Color DARK_BLUE = new Color(0x00, 0x00, 0xC0);

    /** A light blue color. */
    public static final Color LIGHT_BLUE = new Color(0x40, 0x40, 0xFF);

    /** A very light blue color. */
    public static final Color VERY_LIGHT_BLUE = new Color(0x80, 0x80, 0xFF);

    /** A very dark magenta/purple color. */
    public static final Color VERY_DARK_MAGENTA = new Color(0x80, 0x00, 0x80);

    /** A dark magenta color. */
    public static final Color DARK_MAGENTA = new Color(0xC0, 0x00, 0xC0);

    /** A light magenta color. */
    public static final Color LIGHT_MAGENTA = new Color(0xFF, 0x40, 0xFF);

    /** A very light magenta color. */
    public static final Color VERY_LIGHT_MAGENTA = new Color(0xFF, 0x80, 0xFF);

    private static final String JAVAX_JNLP_CLIPBOARD_SERVICE = "javax.jnlp.ClipboardService";
    private static ClipboardService clipboardService;

    public static Font getDefaultFont() {
        return UIManager.getFont("Label.font");
    }

    public static void addStyles(JEditorPane editorPane, int fontSize) {
        editorPane.setContentType("text/html");
        Font font = getDefaultFont();
        String bodyCss = String.format("body { font-family: %s; font-size: %d px; padding: 2em 1em;}", font.getFamily(), fontSize);
        String containerCss = ".container { position: relative; margin-left: auto; margin-right: auto; padding-right: 15px; padding-left: 15px; width: 75%; }";
        String h1 = String.format(".h1 { padding: 1em 0 1em 0; font-size: %d px !important; }", (int) Math.floor(fontSize * 2.0d));
        String h2 = String.format(".h2 { font-size: %d px !important; }", (int) Math.floor(fontSize * 1.6d));
        String h3 = String.format(".h3 { font-size: %d px !important; }", (int) Math.floor(fontSize * 1.2d));
        String quizCss = ".incorrect-answer { color: red; }";
        addCss(editorPane, bodyCss, containerCss, h1, h2, h3, quizCss);
    }

    public static void addCss(JEditorPane editorPane, String ... cssStyles) {
        StyleSheet styleSheet = ((HTMLDocument) editorPane.getDocument()).getStyleSheet();
        for (String styleRule : cssStyles) {
            styleSheet.addRule(styleRule);
        }
    }

    public static StyleSheet getStyleSheet(JEditorPane editorPane) {
        return ((HTMLDocument) editorPane.getDocument()).getStyleSheet();
    }

    public static HtmlEditorPane createInstructionsEditorPane() {
        return createInstructionsEditorPane(false);
    }

    public static HtmlEditorPane createInstructionsEditorPane(boolean editable) {
        return createInstructionsEditorPane(editable, 18);
    }

    public static HtmlEditorPane createInstructionsEditorPane(boolean editable, int fontSize) {
        final HtmlEditorPane htmlPane = new HtmlEditorPane();
        htmlPane.setEditable(editable);
        htmlPane.setDoubleBuffered(true);
        htmlPane.setBackground(Color.WHITE);
        UserInterfaceUtils.addStyles(htmlPane, fontSize);
        return htmlPane;
    }

    public static void maximize(JFrame frame) {
        frame.pack();
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public synchronized static ClipboardService getClipboardService() {
        if (clipboardService == null) {
            try {
                clipboardService = (ClipboardService) ServiceManager.lookup(JAVAX_JNLP_CLIPBOARD_SERVICE);
            } catch (UnavailableServiceException e) {
                e.printStackTrace();
            }
        }
        return clipboardService;
    }
}
