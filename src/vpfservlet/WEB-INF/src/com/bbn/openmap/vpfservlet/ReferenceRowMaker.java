// **********************************************************************
// <copyright>
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/ReferenceRowMaker.java,v $
// $Revision: 1.3 $ $Date: 2004/10/14 18:06:33 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.io.File;
import java.util.List;
import javax.servlet.http.*;

import com.bbn.openmap.layer.util.html.TableRowElement;
import com.bbn.openmap.layer.vpf.*;

/**
 * A RowMaker class that retains references to the
 * HttpServletRequest and HttpServletResponse instances of the
 * request.
 */
public abstract class ReferenceRowMaker extends PlainRowMaker {
    /** the servlet request object */
    final protected HttpServletRequest request;
    /** the servlet response object */
    final protected HttpServletResponse response;

    public ReferenceRowMaker(HttpServletRequest request,
                             HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public String toURL(String servletName, String pathname, String filename) {
        return VPFHttpServlet.toURL(request, response, servletName, 
                                    pathname, filename);
    }
    public String fileURL(String pathname, String filename) {
        return VPFHttpServlet.fileURL(request, response, pathname, filename);
    }
}

