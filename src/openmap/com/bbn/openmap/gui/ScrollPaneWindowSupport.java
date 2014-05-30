package com.bbn.openmap.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;

/**
 * Alternative ScrollPaneLayout that returns a preferred layout size that
 * properly takes the scroll bars sizes into account. This is a workaround for a
 * display problem with the Nimbus Look & Feel.
 */
class OMScrollPaneLayout extends ScrollPaneLayout {

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = super.preferredLayoutSize(parent);
        JScrollPane pane = (JScrollPane) parent;
        Component comp = pane.getViewport().getView();
        Dimension viewPref = comp.getPreferredSize();
        Dimension port = pane.getViewport().getExtentSize();

        if (port.height < viewPref.height) {
            dim.width += pane.getVerticalScrollBar().getPreferredSize().width;
        }

        if (port.width < viewPref.width) {
            dim.height += pane.getHorizontalScrollBar().getPreferredSize().height;
        }
        return dim;
    }

}

/**
 * The ScrollPaneWindowSupport class does the same thing as WindowSupport, it
 * just wraps content in a JScrollPane.
 */
public class ScrollPaneWindowSupport extends WindowSupport {

    /**
     * Create the window support.
     * 
     * @param content the content to display in the window.
     * @param windowTitle the title of the window.
     */
    public ScrollPaneWindowSupport(Component content, String windowTitle) {
        super(content, windowTitle);
    }

    /**
     * Wrap content in a JScrollPane.
     */
    public Component modifyContent(Component comp) {

        JScrollPane pane = new JScrollPane(comp);

        // Use an alternative layout in order to properly handle scroll bars
        // with the Nimbus L&F
        pane.setLayout(new OMScrollPaneLayout());

        return pane;

    }

}