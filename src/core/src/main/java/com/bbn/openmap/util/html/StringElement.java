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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/StringElement.java,v
// $
// $RCSfile: StringElement.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

public class StringElement implements Element {

    /** the string value that we are */
    protected String s;

    /**
     * Construct a new StringElement with a string literal
     * 
     * @param s the string to use
     */
    public StringElement(String s) {
        this.s = s;
    }

    /**
     * convert representation to html and write it out
     * 
     * @param out the output Writer
     * @exception java.io.IOException an IO error occurred accessing
     *            out
     */
    public void generate(java.io.Writer out) throws java.io.IOException {
        out.write(s);
    }

}