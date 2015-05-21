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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/SymbolReferenceLibrary.java,v $
// $RCSfile: SymbolReferenceLibrary.java,v $
// $Revision: 1.12 $
// $Date: 2005/08/11 20:39:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The SymbolReferenceLibrary is a organizational class for creating
 * and managing a SymbolPart tree. It can handle requests for decoding
 * a Symbol code and providing a SymbolPart for that code, and can
 * fetch ImageIcons for codes and SymbolParts. This is the class you
 * can put in the MapHandler for other components to use in order to
 * create symbols. The DemoLayer, for instance, uses the
 * MapHandler.findAndInit() method to find it and create a space
 * shuttle icon.
 * <P>
 * 
 * This SymbolReferenceLibrary has one property that lets you specify
 * the SymbolImageMaker it should use to create image icons.
 * 
 * <pre>
 *  
 *   
 *   symbolreferencelibrary.class=com.bbn.openmap.tools.symbology.milStd2525.SymbolReferenceLibrary
 *   
 *   # properties to use for GIF image data files
 *   symbolreferencelibrary.imageMakerClass=com.bbn.openmap.tools.symbology.milStd2525.GIFSymbolImageMaker
 *   # optional property used as a path to the parent of the data files if it's not in the classpath.
 *   symbolreferencelibrary.path=&lt;path to image data file directory&gt;
 *   # optional background color for icons if you want something other than clear.
 *   symbolreferencelibrary.background=AAGGRRBB
 *   
 *   # properties to use for SVG image data files obtained from DISA.  You need to have Batik jars in 
 *   # your classpath to use the SVG files, as well as the omsvg.jar file
 *   symbolreferencelibrary.imageMakerClass=com.bbn.openmap.tools.symbology.milStd2525.SVGSymbolImageMaker
 *   # optional property used as a path to the parent of the data files if it's not in the classpath.
 *   symbolreferencelibrary.path=&lt;path to image data file directory&gt;
 *   # optional background color for icons if you want something other than clear.
 *   symbolreferencelibrary.background=AAGGRRBB
 *  
 * </pre>
 */
public class SymbolReferenceLibrary extends OMComponent {

    public final static String SymbolImageMakerClassProperty = "imageMakerClass";

    protected SymbolPart head;
    protected CodePositionTree positionTree;
    protected static SymbolReferenceLibrary library = null;
    protected CodeOptions symbolAttributes;
    protected SymbolImageMaker symbolImageMaker;

    /**
     * A constructor used when the SymbolImageMaker will be set later,
     * either via the setProperties method or programmatically.
     */
    public SymbolReferenceLibrary() {
        this(null);
    }

    /**
     * Create a SymbolReferenceLibrary with a SymbolImageMaker to use
     * to create images from a data source. It's expected that the
     * SymbolImageMaker is ready to go, configured to create images
     * given a code. The SRL will use the hierarchy.properties file
     * found as a resource as the reference for the symbol hierarchy.
     * 
     * @param sim
     */
    public SymbolReferenceLibrary(SymbolImageMaker sim) {
        Properties props = findAndLoadProperties("hierarchy.properties");
        if (props != null) {
            initialize(props, sim);
        }
    }

    public Properties findAndLoadProperties(String propertiesResource) {
        try {
            URL url = PropUtils.getResourceOrFileOrURL(SymbolReferenceLibrary.class,
                    propertiesResource);
            Properties props = new Properties();
            props.load(url.openStream());
            return props;
        } catch (java.net.MalformedURLException murle) {
            Debug.output("SymbolReferenceLibrary has malformed path to "
                    + propertiesResource);
        } catch (java.io.IOException ioe) {
            Debug.output("SymbolReferenceLibrary I/O exception reading "
                    + propertiesResource);
        }
        return null;
    }

    protected void initialize(Properties props, SymbolImageMaker sim) {
        symbolImageMaker = sim;

        if (Debug.debugging("symbology")) {
            Debug.output("SRL: loading");
        }

        Properties positionProperties = findAndLoadProperties("positions.properties");
        if (positionProperties != null) {
            positionTree = new CodePositionTree(positionProperties);
            head = positionTree.parseHierarchy("MIL-STD-2525B Symbology", props);
        }

        if (Debug.debugging("symbology")) {
            Debug.output("SRL: initialized");
        }

    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        String symbolImageMakerClassString = props.getProperty(realPrefix
                + SymbolImageMakerClassProperty);
        if (symbolImageMakerClassString != null) {
            symbolImageMaker = setSymbolImageMaker(symbolImageMakerClassString);
            if (symbolImageMaker != null) {
                symbolImageMaker.setProperties(prefix, props);
            }
        }
    }

