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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/I18n.java,v $
// $RCSfile: I18n.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

import com.bbn.openmap.util.Debug;

/**
 * OpenMap Internationalization class.
 *
 * HACK this needs major reworking or needs to be thrown out and something new
 * done.
 */
public class I18n {
    ResourceBundle b = null;
   
    public I18n(String I18nFile) {
        // HACK we need to handle applets...
        try {
            if (Environment.isApplication()) {
                b = ResourceBundle.getBundle("com.bbn.openmap." + I18nFile);
            }
        }
        catch (RuntimeException e) {
	    Debug.message("I18n", "I18n(): can't find bundle: " +
		    "com.bbn.openmap." + I18nFile);
            b = null;
        }
    }
    
    /**
     * get the internationalized string.<p>
     *
     * @param label label metaname
     * @param defaultLabel default English name
     * @return the current locale equivalent (if property list loaded)
     */
    public String get(String label, String defaultLabel) {
        if (b != null) {
            try {
                return b.getString(label/* + ".label"*/);
            }
            catch (MissingResourceException e) {
                Debug.message("I18n", "I18n.get(): " + e);
            }
        }
        return defaultLabel;
    }
}
