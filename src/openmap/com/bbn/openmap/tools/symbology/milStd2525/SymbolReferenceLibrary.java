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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/SymbolReferenceLibrary.java,v $
// $RCSfile: SymbolReferenceLibrary.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/08 18:37:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.net.URL;
import java.util.Properties;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 */
public class SymbolReferenceLibrary {

    protected SymbolPart head;
    protected static SymbolReferenceLibrary library = null;

    public SymbolReferenceLibrary() {
	this(null);
    }

    public SymbolReferenceLibrary(Properties props) {
	initialize(props);
    }

    protected void initialize(Properties props) {
	if (props == null) {
	    // Get the heirarchy.properties file as a local resource
	    // and load that.
	}

	if (Debug.debugging("symbology")) {
	    Debug.output("SRL: loading");
	}

 	head = new SymbolPart(props);

	if (Debug.debugging("symbology")) {
	    Debug.output("SRL: initialized");
	}
    }

    public SymbolPart getHead() {
	return head;
    }

    public String getDescription() {
	String description = null;
	if (head != null) {
	    description = head.getDescription();
	}
	return description;
    }

    public static void main(String[] argv) {
	try {
	    Debug.init();

	    URL url = PropUtils.getResourceOrFileOrURL(SymbolReferenceLibrary.class, 
						       "heirarchy.properties");
	    Properties props = new Properties();
	    props.load(url.openStream());
	    if (url != null) {
		SymbolReferenceLibrary srl = new SymbolReferenceLibrary(props);
		if (Debug.debugging("symbology")) {
		    Debug.output(srl.getDescription());
		}
	    }
	    
	} catch (java.net.MalformedURLException murle) {
	    Debug.output("SymbolReferenceLibrary MURLE");
	} catch (java.io.IOException ioe) {
	    Debug.output("SymbolReferenceLibrary IOException");
	}

    }

}
