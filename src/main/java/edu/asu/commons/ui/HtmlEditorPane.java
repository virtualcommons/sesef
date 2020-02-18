package edu.asu.commons.ui;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 *
 * Provides HTML form processing
 * (inspired by Allen Holub's JavaWorld article)
 *
 * @author <a href='allen.lee@asu.edu'>Allen Lee</a>
 */
@SuppressWarnings("serial")
public final class HtmlEditorPane extends JEditorPane {

    private ActionListener actionListeners;

    public HtmlEditorPane() {
        registerEditorKitForContentType("text/html",
                HtmlEditorPaneKit.class.getName());
        setEditorKit(new HtmlEditorPaneKit());
        setContentType("text/html");
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        // setFont(new Font("Helvetica", Font.TRUETYPE_FONT, 14));

        setEditable(false);
        addHyperlinkListener((HyperlinkEvent e) -> {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            URL url = e.getURL();
            StringBuilder errorMessageBuilder = new StringBuilder("Couldn't display ").append(url);
            if (url == null) {
                errorMessageBuilder.append("No URL found in the event, this is probably due to an error in the foraging configuration.");
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(url.toURI());
                    } catch (IOException | URISyntaxException exception) {
                        exception.printStackTrace();
                        errorMessageBuilder.append(exception.getMessage());
                    }
                    return;
                }
            }
            JOptionPane.showMessageDialog(HtmlEditorPane.this, errorMessageBuilder.toString());
        });
    }

    public HTMLEditorKit getHtmlEditorKit() {
        return (HTMLEditorKit) getEditorKit();
    }

    public StyleSheet getStyleSheet() {
        return getHtmlEditorKit().getStyleSheet();
    }

    /**
     * The {@link JEditorPane} uses an editor kits to get a factory of {@link View} objects, each of which is responsible for rendering
     * an HTML element on the screen. This kit returns a factory that
     * creates custom views, and it also modifies the behavior of the
     * underlying {@link Document} slightly.
     */
    public class HtmlEditorPaneKit extends HTMLEditorKit {
        @Override
        public ViewFactory getViewFactory() {
            return new CustomViewFactory();
        }
    }

    /*******************************************************************
     * Create Views for the various HTML elements. This factory differs from
     * the standard one in that it can create views that handle the
     * modifications that I've made to EditorKit. For the most part, it
     * just delegates to its base class.
     */

    private final class CustomViewFactory extends HTMLEditorKit.HTMLFactory {
        // Create views for elements.
        // Note that the views are not created as the elements
        // are encountered; rather, they're created more or less
        // at random as the elements are displayed. Don't do anything here
        // that depends on the order in which elements appear in the input.
        //
        // Also note that undefined start-element tags are not in any way
        // linked to the matching end-element tag. They two might move
        // around arbitrarily.

        public View create(Element element) { // dumpElement( element );
            HTML.Tag kind = (HTML.Tag) (
                    element.getAttributes().getAttribute(
                            javax.swing.text.StyleConstants.NameAttribute));

            if ((kind == HTML.Tag.INPUT) || (kind == HTML.Tag.SELECT)
                    || (kind == HTML.Tag.TEXTAREA))
            {
                // Create special views that understand Forms and
                // route submit operations to form observers only
                // if observers are registered.
                //
                FormView view = (actionListeners != null)
                        ? new LocalFormView(element)
                        : (FormView) (super.create(element));

                // String type = (String)( element.getAttributes().
                // getAttribute(HTML.Attribute.TYPE));
                return view;
            }
            return super.create(element);
        }
    }

    /*******************************************************************
     * Special handling for elements that can occur inside forms.
     */
    public final class LocalFormView extends FormView
    {
        public LocalFormView(Element element)
        {
            super(element);
        }

        /**
         * Override the base-class method that actually submits the form
         * data to process it locally instead if the URL in the action
         * field matches the "local" URL.
         */
        @Override
        protected void submitData(String data) {
            actionListeners.actionPerformed(new FormActionEvent(data));
        }

        /**
         * Override the base-class image-submit-button class. Given the tag:
         *
         * <PRE>
         *  &lt;input type="image" src="grouchoGlasses.gif"
         *                 name=groucho value="groucho.pressed"&gt;
         * </PRE>
         *
         * The data will hold only two properties:
         *
         * <PRE>
         *  groucho.y=23
         *  groucho.x=58
         * </PRE>
         *
         * Where 23 and 58 are the image-relative positions of the mouse when
         * the user clicked. (Note that the value= field is ignored.)
         * Image tags are useful primarily for implementing a cancel button.
         * <p>
         * This method does nothing but chain to the standard submit- processing code, which can figure out what's going on by looking at the attribute names.
         */
        protected void imageSubmit(String data)
        {
            submitData(data);
        }

        /**
         * Make transparent any standard components used for input.
         * The default components are all opaque with a gray (0xd0d0d0)
         * background, which looks awful when you've set the background
         * color to something else (or set it to an image) in the <code>&lt;body&gt;</code> tag. Setting opaque-mode
         * off lets the specified background color show through the <code>&lt;input&gt;</code> fields.
         */
        public static final float BASELINE_ALIGNMENT = 0.70F;

        protected Component createComponent()
        {
            JComponent widget = (JComponent) (super.createComponent());

            // The widget can be null for things like type=hidden fields

            if (widget != null)
            {
                if (!(widget instanceof JButton))
                    widget.setOpaque(false);

                // Adjust the alignment of everything except multiline text
                // fields so that the control straddles the text baseline
                // instead of sitting on it. This adjustment will make
                // buttons and the text within a single-line text-input
                // field align vertically with any adjacent text.

                if (!(widget instanceof JScrollPane)) // <input>
                {
                    widget.setAlignmentY(BASELINE_ALIGNMENT);
                }
                else
                { // a JList is a <select>, a JTextArea is a <textarea>
                    Component contained =
                            ((JScrollPane) widget).getViewport().getView();

                    // If it's a select, change the width from the default
                    // (full screen) to a bit wider than the actual contained
                    // text.
                    if (contained instanceof JList)
                    {
                        widget.setSize(contained.getPreferredSize());

                        Dimension idealSize = contained.getPreferredSize();
                        idealSize.width += 20;
                        idealSize.height += 5;

                        widget.setMinimumSize(idealSize);
                        widget.setMaximumSize(idealSize);
                        widget.setPreferredSize(idealSize);
                        widget.setSize(idealSize);
                    }
                }
            }
            return widget;
        }
    }

    // @local-form-view-end
    /*******************************************************************
     * Used by {@link HtmlEditorPane} to pass form-submission information to
     * any ActionListener objects.
     * When a form is submitted by the user, an actionPerformed() message
     * that carries a FormActionEvent is sent to all registered
     * action listeners. They can use the event object to get the
     * method and action attributes of the form tag as well as the
     * set of data provided by the form elements.
     */

    public class FormActionEvent extends ActionEvent {
        private final Properties data = new Properties();

        /**
         * @param formData
         *            Data provided by standard HTML element. Data
         *            provided by custom tags is appended to this set.
         */
        private FormActionEvent(String formData)
        {
            super(HtmlEditorPane.this, 0, "submit");
            try {
                // data = UrlUtil.decodeUrlEncoding(data);
                data.load(new ByteArrayInputStream(
                        formData.replaceAll("&", "\n").getBytes())
                );
            } catch (IOException e) {
                throw new RuntimeException("shouldn't happen", e);
            }
        }

        /**
         * Return the a set of properties representing the name=value
         * pairs that would be sent to the server on form submission.
         */
        public Properties getData() {
            return data;
        }

        public String toString() {
            return data.toString();
        }
    }

    /**
     * Chains the given listener to
     */
    public synchronized void addActionListener(ActionListener listener) {
        if (actionListeners == null) {
            actionListeners = listener;
        }
        else {
            actionListeners = AWTEventMulticaster.add(actionListeners, listener);
        }
    }

    public synchronized void setActionListener(ActionListener listener) {
        actionListeners = listener;
    }

    public synchronized void removeActionListener(ActionListener listener) {
        actionListeners = AWTEventMulticaster.remove(actionListeners, listener);
    }

}
