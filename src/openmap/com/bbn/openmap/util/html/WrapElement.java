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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/WrapElement.java,v $
// $RCSfile: WrapElement.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

/**
 * This class is used for html tags that are begin/end paired. For
 * example, the html
 * <code>&lt;code&gt;wrapped element&lt;/code&gt;</code> would have
 * a wrapString of code, and an element of "wrapped element"
 */
public class WrapElement implements Element {

    /** the html tag to "wrap" around the contained element */
    protected String wrapString;
    /** params for the tag open */
    protected String paramString;
    /** the element that gets wrapped */
    protected Element e;

    /**
     * Construct a WrapElement with just a wrapping string
     * 
     * @param wrapString the html tag that gets wrapped around the
     *        element
     */
    public WrapElement(String wrapString) {
        this(wrapString, null, null);
    }

    /**
     * Construct a WrapElement with a wrapping string and element
     * 
     * @param wrapString the html tag that gets wrapped around the
     *        element
     * @param e the element that gets contained
     */
    public WrapElement(String wrapString, Element e) {
        this(wrapString, null, e);
    }

    /**
     * Construct a WrapElement with a wrapping string and params
     * 
     * @param wrapString the html tag that gets wrapped around the
     *        element
     * @param paramString the string that gets contained
     */
    public WrapElement(String wrapString, String paramString) {
        this(wrapString, paramString, null);
    }

    /**
     * Construct a WrapElement with a wrapping string, params and
     * element
     * 
     * @param wrapString the html tag that gets wrapped around the
     *        element
     * @param e the element that gets contained
     */
    public WrapElement(String wrapString, String paramString, Element e) {
        this.wrapString = wrapString;
        this.paramString = paramString;
        this.e = e;
    }

    /**
     * Writer for the Element attribute
     * 
     * @param e the new element value
     */
    public void setElement(Element e) {
        this.e = e;
    }

    /**
     * Accessor for the element attribute
     * 
     * @return the contained element
     */
    public Element getElement() {
        return e;
    }

    /**
     * convert representation to html and write it out
     * 
     * @param out the output Writer
     * @exception java.io.IOException an IO error occurred accessing
     *            out
     */
    public void generate(java.io.Writer out) throws java.io.IOException {
        out.write("<" + wrapString);
        if (paramString != null) {
            out.write(" " + paramString);
        }
        out.write(">");
        e.generate(out);
        out.write("</" + wrapString + ">\r\n");
    }
}