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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/html/Document.java,v $
// $RCSfile: Document.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.html;

import java.io.Writer;

/** This class wraps an entire html document (page) */
public class Document implements ContainerElement {
    /** this title of the document */
    protected String title;
    /** the base URL for this document */
    protected String base;
    /** the body of the document */
    protected ListElement body;

    /** Construct a document with no title and an empty body */
    public Document() {
        body = new ListElement();
    }

    /**
     * Construct a document with a title but an empty body
     * 
     * @param title the title of the document
     */
    public Document(String title) {
        body = new ListElement();
        this.title = title;
    }

    /**
     * Writer for title
     * 
     * @param title the new document title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Writer for the base url
     * 
     * @param base the new base URL
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * Add another element to the body of the document
     * 
     * @param e the element to add
     */
    public void addElement(Element e) {
        body.addElement(e);
    }

    /**
     * Add another string to the body of the document
     * 
     * @param s the string to add
     */
    public void addElement(String s) {
        addElement(new StringElement(s));
    }

    /**
     * Write the header to the output
     * 
     * @param out the Writer to dump output to
     * @exception java.io.IOException an IO error occurred accessing
     *            out
     */
    public void generateHeader(Writer out) throws java.io.IOException {
        out.write("<HEAD>");
        if (title != null) {
            out.write("<TITLE>");
            out.write(title);
            out.write("</TITLE>");
        }
        if (base != null) {
            out.write("<BASE href=\"" + "\">");
        }
        out.write("</HEAD>");
    }

    /**
     * convert representation to html and write it out
     * 
     * @param out the output Writer
     * @exception java.io.IOException an IO error occurred accessing
     *            out
     */
    public void generate(Writer out) throws java.io.IOException {
        out.write("<HTML>");
        generateHeader(out);
        out.write("<BODY>");
        body.generate(out);
        out.write("</BODY>");
        out.write("</HTML>");
    }
}