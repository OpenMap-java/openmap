package com.bbn.openmap.image.wms;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * WMS version as found in the request parameter named VERSION.
 */
abstract class Version {

    private static final Version V111 = new V111("1.1.1");

    private static final Version V130 = new V130("1.3.0");

    private static final Map<String, Version> byVersionString;

    static {
        Map<String, Version> m = new HashMap<String, Version>();
        m.put(V111.getVersionString(), V111);
        m.put(V130.getVersionString(), V130);
        byVersionString = Collections.unmodifiableMap(m);
    }

    private String version;

    private Version(String version) {
        this.version = version;
    }

    public static Version getVersion(String versionString) {
        return byVersionString.get(versionString);
    }
    
   public static Version getVersionBestMatch(String versionString) {
      Version version = getVersion(versionString);
      if (version != null) {
         return version;
      }
      if (versionString == null) {
         return getDefault();
      }
      if (versionString.startsWith("1.1")) {
         return V111;
      }
      if (versionString.startsWith("1.3")) {
         return V130;
      }
      return getDefault();
   }
    
    public static Version getDefault() {
        return V111;
    }

    public String getVersionString() {
        return version;
    }

    public String toString() {
        return getVersionString();
    }

    public abstract Document createCapabilitiesDocumentStart();

    public abstract Element createLatLonBoundingBox(Document doc);

    public abstract String getCoordinateReferenceSystemAcronym();

    public abstract boolean usesAxisOrder();

    public abstract String getServiceName();
    
    public abstract Collection<String> getCapabiltiesFormats();
    
    public abstract Collection<String> getExceptionFormats();

    private static class V111 extends Version {

        public V111(String version) {
            super(version);
        }

        public Document createCapabilitiesDocumentStart() {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();

                DOMImplementation impl = builder.getDOMImplementation();
                DocumentType doctype = impl
                        .createDocumentType("wms", "WMT_MS_Capabilities",
                                "http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd");
                Document doc = impl.createDocument(null, "WMT_MS_Capabilities",
                        doctype);

                return doc;
            } catch (javax.xml.parsers.ParserConfigurationException ex) {
                throw new RuntimeException("Cannot create new Xml Document:"
                        + ex.getMessage());
            }
        }

        public Element createLatLonBoundingBox(Document doc) {
            Element e1 = doc.createElement("LatLonBoundingBox");
            e1.setAttribute("minx", "-180");
            e1.setAttribute("miny", "-90");
            e1.setAttribute("maxx", "180");
            e1.setAttribute("maxy", "90");
            return e1;
        }

        @Override
        public String getCoordinateReferenceSystemAcronym() {
            return "SRS";
        }

        @Override
        public boolean usesAxisOrder() {
            return false;
        }

        @Override
        public String getServiceName() {
            return "OGC:WMS";
        }

      @Override
      public Collection<String> getExceptionFormats() {
         return Arrays.asList("application/vnd.ogc.se_xml");
      }

      @Override
      public Collection<String> getCapabiltiesFormats() {
         return Arrays.asList("application/vnd.ogc.wms_xml");
      }

    }

    private static class V130 extends Version {

        public V130(String version) {
            super(version);
        }

        public Document createCapabilitiesDocumentStart() {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();

                DOMImplementation impl = builder.getDOMImplementation();
                // TODO: add xsd stuff for WMS_Capabilities. how?
                // <WMS_Capabilities version="1.3.0"
                // xmlns="http://www.opengis.net/wms"
                // xmlns:xlink="http://www.w3.org/1999/xlink"
                // xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                // xsi:schemaLocation="http://www.opengis.net/wms
                // http://schemas.opengis.net/wms/1.3.0/capabilities_1_2_0.xsd">
                Document doc = impl.createDocument(
                        "http://www.opengis.net/wms", "WMS_Capabilities", null);

                return doc;
            } catch (javax.xml.parsers.ParserConfigurationException ex) {
                throw new RuntimeException("Cannot create new Xml Document:"
                        + ex.getMessage());
            }
        }

        public Element createLatLonBoundingBox(Document doc) {
            Element bb = doc.createElement("EX_GeographicBoundingBox");

            Element w = doc.createElement("westBoundLongitude");
            w.setTextContent("-180");
            bb.appendChild(w);
            
            Element e = doc.createElement("eastBoundLongitude");
            e.setTextContent("180");
            bb.appendChild(e);
            
            Element s = doc.createElement("southBoundLatitude");
            s.setTextContent("-90");
            bb.appendChild(s);
            
            Element n = doc.createElement("northBoundLatitude");
            n.setTextContent("90");
            bb.appendChild(n);
            
            return bb;
        }

        @Override
        public String getCoordinateReferenceSystemAcronym() {
            return "CRS";
        }

        @Override
        public boolean usesAxisOrder() {
            return true;
        }

        @Override
        public String getServiceName() {
            return "WMS";
        }

      @Override
      public Collection<String> getExceptionFormats() {
         return Arrays.asList("XML");
      }

      @Override
      public Collection<String> getCapabiltiesFormats() {
         return Arrays.asList("text/xml");
      }

    }

}
