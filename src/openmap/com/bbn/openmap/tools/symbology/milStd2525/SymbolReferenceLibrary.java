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
// $Revision: 1.2 $
// $Date: 2003/12/16 01:08:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.net.URL;
import java.util.Properties;
import javax.swing.ImageIcon;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 */
public class SymbolReferenceLibrary {

    protected SymbolPart head;
    protected CodePositionTree positionTree;
    protected static SymbolReferenceLibrary library = null;

    public SymbolReferenceLibrary() {
	Properties props = getProperties("heirarchy.properties");
	if (props != null) {
	    initialize(props);
	}
    }

    public SymbolReferenceLibrary(Properties props) {
	initialize(props);
    }

    public Properties getProperties(String propertiesResource) {
	try {
	    URL url = PropUtils.getResourceOrFileOrURL(SymbolReferenceLibrary.class, 
						       propertiesResource);
	    Properties props = new Properties();
	    props.load(url.openStream());
	    return props;
	} catch (java.net.MalformedURLException murle) {
	    Debug.output("SymbolReferenceLibrary has malformed path to " + propertiesResource);
	} catch (java.io.IOException ioe) {
	    Debug.output("SymbolReferenceLibrary I/O exception reading " + propertiesResource);
	}
	return null;
    }

    protected void initialize(Properties props) {
	if (props == null) {
	    // Get the heirarchy.properties file as a local resource
	    // and load that.
	}

	if (Debug.debugging("symbology")) {
	    Debug.output("SRL: loading");
	}

	Properties positionProperties = getProperties("positions.properties");
	if (positionProperties != null) {
	    positionTree = new CodePositionTree(positionProperties);
	    head = positionTree.parseHeirarchy("MIL-STD-2525B Symbology", props);

	    CodeAffiliation ca = new CodeAffiliation();
	    ca.parsePositions("affiliation", positionProperties);

	    CodeWarfightingModifier cwm = new CodeWarfightingModifier();
	    CodeSizeModifier csm = new CodeSizeModifier();
	    cwm.parsePositions("modifiers", positionProperties);
	    csm.parsePositions("modifiers", positionProperties);

	    CodeStatus cs = new CodeStatus();
	    cs.parsePositions("status", positionProperties);

	    CodeOrderOfBattle coob = new CodeOrderOfBattle();
	    coob.parsePositions("oob", positionProperties);
	}

//     public List getDefaultFlags() {
// 	List schemeList = (List)positions.get(getPrettyName());
// 	if (schemeList == null) {
// 	    // Might as well get them all loaded...
// 	    // WARFIGHTING
// 	    schemeList = new ArrayList();
// 	    schemeList.add(CodeAffiliation.class);
// 	    schemeList.add(CodeStatus.class);
// 	    schemeList.add(CodeWarfightingModifier.class);
// 	    schemeList.add(CodeCountry.class);
// 	    schemeList.add(CodeOrderOfBattle.class);
// 	    positions.put(WARFIGHTING.getPrettyName(), schemeList);
// 	    // TACTICAL_GRAPHICS
// 	    schemeList = new ArrayList();
// 	    schemeList.add(CodeAffiliation.class);
// 	    schemeList.add(CodeStatus.class);
// 	    schemeList.add(CodeSize.class);
// 	    schemeList.add(CodeCountry.class);
// 	    positions.put(TACTICAL_GRAPHICS.getPrettyName(), schemeList);
// 	    // METOC
// 	    schemeList = new ArrayList();
// 	    positions.put(METOC.getPrettyName(), schemeList);
// 	    // INTELLIGENCE
// 	    schemeList = new ArrayList();
// 	    schemeList.add(CodeAffiliation.class);
// 	    schemeList.add(CodeStatus.class);
// 	    schemeList.add(CodeCountry.class);	
// 	    schemeList.add(CodeOrderOfBattle.class);
// 	    positions.put(INTELLIGENCE.getPrettyName(), schemeList);
// 	    // MOOTW
// 	    schemeList = new ArrayList();
// 	    schemeList.add(CodeAffiliation.class);
// 	    schemeList.add(CodeStatus.class);
// 	    schemeList.add(CodeMOOTWModifier.class);
// 	    schemeList.add(CodeCountry.class);
// 	    schemeList.add(CodeOrderOfBattle.class);
// 	    positions.put(MOOTW.getPrettyName(), schemeList);

// 	    schemeList = (List)positions.get(getPrettyName());

// 	}
// 	return schemeList;
//     }

	if (Debug.debugging("symbology")) {
	    Debug.output("SRL: initialized");
	}

    }

    /**
     * Given a SymbolPart, return what options are available for it.
     * This depends on what scheme the SymbolPart inherits from.
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
	return null;
    }

    /**
     * Return an image for a particular SymbolPart, its options and dimensions.
     */
    public ImageIcon getIcon(SymbolPart sp, CodeOptions co, Dimension di) {
	return null;
    }

    /**
     * Return the 15 character character string representing a
     * SymbolPart with CodeOptions.
     */
    public String getSymbolCode(SymbolPart sp, CodeOptions co) {
	return null;
    }

    /**
     * The SymbolParts in the library are stored in a tree heirarchy,
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

    public static void main(String[] argv) {
	Debug.init();
	SymbolReferenceLibrary srl = new SymbolReferenceLibrary();
	if (Debug.debugging("symbology")) {
	    Debug.output(srl.getDescription());
	}
    }
}
