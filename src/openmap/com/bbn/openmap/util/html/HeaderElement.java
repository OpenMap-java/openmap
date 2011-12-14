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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/HeaderElement.java,v $
// $RCSfile: HeaderElement.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

/** This class provides easy access to the html header tags */
public class HeaderElement extends WrapElement {

    /**
     * Construct a header
     * 
     * @param headLevel the level of the header (should be a small
     *        integer)
     * @param e the element in the header
     * @exception IllegalArgumentException headLevel was invalid
     */
    public HeaderElement(int headLevel, Element e) {
        super("h" + headLevel, e);
        if (headLevel < 1)
            throw new IllegalArgumentException("HeaderElement: headLevel = "
                    + headLevel);
    }

    /**
     * Construct a header
     * 
     * @param headLevel the level of the header (should be a small
     *        integer)
     * @param s the string in the header
     * @exception IllegalArgumentException headLevel was invalid
     */
    public HeaderElement(int headLevel, String s) {
        this(headLevel, new StringElement(s));
    }
}