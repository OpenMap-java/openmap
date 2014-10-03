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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/wanderer/WandererCallback.java,v $
// $RCSfile: WandererCallback.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.wanderer;

import java.io.File;

/**
 * The Wanderer walks through a directory tree, and makes calls to the
 * WandererCallback with what it finds. You add your WandererCallback to the
 * Wanderer, and then just take your action on the files.
 */
public interface WandererCallback {

   /**
    * Do what you need to do to the directory.
    * 
    * @param directory that represents a directory to deal with.
    * @return true to continue wandering through to children.
    */
   public boolean handleDirectory(File directory);

   /**
    * Do what you need to do to the file.
    * 
    * @param file to handle.
    * @return true to continue evaluating siblings and parent directory
    *         siblings.
    */
   public boolean handleFile(File file);
}