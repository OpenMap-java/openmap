// **********************************************************************
// <copyright>
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/ContextInfo.java,v $
// $Revision: 1.3 $ $Date: 2004/01/26 18:18:16 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.io.File;
import java.util.*;
import javax.servlet.*;

import com.bbn.openmap.layer.vpf.LibrarySelectionTable;

/**
 * This class holds information retrieved from the ServletContext.
 */
public class ContextInfo {
    /** the name of the attribute where ContextInfo objects are in the 
     * ServletContext  */
    public static final String CONTEXT_INFO = ContextInfo.class.getPackage().getName() + ".contextInfo";
    /** the prefix used to configure VPF libraries in web.xml */
    public static final String LIBRARY_PREFIX = ContextInfo.class.getPackage().getName() + "vpf_library.";

    /** a map from library name to (String) library path */
    private Map lib_pathmap;
    /** a map from library name to LibrarySelectionTable */
    private Map lib_lstmap;
    
    /**
     * A constructor - use getContextInfo to get one.
     * @param context the ServletContext to use to initialize
     * @see #getContextInfo
     */
    private ContextInfo(ServletContext context) {
        lib_pathmap = createLibrariesMap(context);
        lib_lstmap = new HashMap();
    }

    /**
     * Returns the ContextInfo object for the ServletContext.
     * @param context the ServletContext to either get an existing 
     * ContextInfo from, or the context to use to initialize a new ContextInfo,
     * if one doesn't already exist for the context.
     */
    public static synchronized ContextInfo getContextInfo(ServletContext context) {
        ContextInfo ci = (ContextInfo)context.getAttribute(CONTEXT_INFO);
        if (ci == null) {
            ci = new ContextInfo(context);
            context.setAttribute(CONTEXT_INFO, ci);
        }
        return ci;
    }

    /**
     * Grovels through the ServletContext initialization parameters
     * and creates a map from library name to library path.
     * @param context the context to grovel through
     */
    private Map createLibrariesMap(ServletContext context) {
        HashMap library_map = new HashMap();
        for (Enumeration en = context.getInitParameterNames();
             en.hasMoreElements(); ) {
            String s = (String)en.nextElement();
            if (s.startsWith(LIBRARY_PREFIX)) {
                library_map.put(s.substring(LIBRARY_PREFIX.length()),
                                context.getInitParameter(s));
            }
        }
        return Collections.unmodifiableMap(library_map);
    }
    
    /**
     * Return a file object that the path resolves to.
     * @param pathInfo the path to resolve (expected to be of the form
     * "/library_name_in_web_xml/path/to/file")
     * @return a File if it could be resolved, null otherwise
     */
    public String resolvePath(String pathInfo) {
        if ((pathInfo == null) || 
            (pathInfo.indexOf("..") != -1)) { //don't climb out of sandbox
            return null;
        }
        int libStart = pathInfo.indexOf('/') + 1;
        int libEnd = pathInfo.indexOf('/', libStart);
        if (libEnd == -1) {
            libEnd = pathInfo.length();
        }
        String libname = pathInfo.substring(libStart, libEnd);
        String subpath = pathInfo.substring(libEnd);

        String lib_home = getPath(libname);
        if (lib_home == null) {
            return null;
        }

        return lib_home + "/" + subpath;
    }
    
    /**
     * Returns the final path component (minus any trailing '.')
     */
//      public static String getFilename(String filename) {
//      int strlen = filename.length();
//      int firstchar = filename.lastIndexOf('/');
//      int lastchar = filename.endsWith(".")?strlen-1:strlen;
//      return filename.substring(firstchar+1, lastchar);
//     }
    
    /**
     * Returns a Set whose values are the (String) names of the configured
     * libraries.
     * @return a set of library names
     */
    public Set keySet() {
        return new TreeSet(lib_pathmap.keySet());
    }

    /**
     * Returns the path (or null) for the library
     * @param libname the library name
     * @return the path or null
     */    
    public String getPath(String libname) {
        return (String)lib_pathmap.get(libname);
    }
    
    /**
     * Returns the LibrarySelectionTable (or null) for the library
     * @param libname the library name
     * @return the LST or null
     */    
    public LibrarySelectionTable getLST(String libname) {
        return (LibrarySelectionTable)lib_lstmap.get(libname);
    }

    /**
     * Adds an LST for a library
     * @param libname the library name
     * @param lst the LibrarySelectionTable for libname
     */    
    public void putLST(String libname, LibrarySelectionTable lst) {
        lib_lstmap.put(libname, lst);
    }
}
    
