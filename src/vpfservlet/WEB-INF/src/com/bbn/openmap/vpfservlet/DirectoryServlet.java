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
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/DirectoryServlet.java,v $
// $Revision: 1.2 $ $Date: 2004/01/26 18:18:16 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.bbn.openmap.layer.util.html.*;
import com.bbn.openmap.layer.vpf.*;
import com.bbn.openmap.io.FormatException;

/**
 * This servlet lists the files in a directory of a configured VPF database.
 * Directory listing can be disabled, see the listDirectories servlet
 * parameter in the deployment descriptor. (web.xml)
 */
public class DirectoryServlet extends VPFHttpServlet {

    /**
     * A do-nothing constructor - init does all the work.
     */
    public DirectoryServlet() {
        super();
    }

    /**
     * false if this servlet should generate a "disabled by administrator"
     * method rather than a directory list.
     */
    private boolean listFiles;

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) 
        throws ServletException, IOException {


        String filePath = (String)request.getAttribute(DispatchServlet.ROOTPATH_FILENAME);
        if (filePath == null) {
            String path = setPathInfo(request);
            filePath = contextInfo.resolvePath(path);
            if (!pathOkay(filePath, path, response)) {
                return;
            }
        }
        File fp = new File(filePath);
        String pathInfo = getPathInfo(request);
        
        PrintWriter out = response.getWriter();

        String filename = fp.getName().toLowerCase();
        String end = "</BODY></HTML>";

        response.setContentType("text/html");
        out.println(HTML_DOCTYPE + "<HTML>\n<HEAD><TITLE>" + filename +
                    "</TITLE></HEAD>\r\n<BODY>\r\n<H1>Directory " +
                    filename+"</H1>\r\n");
        out.println(getStylesheetHTML(request));

        if (!listFiles) {
            out.println("Directory listing disabled by administrator.");
        } else if (!fp.isDirectory()) {
            out.println("Requested path is not a directory.");
        } else {
            out.println("");
            File files[] = fp.listFiles();
            ArrayList filenames = new ArrayList();
            ArrayList directories = new ArrayList();
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                if (files[i].isDirectory()) {
                    directories.add(name);
                } else {
                    filenames.add(name);
                }
            }
            Collections.sort(directories);
            Collections.sort(filenames);
            
            if (!pathInfo.endsWith("/")) {
                pathInfo += '/';
            }
            
            HtmlListElement filelist = new HtmlListElement("Sub-Directories");
            
            for (Iterator dir = directories.iterator(); dir.hasNext(); ) {
                String url = fileURL(request, response,
                                     pathInfo, (String)dir.next());
                filelist.addElement(url);
            }
            filelist.generate(out);
            
            filelist = new HtmlListElement("Files");
            for (Iterator file = filenames.iterator(); file.hasNext(); ) {
                String url = fileURL(request, response,
                                     pathInfo, (String)file.next());
                filelist.addElement(url);
            }
            filelist.generate(out);
        }
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        listFiles = new Boolean(config.getInitParameter("listDirectories")).booleanValue();
    }
}
