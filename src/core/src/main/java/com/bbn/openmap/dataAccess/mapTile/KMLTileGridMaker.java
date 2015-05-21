/*
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */

package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.util.FileUtils;

/**
 * Creates a KML grid file for import into Google Earth. This file shows the
 * tile boundaries for a specified tile zoom level.
 * 
 * @author ddietrick
 */
public class KMLTileGridMaker {
   protected String docTitle;
   protected int level;
   protected List<Tile> tiles;
   protected DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();

   /**
    * @param builder
    */
   protected KMLTileGridMaker(Builder builder) {
      this.level = builder.zoomLevel;
      tiles = makeTiles(level);
      this.docTitle = builder.docTitle;

      drawingAttributes.setLinePaint(builder.lineColor);
      drawingAttributes.setFillPaint(builder.fillColor);
      drawingAttributes.setSelectPaint(builder.labelColor);
   }

   protected List<Tile> makeTiles(int level) {
      List<Tile> ret = new ArrayList<Tile>();
      ZoomLevelInfo zli = new ZoomLevelInfo();
      zli.setZoomLevel(level);
      int edgeTileCount = zli.getEdgeTileCount();
      OSMMapTileCoordinateTransform transform = new OSMMapTileCoordinateTransform();
      zli.setScale(transform.getScaleForZoom(level));
      for (int x = 0; x < edgeTileCount; x++) {
         for (int y = 0; y < edgeTileCount; y++) {
            Point2D ulllp = transform.tileUVToLatLon(new Point2D.Double(x, y), level);
            Point2D lrllp = transform.tileUVToLatLon(new Point2D.Double(x + 1, y + 1), level);

            ret.add(new Tile(x, y, ulllp, lrllp));
         }
      }
      return ret;
   }

   protected void writeDoc(OutputStream output)
         throws ParserConfigurationException, TransformerException {

      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder;
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.newDocument();

      Element kmlElement = appendChild(document, document, createKMLElement(document));
      Element docElement = appendChild(document, kmlElement, "Document", null);
      appendChild(document, docElement, createStyle(document));
      appendChild(document, docElement, createTiles(document));

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DOMSource source = new DOMSource(document);

      StreamResult result = new StreamResult(output);
      transformer.transform(source, result);
   }

   protected Element createKMLElement(Document doc) {
      Element rootElement = doc.createElement("kml");
      rootElement.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");
      rootElement.setAttribute("xmlns:gx", "http://www.google.com/kml/ext/2.2");
      rootElement.setAttribute("xmlns:kml", "http://www.opengis.net/kml/2.2");
      rootElement.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom");
      return rootElement;
   }

   protected Element createTiles(Document doc) {
      Element folderElement = doc.createElement("Folder");
      appendChild(doc, folderElement, "name", "Tile Layout for Zoom Level " + level);
      appendChild(doc, folderElement, "open", "1");

      List<Tile> tiles = makeTiles(level);

      for (Tile tile : tiles) {
         Point2D ul = tile.ul;
         Point2D lr = tile.lr;

         StringBuilder buf = new StringBuilder();

         buf.append(ul.getX() + "," + ul.getY() + ",0 ");
         buf.append(lr.getX() + "," + ul.getY() + ",0 ");
         buf.append(lr.getX() + "," + lr.getY() + ",0 ");
         buf.append(ul.getX() + "," + lr.getY() + ",0 ");
         buf.append(ul.getX() + "," + ul.getY() + ",0 ");

         // Polygon representing tile
         Element placemark = appendChild(doc, folderElement, "Placemark", null);

         appendChild(doc, placemark, "name", tile.title);
         appendChild(doc, placemark, "styleUrl", "#" + styleString);

         Element polygon = appendChild(doc, placemark, "Polygon", null);
         appendChild(doc, polygon, "tessellate", "1");

         Element obiElement = appendChild(doc, polygon, "outerBoundaryIs", null);
         Element lrElement = appendChild(doc, obiElement, "LinearRing", null);
         Element coordElement = appendChild(doc, lrElement, "coordinates", buf.toString());

         // Label for tile
         Element placemark2 = appendChild(doc, folderElement, "Placemark", null);
         appendChild(doc, placemark2, "name", tile.title);
         appendChild(doc, placemark2, "styleUrl", "#" + styleString);
         Element point = appendChild(doc, placemark2, "Point", null);
         appendChild(doc, point, "coordinates", ((lr.getX() + ul.getX()) / 2) + "," + ((ul.getY() + lr.getY()) / 2) + ",0");
      }

      return folderElement;
   }

