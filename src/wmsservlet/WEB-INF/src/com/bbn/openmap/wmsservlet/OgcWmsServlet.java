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
// $Source: /cvs/distapps/openmap/src/wmsservlet/WEB-INF/src/com/bbn/openmap/wmsservlet/OgcWmsServlet.java,v $
// $Revision: 1.3 $ $Date: 2007/01/26 15:04:52 $ $Author: dietrick $
// **********************************************************************
package com.bbn.openmap.wmsservlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.image.wms.WMSException;
import com.bbn.openmap.image.wms.WmsRequestHandler;

/**
 * 
 */
public class OgcWmsServlet extends HttpServlet {

    /**
     * A do-nothing constructor - init does all the work.
     */
    public OgcWmsServlet() {
        super();
    }

    /**
     * 
     */
    protected Properties parsePropertiesFromRequest(HttpServletRequest request) {
        Properties props = new Properties();
        java.util.Enumeration keys = request.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = request.getParameter(key);
            if (value != null) {
                // A wms client can send lowercase request parameters. like
                // Metacarta TileCache
                key = key.toUpperCase();
                props.put(key, value);
            }
        }
        return props;
    }

    protected WmsRequestHandler createRequestHandler(HttpServletRequest request) throws ServletException,
            IOException {
        Debug.message("wms", "OgcWmsServlet.createRequestHandler : ");

        // configure this by subclassing the servlet and override this method
        String mapDefinition = "WEB-INF/openmap.properties";

        int serverPort = request.getServerPort();

        String contextPath = request.getContextPath();
        if (contextPath == null) {
            throw new ServletException("context path is not specified");
        }

        String servletPath = request.getServletPath();
        if (servletPath == null) {
            throw new ServletException("servlet path is not specified");
        }

        Debug.message("wms", "Using map definition:" + mapDefinition);
        try {
            PropertyHandler propHandler = new PropertyHandler(mapDefinition);
            Properties props = propHandler.getProperties();
            WmsRequestHandler wmsRequestHandler = new WmsRequestHandler(serverPort, contextPath
                    + servletPath, props);
            return wmsRequestHandler;
        } catch (java.net.MalformedURLException me) {
            Debug.message("wms", "MS: caught MalformedURLException - \n" + me.getMessage());
            throw me;
        } catch (java.io.IOException ioe) {
            Debug.message("wms", "MS: caught IOException - \n" + ioe.getMessage());
            throw ioe;
        } catch (WMSException wmse) {
            Debug.message("wms", "MS: caught WMSException - \n" + wmse.getMessage());
            throw new ServletException(wmse);
        }

    }

    /**
     * 
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Debug.message("wms", "OgcWmsServlet.doGet");

        WmsRequestHandler wmsRequestHandler = createRequestHandler(request);

        Properties properties = parsePropertiesFromRequest(request);
        HttpResponse httpResponse = new HttpResponse(response);
        wmsRequestHandler.handleRequest(properties, httpResponse);
    }

}
