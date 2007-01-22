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
//$RCSfile: ErrWorldFile.java,v $
//$Revision: 1.1 $
//$Date: 2007/01/22 15:47:36 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image;

/**
 * An ErrWorldFile is a World File that can't be handled for some reason. This
 * class is used as a placeholder to let an ImageReader know there is some
 * problem figuring out where or how to locate the associated image file.
 * 
 * @author dietrick
 */
public class ErrWorldFile extends WorldFile {
    protected String problemMessage;

    public ErrWorldFile(String problemMessage) {
        this.problemMessage = problemMessage;
    }

    public String getProblemMessage() {
        return problemMessage;
    }

    public void setProblemMessage(String problemMessage) {
        this.problemMessage = problemMessage;
    }
}