   String styleString = "tileborderstyle";

   protected Element createStyle(Document doc) {
      Element style = doc.createElement("Style");
      style.setAttribute("id", styleString);
      Element label = appendChild(doc, style, "LabelStyle", null);
      // Element icon = appendChild(doc, style, "IconStyle", null);
      Element line = appendChild(doc, style, "LineStyle", null);
      Element poly = appendChild(doc, style, "PolyStyle", null);

      String labelColorString = Integer.toHexString(((Color) drawingAttributes.getSelectPaint()).getRGB());
      String lineColorString = Integer.toHexString(((Color) drawingAttributes.getLinePaint()).getRGB());
      String fillColorString = Integer.toHexString(((Color) drawingAttributes.getFillPaint()).getRGB());

      appendChild(doc, label, "color", labelColorString);
      appendChild(doc, line, "color", lineColorString);
      appendChild(doc, poly, "color", fillColorString);

      return style;
   }

   protected Element appendChild(Document doc, Node parent, String childField, String value) {
      Element element = (Element) parent.appendChild(doc.createElement(childField));
      if (value != null) {
         element.setTextContent(value);
      }
      return element;
   }

   protected Element appendChild(Document doc, Node parent, Element child) {
      parent.appendChild(child);
      return child;
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      Color fillColor = new Color(0x3300FF00, true);
      Color lineColor = new Color(0xFF00FF00);

      com.bbn.openmap.util.ArgParser ap = new com.bbn.openmap.util.ArgParser("KMLTileGridMaker");

      ap.add("level", "Tile Zoom Level to create grid for.", 1);

      if (!ap.parse(args)) {
         ap.printUsage();
         System.exit(0);
      }

      String[] arg = ap.getArgValues("level");
      if (arg != null) {

         try {
            int level = Integer.parseInt(arg[0]);
            new Builder(level).setLineColor(lineColor).setFillColor(fillColor).setLabelColor(lineColor).go();
         } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
         }
      }
   }

   protected class Tile {
      protected int uvx;
      protected int uvy;
      protected Point2D ul;
      protected Point2D lr;
      protected String title;

      protected Tile(int uvx, int uvy, Point2D ul, Point2D lr) {
         this.uvx = uvx;
         this.uvy = uvy;
         this.ul = ul;
         this.lr = lr;

         title = uvx + "_" + uvy;
      }
   }

   /**
    * Use this class to run the KMLTileGridMake. Create a builder, add any
    * customization you want, call go() when you want to create the file.
    * 
    * @author ddietrick
    */
   public static class Builder {
      int zoomLevel;
      Color lineColor = Color.WHITE;
      Color fillColor = Color.WHITE;
      Color labelColor = Color.WHITE;
      String docTitle;
      String fileName;
      OutputStream output;
      boolean closeOutput = false;

      public Builder() {
         this(5);
      }

      public Builder(int tileZoomLevel) {
         zoomLevel = tileZoomLevel;
         docTitle = "Tile Boundaries for Zoom Level " + zoomLevel;
      }

      public Builder setLineColor(Color lineColor) {
         this.lineColor = lineColor;
         return this;
      }

      public Builder setFillColor(Color fillColor) {
         this.fillColor = fillColor;
         return this;
      }

      public Builder setLabelColor(Color labelColor) {
         this.labelColor = labelColor;
         return this;
      }

      public Builder setDocTitle(String docTitle) {
         this.docTitle = docTitle;
         return this;
      }

      public Builder setFileName(String fileName)
            throws FileNotFoundException {
         this.fileName = fileName;

         File file = null;
         if (fileName != null) {
            file = new File(fileName);
         }

         if (file == null) {
            String filePath = FileUtils.getFilePathToSaveFromUser("Choose location and name of KML file");
            if (filePath != null) {
               file = new File(filePath);
            }
         }

         if (file != null) {
            output = new FileOutputStream(file);
            closeOutput = true;
         }

         return this;
      }

      /**
       * If you provide an OutputStream, you're responsible for closing it.
       * 
       * @param out
       * @return this Builder
       */
      public Builder setOutputStream(OutputStream out) {
         this.output = out;
         return this;
      }

      public void go()
            throws ParserConfigurationException, TransformerException, IOException {
         KMLTileGridMaker kml = new KMLTileGridMaker(Builder.this);

         if (output == null) {
            setFileName(null);
         }

         if (output != null) {
            kml.writeDoc(output);
            if (closeOutput) {
               output.close();
            }
         } else {
            throw new IOException("No output specified");
         }
      }
   }
}
