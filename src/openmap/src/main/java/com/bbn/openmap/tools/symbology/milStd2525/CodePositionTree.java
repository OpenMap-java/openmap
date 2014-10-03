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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodePositionTree.java,v $
// $RCSfile: CodePositionTree.java,v $
// $Revision: 1.7 $
// $Date: 2004/12/08 01:08:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.Debug;

/**
 * The CodePositionTree is a CodeScheme CodePosition object that has
 * some intelligence on how to starting building the SymbolPart tree
 * for the SymbolReferenceLibrary. It knows how to set up CodeOptions
 * for the different types of CodeSchemes, and starts the parsing of
 * the hierarchy properties.
 */
public class CodePositionTree extends CodeScheme {

    public CodePositionTree(Properties positionProperties) {
        // Read CodeSchemes, build position tree. Then, this
        // CodePosition can be used to build the hierarchal symbol
        // tree from the head SymbolPart

        CodeScheme cs = new CodeScheme();
        cs.parsePositions("scheme", positionProperties);
        // This is needed to parse hierarchy...
        choices = cs.getPositionChoices();

        //      parsePositions("scheme", positionProperties);
        //      // This is needed to parse hierarchy...
        //      choices = cs.getPositionChoices();

        // Read Optional Flag Positions
        CodeAffiliation ca = new CodeAffiliation();
        ca.parsePositions("affiliation", positionProperties);
        ca.choices.add(0, ca.getNULLCodePosition());

        CodeWarfightingModifier cwm = new CodeWarfightingModifier();
        CodeSizeModifier csm = new CodeSizeModifier();
        CodeMOOTWModifier cmm = new CodeMOOTWModifier();
        cwm.parsePositions("modifiers", positionProperties);
        cwm.choices.add(0, cwm.getNULLCodePosition());
        csm.parsePositions("modifiers", positionProperties);
        csm.choices.add(0, csm.getNULLCodePosition());
        cmm.parsePositions("modifiers", positionProperties);
        cmm.choices.add(0, cmm.getNULLCodePosition());

        CodeStatus cstatus = new CodeStatus();
        cstatus.parsePositions("status", positionProperties);
        cstatus.choices.add(0, cstatus.getNULLCodePosition());
        
        CodeOrderOfBattle coob = new CodeOrderOfBattle();
        coob.parsePositions("oob", positionProperties);
        coob.choices.add(0, coob.getNULLCodePosition());
        
        List basicOptions = new LinkedList();
        basicOptions.add(ca);
        basicOptions.add(cstatus);
        basicOptions.add(cwm);
        basicOptions.add(csm);
        basicOptions.add(cmm);
        //      basicOptions.add(cc);
        basicOptions.add(coob);
        setCodeOptions(new CodeOptions(basicOptions));

        List warfightingOptions = new LinkedList();
        warfightingOptions.add(ca);
        warfightingOptions.add(cstatus);
        warfightingOptions.add(cwm);
        //      warfightingOptions.add(cc); // CodeCountry
        warfightingOptions.add(coob);
        // Kind of a hack, I know the number is the hierarchy number
        // of the particular scheme, and that the position.properties
        // file is setting up the options to reflect that for all of
        // these code option settings.
        ((CodeScheme) cs.getFromChoices(1)).setCodeOptions(new CodeOptions(warfightingOptions));
        //      ((CodeScheme)getFromChoices(1)).setCodeOptions(new
        // CodeOptions(warfightingOptions));

        List tacOptions = new LinkedList();
        tacOptions.add(ca);
        tacOptions.add(cstatus);
        tacOptions.add(csm);
        //      tacOptions.add(cc); // CodeCountry
        ((CodeScheme) cs.getFromChoices(2)).setCodeOptions(new CodeOptions(tacOptions));
        //      ((CodeScheme)getFromChoices(2)).setCodeOptions(new
        // CodeOptions(tacOptions));

        List intelOptions = new LinkedList();
        intelOptions.add(ca);
        intelOptions.add(cstatus);
        //      intelOptions.add(cc); // CodeCountry
        intelOptions.add(coob);
        ((CodeScheme) cs.getFromChoices(4)).setCodeOptions(new CodeOptions(intelOptions));
        //      ((CodeScheme)getFromChoices(4)).setCodeOptions(new
        // CodeOptions(intelOptions));

        List mootwOptions = new LinkedList();
        mootwOptions.add(ca);
        mootwOptions.add(cstatus);
        mootwOptions.add(cmm);
        //      mootwOptions.add(cc); // CodeCountry
        mootwOptions.add(coob);
        ((CodeScheme) cs.getFromChoices(5)).setCodeOptions(new CodeOptions(mootwOptions));
        //      ((CodeScheme)getFromChoices(5)).setCodeOptions(new
        // CodeOptions(mootwOptions));
    }

    public SymbolPart parseHierarchy(String name, Properties hierarchyProperties) {
        List positions = getPositionChoices();

        SymbolPartTree head = new SymbolPartTree(name);
        List subs = new LinkedList();
        head.setSubs(subs);

        for (Iterator it = positions.iterator(); it.hasNext();) {
            CodeScheme cs = (CodeScheme) it.next();

            if (Debug.debugging("symbolpart")) {
                Debug.output("CodePositionTree: loading " + cs.getPrettyName());
            }

            SymbolPart sp = cs.parseHierarchy(hierarchyProperties, head);
            if (sp != null) {
                subs.add(sp);
            }
        }

        return head;
    }
}