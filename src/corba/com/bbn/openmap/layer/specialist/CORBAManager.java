// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/Attic/CORBAManager.java,v $
// $RCSfile: CORBAManager.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:47 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.specialist;

import java.applet.Applet;

import com.bbn.openmap.Environment;
import com.bbn.openmap.util.Debug;
import org.omg.CORBA.*;

/**
 * @HACK CORBAManager class is a hack workaround broken Visibroker
 * implementation of ORB.init(applet, props) where it spawns two new threads
 * each time it's called.  If it didn't do this stupid thing, then we wouldn't
 * need this class.
 */
public class CORBAManager {
    private static ORB orb = null;

    private CORBAManager() {}

    /**
     * Return a static reference to the ORB.
     *
     * @HACK see above.
     *
     */
    public static ORB getORB() {
	if (orb == null) {
	    Debug.message("basic", "CORBAManager.getORB(): initializing ORB");
	    Applet applet = Environment.getApplet();
	    if (applet == null) {
		if (Debug.debugging("basic")) {
		    System.out.println("CORBAManager: initializing application");
		}
		orb = ORB.init();
	    } else {
		// initialize the Environment with the properties passed in.
		if (Debug.debugging("basic")) {
		    System.out.println("CORBAManager: initializing applet");
		}
		orb = ORB.init(applet, Environment.getProperties());
	    }
	}
	return orb;
    }
}
