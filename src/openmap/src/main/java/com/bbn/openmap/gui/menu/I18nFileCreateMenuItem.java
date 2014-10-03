//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: I18nFileCreateMenuItem.java,v $
//$Revision: 1.1 $
//$Date: 2005/02/11 22:30:29 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import com.bbn.openmap.BasicI18n;
import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;

public class I18nFileCreateMenuItem extends JMenuItem implements ActionListener {

    /**
     * 
     */
    public I18nFileCreateMenuItem() {
        super("Create I18N Resource Files...");
        addActionListener(this);
    }

    /**
     * @param icon
     */
    public I18nFileCreateMenuItem(Icon icon) {
        super(icon);
    }

    /**
     * @param text
     */
    public I18nFileCreateMenuItem(String text) {
        super(text);
    }

    /**
     * @param a
     */
    public I18nFileCreateMenuItem(Action a) {
        super(a);
    }

    /**
     * @param text
     * @param icon
     */
    public I18nFileCreateMenuItem(String text, Icon icon) {
        super(text, icon);
    }

    /**
     * @param text
     * @param mnemonic
     */
    public I18nFileCreateMenuItem(String text, int mnemonic) {
        super(text, mnemonic);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        I18n i18n = Environment.getI18n();
        if (i18n instanceof BasicI18n) {
            ((BasicI18n)i18n).dumpCreatedResourceBundles();
        }
    }

}
