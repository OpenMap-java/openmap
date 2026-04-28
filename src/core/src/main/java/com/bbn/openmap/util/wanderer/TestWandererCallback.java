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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/wanderer/TestWandererCallback.java,v $
// $RCSfile: TestWandererCallback.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.wanderer;

import java.io.File;

/**
 * The Wanderer walks through a directory tree, and makes calls to the
 * WandererCallback with what it finds. You add your WandererCallback
 * to the Wanderer, and then just take your action on the files.
 */
public class TestWandererCallback implements WandererCallback {

    public boolean handleDirectory(File directory) {
        System.out.println("Directory - " + directory.getName() + " has "
                + directory.list().length + " item(s)");
        return true;
    }

    public boolean handleFile(File file) {
        System.out.println("File - " + file.getName());
        return true;
    }

}