package com.bbn.openmap.graphicLoader.netmap;

import java.awt.*;

/** 
 * Structure definition for an image icon
 */
class JIcon {
    public String       name;
    public Image        icon;
    public Image[]      cicon;
    
    public JIcon( String name) {
        this.name = name;
        icon = null;
        cicon = new Image[8];
    }
}
