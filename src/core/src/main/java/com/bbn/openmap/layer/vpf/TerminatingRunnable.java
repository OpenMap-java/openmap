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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/TerminatingRunnable.java,v $
// $RCSfile: TerminatingRunnable.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

/**
 * Classes that implement this interface are Runnable (in the Thread
 * sense), but have run() methods that will terminate in a bounded
 * amount of time.
 * 
 * @see java.lang.Thread
 * @see java.lang.Runnable#run()
 */
public interface TerminatingRunnable extends Runnable {
}