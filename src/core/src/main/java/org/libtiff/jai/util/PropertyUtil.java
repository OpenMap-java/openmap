package org.libtiff.jai.util;

/*
 * XTIFF: eXtensible TIFF libraries for JAI.
 * 
 * The contents of this file are subject to the  JAVA ADVANCED IMAGING
 * SAMPLE INPUT-OUTPUT CODECS AND WIDGET HANDLING SOURCE CODE  License
 * Version 1.0 (the "License"); You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.sun.com/software/imaging/JAI/index.html
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License. 
 *
 * The Original Code is JAVA ADVANCED IMAGING SAMPLE INPUT-OUTPUT CODECS
 * AND WIDGET HANDLING SOURCE CODE. 
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 * Portions created by: Niles Ritter 
 * are Copyright (C): Niles Ritter, GeoTIFF.org, 1999,2000.
 * All Rights Reserved.
 * Contributor(s): Niles Ritter
 */

import java.util.Locale;
import java.util.ResourceBundle;

public class PropertyUtil {

    private static ResourceBundle b;

    /** Get bundle from .properties files in the package path */
    private static ResourceBundle getBundle() {
        ResourceBundle bundle = null;

        try {
            bundle = ResourceBundle.getBundle("org.libtiff.jai.codec.xtiff", Locale.US);
            return bundle;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getString(String key) {
        if (b == null) {
            b = getBundle();
        }
        return b.getString(key);
    }
}
