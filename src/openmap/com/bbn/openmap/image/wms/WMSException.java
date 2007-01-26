/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/WMSException.java,v 1.1 2007/01/26 15:04:23 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.io.IOException;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bbn.openmap.util.Debug;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Generates a WMS Exception in the form of XML with the specified code and
 * explanation message.
 * 
 * @version $Header:
 *          /cvs/CVS_LEBA/common/porttrack/src/java/com/obrctm/porttrack/modules/OGCMapRequestHandler.java,v
 *          1.10 2005/10/14 10:08:32 wachu Exp
 * @author pitek
 */
public class WMSException extends Exception {

    // these codes are defined in WMS specification version 1.3.0
    /**
     */
    public static final String INVALIDFORMAT = "InvalidFormat";

    // WMS 1.3.0
    // public static final String INVALIDCRS = "InvalidCRS";
    // WMS 1.1.1
    /**
     */
    public static final String INVALIDSRS = "InvalidSRS";

    /**
     */
    public static final String LAYERNOTDEFINED = "LayerNotDefined";

    /**
     */
    public static final String STYLENOTDEFINED = "StyleNotDefined";

    /**
     */
    public static final String LAYERNOTQUERYABLE = "LayerNotQueryable";

    /**
     */
    public static final String INVALIDPOINT = "InvalidPoint";

    /**
     */
    public static final String CURRENTUPDATESEQUENCE = "CurrentUpdateSequence";

    /**
     */
    public static final String INVALIDUPDATESEQUENCE = "InvalidUpdateSequence";

    /**
     */
    public static final String MISSINGDIMENSIONVALUE = "MissingDimensionValue";

    /**
     */
    public static final String INVALIDDIMENSIONVALUE = "InvalidDimensionValue";

    // only in WMS 1.3.0, but who cares...
    /**
     */
    public static final String OPERATIONNOTSUPPORTED = "OperationNotSupported";

    // this is an unsupported code, but it seems usefull enough to include.
    /**
     */
    public static final String INTERNALERROR = "InternalError";

    // *************ta linia u Pitka jest dodatkowa
    protected static String exceptionTemplate = "<?xml version='1.0' encoding=\"UTF-8\"?>"
            + "<ServiceExceptionReport version=\"1.1.0\">"
            + "</ServiceExceptionReport>";

    private String XML;

    private String Message;

    private String Code;

    /**
     * Creates a new WMSException object.
     */
    public WMSException() {
        this("All your base are belong to us.", null);
    }

    /**
     * Creates a new WMSException object.
     * 
     * @param message
     */
    public WMSException(String message) {
        this(message, null);
    }

    /**
     * Creates a new WMSException object.
     * 
     * @param message
     * @param code
     */
    public WMSException(String message, String code) {
        super();
        Message = message;
        Code = code;

        Document doc = new DocumentImpl();
        Element root = doc.createElement("ServiceExceptionReport");
        root.setAttribute("version", "1.1.0");
        Element ex = doc.createElement("ServiceException");
        ex.appendChild(doc.createTextNode(Message));
        if (Code != null)
            ex.setAttribute("code", Code);

        root.appendChild(ex);
        doc.appendChild(root);

        StringWriter strWriter = null;
        XMLSerializer serializer = null;
        OutputFormat outFormat = null;

        try {
            serializer = new XMLSerializer();
            strWriter = new StringWriter();
            outFormat = new OutputFormat();

            // Setup format settings
            outFormat.setEncoding("UTF-8");
            outFormat.setVersion("1.0");
            outFormat.setIndenting(true);
            outFormat.setIndent(2);

            // Define a Writer
            serializer.setOutputCharStream(strWriter);

            // Apply the format settings
            serializer.setOutputFormat(outFormat);

            // Serialize XML Document
            serializer.serialize(doc);
            this.XML = strWriter.toString();
            strWriter.close();
        } catch (IOException ioEx) {
            Debug.output("WMSException Internal Error !\n[");
            ioEx.printStackTrace();
            Debug.output("]");
            this.XML = INTERNALERROR;
        }

    }

    /**
     * @return
     */
    public String getXML() {
        return XML;
    }

    /**
     * @return
     */
    public String getMessage() {
        return Message;
    }

    /**
     * @return
     */
    public String getCode() {
        return Code;
    }
}
