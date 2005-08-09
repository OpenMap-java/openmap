/* ***********************************************************************
 * This is used by the MysqlGeometryLayer.
 * This program is distributed freely and in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * Merchantability or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Copyright 2003 by the Author
 *
 * Author name: Uwe Baier uwe.baier@gmx.net
 * Version 1.0
 * ***********************************************************************
 */
package com.bbn.openmap.layer.mysql;

import java.util.Vector;

import com.bbn.openmap.util.Debug;

/**
 * This class represents the Factory class do create the different
 * Geometry Objects. It takes as input a WKT Representation (text) of
 * the Geometry. It should have less performance than one which
 * manipulates WKB (binary) --> ToDo.
 * <p>
 * 
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class MysqlWKTGeometryFactory {

//    private Vector coordStringsVector = new Vector();

    public static MysqlGeometry createGeometry(String geotext) {
        WKTNode wktRoot = parseGeometryText(geotext);
        String type = geotext.substring(0, geotext.indexOf("("));
        return util(wktRoot, type);
    }

    protected static MysqlGeometry util(WKTNode wktRoot, String type) {

        if (type.equals(MysqlGeometry.POINTTYPE)) {
            return createPoint(wktRoot.getChildByNumber(0).getGeoWKT());
        } else if (type.equals(MysqlGeometry.LINESTRINGTYPE)) {
            return createLine(wktRoot.getChildByNumber(0).getGeoWKT());
        } else if (type.equals(MysqlGeometry.POLYGONTTYPE)) {
            return createPolygon(wktRoot);
        } else if (type.equals(MysqlGeometry.GEOMETRYCOLLECTIONTYPE)) {
            MysqlCollection g = new MysqlCollection();
            WKTNode n = wktRoot.getChildByNumber(0);
            String elementTypes = n.getGeoWKT();
            Vector typeVector = parseStringByToken(elementTypes, ',');
            if (typeVector.size() == n.countChildren()) {
                for (int i = 0; i < typeVector.size(); i++) {
                    WKTNode newRoot = new WKTNode();
                    newRoot.setRoot(true);
                    newRoot.adChild(n.getChildByNumber(i));
                    MysqlGeometry newGeometry = util(newRoot,
                            (String) typeVector.elementAt(i));
                    g.addElement(newGeometry);
                }
            } else {
                Debug.error("MysqlWKTGeometryFactory: Error in GeometryCollection");
            }
            return g;
        } else if (type.equals(MysqlGeometry.MULTIPOINTTYPE)) {
            MysqlMultiPoint g = new MysqlMultiPoint();
            WKTNode n = wktRoot.getChildByNumber(0);
            String pointString = n.getGeoWKT();
            Vector pointVector = parseStringByToken(pointString, ',');
            for (int i = 0; i < pointVector.size(); i++) {
                g.addElement(createPoint((String) pointVector.elementAt(i)));
            }

            return g;
        } else if (type.equals(MysqlGeometry.MULTILINESTRINGTYPE)) {
            MysqlMultiLineString g = new MysqlMultiLineString();

            WKTNode n = wktRoot.getChildByNumber(0);

            for (int i = 0; i < n.countChildren(); i++) {
                String lineString = n.getChildByNumber(i).getGeoWKT();
                g.addElement(createLine(lineString));
            }

            return g;
        } else if (type.equals(MysqlGeometry.MULTIPOLYGONTYPE)) {
            MysqlMultiPolygon g = new MysqlMultiPolygon();

            WKTNode n = wktRoot.getChildByNumber(0);

            for (int i = 0; i < n.countChildren(); i++) {
                WKTNode newRoot = new WKTNode();
                newRoot.setRoot(true);
                newRoot.adChild(n.getChildByNumber(i));
                g.addElement(createPolygon(newRoot));
            }

            return g;
        } else {
            Debug.output("MysqlWKTGeometryFactory: Type " + type
                    + " not implemented");
            return null;
        }
    }

    protected static MysqlPoint createPoint(String s) {
        MysqlPoint g = new MysqlPoint();
        Vector v = parseStringByToken(s, ' ');
        double[] d = createDoubleLatLongArray(v);
        g.setNorthings(d[0]);
        g.setEastings(d[1]);
        return g;
    }

    protected static MysqlLine createLine(String s) {
        MysqlLine g = new MysqlLine();
        Vector v = parseStringByToken(s.replaceAll(",", " "), ' ');
        double[] d = createDoubleLatLongArray(v);
        g.setCoordinateArray(d);
        return g;
    }

    protected static MysqlPolygon createPolygon(WKTNode wktRoot) {
        MysqlPolygon g = new MysqlPolygon();

        WKTNode n = wktRoot.getChildByNumber(0);

        Vector rings = new Vector();
        for (int i = 0; i < n.countChildren(); i++) {
            Vector v = parseStringByToken(n.getChildByNumber(i)
                    .getGeoWKT()
                    .replaceAll(",", " "), ' ');
            double[] d = createDoubleLatLongArray(v);
            rings.add(d);
        }
        g.setRings(rings);

        return g;
    }

    protected static void iterateWKTNodeTree(WKTNode n) {
        if (Debug.debugging("mysql")) {
            Debug.output("------------");
        }

        for (int i = 0; i < n.countChildren(); i++) {
            WKTNode child = n.getChildByNumber(i);

            if (Debug.debugging("mysql")) {
                Debug.output(child.getGeoWKT() + " " + child.isLeaf());
            }

            iterateWKTNodeTree(child);
        }
    }

    protected static WKTNode parseGeometryText(String coordString) {
        int i = coordString.indexOf("(");
        int j = coordString.lastIndexOf(")") + 1;
        coordString = coordString.substring(i, j);
        char[] ca = coordString.toCharArray();

        WKTNode rootnode = new WKTNode();
        rootnode.setRoot(true);

        WKTNode actualNode;

        actualNode = rootnode;
        char actualDelimiter = '#';
        for (int k = 0; k < ca.length; k++) {
            if (ca[k] == '(') {

                WKTNode n = new WKTNode();
                n.setParent(actualNode);
                actualNode.adChild(n);
                actualNode = n;
                actualDelimiter = '(';
            } else if (ca[k] == ')') {
                if (actualDelimiter == '(') {
                    actualNode.setLeaf(true);
                }
                actualNode = actualNode.getParent();
                actualDelimiter = ')';
            } else {
                char[] c = new char[1];
                c[0] = ca[k];
                actualNode.adToGeoWKT(c);
            }
        }

        return rootnode;
    }

    //     private static Vector iterateString(String s) {

    //      int low = 0;
    //      int up = 0;

    //      s = s.replaceAll(","," ");
    //      Vector coordVector = new Vector();

    //      while(up != -1)
    //      {
    //          up = s.indexOf(" ");

    //          if (up != -1 ) {
    //              Double d = new Double(s.substring(low,up));
    //              coordVector.addElement(d);
    //              s = s.substring(up+1,s.length());

    //          }
    //          else
    //          {

    //              Double d = new Double(s);
    //              coordVector.addElement(d);
    //          }

    //      }

    //      return coordVector;
    //     }

    //     private static double[] createDoubleLatLongArray(Vector v) {
    //      int i = v.size();
    //      double[] darray = new double[i];

    //      for (int j = 0; j < i / 2; j++) {

    //          for(int k = 0; k < 2; k++){
    //              if (j == 0) {
    //                  Double d = (Double) v.elementAt(1 - k);
    //                  darray[k] = d.doubleValue();
    //              } else {
    //                  Double d = (Double) v.elementAt(2 * j + 1 - k);
    //                  darray[2 * j + k] = d.doubleValue();

    //              }

    //          }

    //      }
    //      return darray;
    //     }

    protected static double[] createDoubleLatLongArray(Vector v) {
        int i = v.size();
        double[] darray = new double[i];

        for (int j = 0; j < i / 2; j++) {
            for (int k = 0; k < 2; k++) {
                if (j == 0) {
                    try {
                        Double d = new Double((String) v.elementAt(1 - k));
                        darray[k] = d.doubleValue();
                    } catch (Exception e) {
                        Debug.error((String) v.elementAt(1 - k));
                    }
                } else {
                    Double d = new Double((String) v.elementAt(2 * j + 1 - k));
                    darray[2 * j + k] = d.doubleValue();
                }
            }
        }
        return darray;
    }

    protected static Vector parseStringByToken(String s, char token) {

        char[] ca = s.toCharArray();
        char[] charArray;
        if (ca[ca.length - 1] != token) {
            char[] tk = new char[1];
            tk[0] = token;
            String s1 = s.concat(new String(tk));
            charArray = s1.toCharArray();
        } else {
            charArray = ca;
        }

        String t = new String();
        Vector v = new Vector();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == token) {
                v.add(t);
                t = "";
            } else {
                char[] c = new char[1];
                c[0] = charArray[i];
                t = t.concat(new String(c));
            }
        }
        return v;
    }

}