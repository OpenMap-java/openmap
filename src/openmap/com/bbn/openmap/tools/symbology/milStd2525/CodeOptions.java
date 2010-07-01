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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeOptions.java,v $
// $RCSfile: CodeOptions.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Iterator;
import java.util.List;

/**
 * CodeOptions represent a set of CodePositions that can be chosen for
 * a particular SymbolPart. This class is a holder for affiliations,
 * order of battle, modifier settings, etc.
 */
public class CodeOptions {
    protected List options;

    public CodeOptions(List opts) {
        options = opts;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("CodeOptions:\n");
        if (options != null) {
            for (Iterator it = options.iterator(); it.hasNext();) {
                sb.append(((CodePosition) it.next()).toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public List getOptions() {
        return options;
    }

}