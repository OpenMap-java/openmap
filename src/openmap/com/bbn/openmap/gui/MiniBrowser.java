// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MiniBrowser.java,v
// $
// $RCSfile: MiniBrowser.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import com.bbn.openmap.util.Debug;

public class MiniBrowser extends OMComponentPanel implements ActionListener {

    JEditorPane jep;
    Stack stack;
    JButton browserLaunch = null;
    JButton backButton = null;
    JButton dismissButton = null;

    public final static String BackCmd = "back";
    public final static String LaunchBrowserCmd = "browser";

    public MiniBrowser(String content) {
        this("text/html", content);
    }

    public MiniBrowser(String mimeType, String content) {
        this(null, mimeType, content);
    }

    public MiniBrowser(Frame owner, String mimeType, String content) {
        WindowSupport ws = init();
        push(mimeType, content);

        ws.displayInWindow(owner, 200, 200, 300, 300);
    }

    public MiniBrowser(URL url) {
        this(null, url);
    }

    public MiniBrowser(Frame owner, URL url) {
        WindowSupport ws = init();
        push(url);

        ws.displayInWindow(owner, 200, 200, 300, 300);
    }

    protected WindowSupport init() {
        stack = new Stack();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        // Has to happen before first push
        browserLaunch = new JButton("Open in Browser");
        URL url = this.getClass().getResource("w.gif");
        ImageIcon imageIcon = new ImageIcon(url, "Go back");
        backButton = new JButton(imageIcon);
        dismissButton = new JButton("Close");
        //////
        jep = new JEditorPane();
        jep.setEditable(false);
        jep.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    JEditorPane pane = (JEditorPane) e.getSource();

                    if (e instanceof HTMLFrameHyperlinkEvent) {
                        Debug.message("minibrowser",
                                "processing HTMLFrameHyperlinkEvent");
                        HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                        HTMLDocument doc = (HTMLDocument) pane.getDocument();
                        doc.processHTMLFrameHyperlinkEvent(evt);
                    } else {
                        Debug.message("minibrowser",
                                "processing HyperlinkEvent");
                        try {
                            push(e.getURL());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        });

        JScrollPane jsp = new JScrollPane(jep, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1;
        c.weighty = 1;
        gridbag.setConstraints(jsp, c);
        add(jsp);

        JPanel buttonPanel = new JPanel();
        GridBagLayout gridbag2 = new GridBagLayout();
        GridBagConstraints c2 = new GridBagConstraints();
        buttonPanel.setLayout(gridbag2);

        c2.fill = GridBagConstraints.NONE;
        c2.anchor = GridBagConstraints.WEST;
        c2.weightx = 0;
        c2.weighty = 0;

        backButton.setActionCommand(BackCmd);
        backButton.addActionListener(this);
        backButton.setEnabled(false);
        gridbag2.setConstraints(backButton, c2);
        buttonPanel.add(backButton);

        browserLaunch.setActionCommand(LaunchBrowserCmd);
        browserLaunch.addActionListener(this);
        browserLaunch.setVisible(false);
        gridbag2.setConstraints(browserLaunch, c2);
        buttonPanel.add(browserLaunch);

        WindowSupport ws = new WindowSupport(this, "");

        c2.anchor = GridBagConstraints.EAST;
        c2.weightx = 1;
        dismissButton.setActionCommand(WindowSupport.KillWindowCmd);
        dismissButton.addActionListener(ws);
        gridbag2.setConstraints(dismissButton, c2);
        buttonPanel.add(dismissButton);

        ////////////

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.weighty = 0;
        c.gridy = 1;

        gridbag.setConstraints(buttonPanel, c);
        add(buttonPanel);

        // Call displayInWindow on this.
        return ws;
    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();

        try {
            if (command == BackCmd) {
                stack.pop();
                ((MiniBrowserPage) stack.peek()).loadInto(jep);
                if (stack.size() <= 1) {
                    backButton.setEnabled(false);
                }
            } else if (command == LaunchBrowserCmd) {
                ((MiniBrowserPage) stack.peek()).launchInBrowser();
            }
        } catch (EmptyStackException ese) {
            backButton.setEnabled(false);
        } catch (IOException ioe) {
        }
    }

    protected void push(URL newPage) {
        Debug.message("minibrowser", "push(URL)");
        push(new MiniBrowserPage(newPage));
    }

    protected void push(String mimeType, String content) {
        Debug.message("minibrowser", "push(String)");
        push(new MiniBrowserPage(mimeType, content));
    }

    protected void push(MiniBrowserPage mbp) {
        try {
            mbp.loadInto(jep);
            stack.push(mbp);
            if (stack.size() > 1) {
                backButton.setEnabled(true);
            }
        } catch (IOException ioe) {
        }
    }

    protected void enableBrowserLaunch(boolean set) {
        browserLaunch.setVisible(set);
        invalidate();
    }

    protected void finalize() {
        Debug.message("minibrowser", "MiniBrowser getting gc'd");
    }

    public static void display(String content) {
        display("text/html", content);
    }

    public static void display(String mimeType, String content) {
        display(null, mimeType, content);
    }

    public static void display(Frame owner, String mimeType, String content) {
        new MiniBrowser(owner, mimeType, content);
    }

    public static void display(URL url) {
        display(null, url);
    }

    public static void display(Frame owner, URL url) {
        new MiniBrowser(owner, url);
    }

    public static void main(String[] argv) {
        if (argv.length > 0) {
            try {
                new MiniBrowser(new URL(argv[0]));
            } catch (MalformedURLException murle) {
                new MiniBrowser("text/html", argv[0]);
            }
        } else {
            new MiniBrowser("text/html", "String link to the <a href=\"http://openmap.bbn.com\">OpenMap</a> web site");
        }
    }

    public class MiniBrowserPage {
        String content = null;
        String mimeType = null;
        URL url = null;

        public MiniBrowserPage(String mt, String stuff) {
            mimeType = mt;
            content = stuff;
        }

        public MiniBrowserPage(URL page) {
            url = page;
        }

        public void loadInto(JEditorPane jep) throws IOException {
            if (isURL()) {
                Debug.message("minibrowser", "loadInto(URL)");
                jep.setPage(url);
                enableBrowserLaunch(true);
                jep.updateUI();
            } else {
                Debug.message("minibrowser", "loadInto(String)");
                jep.setContentType(mimeType);
                jep.setText(content);
                enableBrowserLaunch(false);
                jep.updateUI();
            }
        }

        public void launchInBrowser() {
            try {
                if (isURL())
                    edu.stanford.ejalbert.BrowserLauncher.openURL(url.toString());
            } catch (IOException ioe) {
                Debug.error("MiniBrowser caught IOException loading webpage ("
                        + url.toString() + ")\n" + ioe.getMessage());
            }
        }

        public boolean isURL() {
            return url != null;
        }
    }
}