    public SymbolImageMaker setSymbolImageMaker(String classname) {
        try {
            setSymbolImageMaker((SymbolImageMaker) Class.forName(classname)
                    .newInstance());
            return getSymbolImageMaker();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CodeOptions getCodeOptions() {
        if (positionTree != null) {
            return positionTree.getCodeOptions(null);
        }
        return null;
    }

    /**
     * Given a SymbolPart, return what options are available for it.
     * This depends on what scheme the SymbolPart inherits from. Not
     * implemented.
     * 
     * @param sp The SymbolPart in question.
     * @param co Current settings that may be in use.
     */
    public CodeOptions getCodeOptions(SymbolPart sp, CodeOptions co) {
        return null;
    }

    /**
     * Return an image given a SymbolCode and dimension.
     */
    public ImageIcon getIcon(String symbolCode, Dimension di) {
        if (Debug.debugging("symbology")) {
            Debug.output("SymbolReferenceLibrary asked to create: "
                    + symbolCode + " at " + di);
        }
        if (symbolImageMaker != null) {
            return symbolImageMaker.getIcon(symbolCode, di);
        }
        return null;
    }

    /**
     * Return an image for a particular SymbolPart, its options and
     * dimensions. Not implemented.
     */
    public ImageIcon getIcon(SymbolPart sp, CodeOptions co, Dimension di) {
        return null;
    }

    /**
     * Return the 15 character character string representing a
     * SymbolPart with CodeOptions. Not implemented.
     */
    public String getSymbolCode(SymbolPart sp, CodeOptions co) {
        return null;
    }

    /**
     * The SymbolParts in the library are stored in a tree hierarchy,
     * and this method gets the top level one representing the
     * MIL-STD-2525 tree.
     */
    public SymbolPart getHead() {
        return head;
    }

    /**
     * Returns a huge, multi-line string listing all of the symbols,
     * their names and their relation to each other.
     */
    public String getDescription() {
        String description = null;
        if (head != null) {
            description = head.getDescription();
        }
        return description;
    }

    /**
     * Check to see if code exists, if it's valid. If you get a
     * SymbolPart back from this query, then you can call getIcon.
     * 
     * @param code
     * @return SymbolPart for the 15 character code of a symbol.
     */
    public SymbolPart getSymbolPartForCode(String code) {

        if (Debug.debugging("symbology.detail")) {
            Debug.output("SymbolReferenceLibrary checking for " + code
                    + " in SymbolPart tree.");
        }

        if (head.codeMatches(code)) {
            return getSymbolPartForCodeStartingAt(head, code);
        } else {
            return null;
        }
    }

    protected SymbolPart getSymbolPartForCodeStartingAt(SymbolPart node,
                                                        String code) {
        List sublist = node.getSubs();

        if (sublist == null || sublist.isEmpty()) {
            // Last on the line of the tree, this is the closest match...
            return node;
        }
        
        for (Iterator it = sublist.iterator(); it.hasNext();) {
            SymbolPart ssp = (SymbolPart) it.next();

            try {
                char ch = code.charAt(ssp.getCodePosition().startIndex);
                if (ch == '-' || ch == '*')
                    return node;

                if (ssp.codeMatches(code)) {
                    return getSymbolPartForCodeStartingAt(ssp, code);
                }
            } catch (StringIndexOutOfBoundsException sioobe) {
            } catch (NullPointerException npe) {
            }
        }

        return node;
    }

    public static void main(String[] argv) {
        Debug.init();
        Debug.put("codeposition");
        SymbolReferenceLibrary srl = new SymbolReferenceLibrary();
        Debug.output(srl.getDescription());
    }

    /**
     * @return Returns the symbolImageMaker.
     */
    public SymbolImageMaker getSymbolImageMaker() {
        return symbolImageMaker;
    }

    /**
     * @param symbolImageMaker The symbolImageMaker to set.
     */
    public void setSymbolImageMaker(SymbolImageMaker symbolImageMaker) {
        this.symbolImageMaker = symbolImageMaker;
    }

}