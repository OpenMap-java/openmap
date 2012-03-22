//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/JIcon.java,v $
//$RCSfile: JIcon.java,v $
//$Revision: 1.4 $
//$Date: 2004/10/14 18:05:46 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.awt.Image;

/**
 * Structure definition for an image icon
 */
class JIcon {
    public String name;
    public Image icon;
    public Image[] cicon;

    public JIcon(String name) {
        this.name = name;
        icon = null;
        cicon = new Image[8];
    }
}