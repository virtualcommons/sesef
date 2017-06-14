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

    public final static int DEFAULT_FONT_SIZE = 18;

    public static final Font DEFAULT_PLAIN_FONT = new Font(getDefaultFont().getFamily(), Font.PLAIN, DEFAULT_FONT_SIZE);

    public static final Font DEFAULT_BOLD_FONT = new Font(getDefaultFont().getFamily(), Font.BOLD, DEFAULT_FONT_SIZE);

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

    public static final Color OFF_WHITE = new Color(0xf0, 0xf4, 0xfa);

    public static final Color LIGHT_BLUE_GRAY = new Color(0xce, 0xd9, 0xee);

    private static final String JAVAX_JNLP_CLIPBOARD_SERVICE = "javax.jnlp.ClipboardService";
    private static ClipboardService clipboardService;

    public static Font getDefaultFont() {
        return getDefaultFont(30.0f);
    }

    public static Font getDefaultFont(float fontSize) {
        return UIManager.getFont("Label.font").deriveFont(fontSize);
    }

    public static void addStyles(JEditorPane editorPane, int fontSize) {
        editorPane.setContentType("text/html");
        Font font = getDefaultFont(fontSize);
        String bodyCss = String.format("body { font-family: %s; font-size: %d px; padding: 20px 15px 20px 15px; }", font.getFamily(), fontSize);
        String containerCss = ".container { position: relative; margin-left: auto; margin-right: auto; padding: 45px 25px 45px 25px; width: 75%; }";
        String h1 = ".h1 { font-size: 2em !important; }";
        String h2 = ".h2 { font-size: 1.8em !important; }";
        String h3 = ".h3 { font-size: 1.6em !important; }";
        String submitCss = ".btn { width: 100px; height: 33px; }";
        String quizCss = "input { padding-left: 40px; } .incorrect-answer { color: red; } .question { padding: 30px 20px 30px 20px; }";
        addCss(editorPane, bodyCss, containerCss, h1, h2, h3, submitCss, quizCss);
    }

    public static void addCss(JEditorPane editorPane, String ... cssStyles) {
        StyleSheet styleSheet = getStyleSheet(editorPane);
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
        return createInstructionsEditorPane(editable, 30);
    }

    public static HtmlEditorPane createInstructionsEditorPane(boolean editable, int fontSize) {
        final HtmlEditorPane htmlPane = new HtmlEditorPane();
        htmlPane.setEditable(editable);
        htmlPane.setDoubleBuffered(true);
        htmlPane.setBackground(OFF_WHITE);
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
