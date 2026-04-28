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
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: ErrImageTile.java,v $
//$Revision: 1.1 $
//$Date: 2007/01/22 15:47:35 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image;

import com.bbn.openmap.proj.Projection;

/**
 * An ErrImageTile is an ImageTile that can't be displayed due to projection
 * incompatibilities or some other problem. It's a space-holder that can be
 * queried for information about the file and also provide a GUI-component-like
 * list object that can be used to represent a file that was decoded but not
 * able to be displayed for some reason.
 * 
 * @author dietrick
 */
public class ErrImageTile extends ImageTile {

    protected String problemMessage;

    public ErrImageTile(String problemMessage) {
        this.problemMessage = problemMessage;
    }

    public String getProblemMessage() {
        return problemMessage;
    }

    public void setProblemMessage(String problemMessage) {
        this.problemMessage = problemMessage;
    }

    public boolean isVisible() {
        return false;
    }

    public boolean generate(Projection proj) {
        return false;
    }
}
