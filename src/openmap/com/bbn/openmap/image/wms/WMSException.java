/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/WMSException.java,v 1.3 2009/01/15 19:38:33 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bbn.openmap.util.Debug;

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

    // this is an unsupported code, but it seems useful enough to include.
    /**
     */
    public static final String INTERNALERROR = "InternalError";

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

//    /**
//     * Creates a new WMSException object.
//     * 
//     * @param message
//     * @param code
//     */
//    public WMSException(String message, String code) {
//        super();
//        Message = message;
//        Code = code;
//
//        Document doc = new DocumentImpl();
//        Element root = doc.createElement("ServiceExceptionReport");
//        root.setAttribute("version", "1.1.0");
//        Element ex = doc.createElement("ServiceException");
//        ex.appendChild(doc.createTextNode(Message));
//        if (Code != null)
//            ex.setAttribute("code", Code);
//
//        root.appendChild(ex);
//        doc.appendChild(root);
//
//        StringWriter strWriter = null;
//        XMLSerializer serializer = null;
//        OutputFormat outFormat = null;
//
//        try {
//            serializer = new XMLSerializer();
//            strWriter = new StringWriter();
//            outFormat = new OutputFormat();
//
//            // Setup format settings
//            outFormat.setEncoding("UTF-8");
//            outFormat.setVersion("1.0");
//            outFormat.setIndenting(true);
//            outFormat.setIndent(2);
//
//            // Define a Writer
//            serializer.setOutputCharStream(strWriter);
//
//            // Apply the format settings
//            serializer.setOutputFormat(outFormat);
//
//            // Serialize XML Document
//            serializer.serialize(doc);
//            this.XML = strWriter.toString();
//            strWriter.close();
//        } catch (IOException ioEx) {
//            Debug.output("WMSException Internal Error !\n[");
//            ioEx.printStackTrace();
//            Debug.output("]");
//            this.XML = INTERNALERROR;
//        }
//
//    }
    
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

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

//            Document doc = new DocumentImpl();
        Element root = doc.createElement("ServiceExceptionReport");
        root.setAttribute("version", "1.1.0");
        Element ex = doc.createElement("ServiceException");
        ex.appendChild(doc.createTextNode(Message));
        if (Code != null)
            ex.setAttribute("code", Code);

        root.appendChild(ex);
        doc.appendChild(root);

        StringWriter strWriter = new StringWriter();
//        XMLSerializer serializer = null;
//        OutputFormat outFormat = null;
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD,"xml");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        tr.setOutputProperty(OutputKeys.VERSION,"1.0");
        tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
        
//      Serialize XML Document
        tr.transform( new DOMSource(doc),new StreamResult(strWriter));    

//        try {
//            serializer = new XMLSerializer();
//            strWriter = new StringWriter();
//            outFormat = new OutputFormat();
//
//            // Setup format settings
//            outFormat.setEncoding("UTF-8");
//            outFormat.setVersion("1.0");
//            outFormat.setIndenting(true);
//            outFormat.setIndent(2);
//
//            // Define a Writer
//            serializer.setOutputCharStream(strWriter);
//
//            // Apply the format settings
//            serializer.setOutputFormat(outFormat);
//
//            // Serialize XML Document
//            serializer.serialize(doc);
            this.XML = strWriter.toString();
            strWriter.close();
        } catch (Exception ex) {
            Debug.output("WMSException Internal Error !\n[");
            ex.printStackTrace();
            Debug.output("]");
            this.XML = INTERNALERROR;
        }

    }

    /**
     * @return xml for exception
     */
    public String getXML() {
        return XML;
    }

    /**
     * @return string error message
     */
    public String getMessage() {
        return Message;
    }

    /**
     * @return string for error code
     */
    public String getCode() {
        return Code;
    }
}
