/*
 *  File: DrawingToolRenderException.java
 *  OptiMetrics, Inc.
 *  2107 Laurel Bush Road - Suite 209
 *  Bel Air, MD 21015
 *  (410)569 - 6081
 */
package com.bbn.openmap.dataAccess.shape;

import javax.swing.JOptionPane;

/**
 * Used to throw an exception when OMGraphics are not rendered as
 * OMGraphicConstants.RENDERTYPE_LATLON in EsriShapeExport and its
 * subclasses.
 */
public class DrawingToolRenderException extends Exception {

    /**
     * Displays a JOtionPane Message Dialog informing the user that
     * any Drawing Tool graphics not rendered in LAT/LON will not be
     * exported.
     */
    public static void notifyUserOfNonLatLonGraphics(int count) {
        String errMsg = "All Drawing Tool Graphics must be rendered as LAT/LON \nto be exported as ESRI shape files. \n\n"
                + count
                + " graphic"
                + (count > 1 ? "s" : "")
                + " not rendered in LAT/LON will not be exported.";
        String title = "Exporting Error";
        JOptionPane.showMessageDialog(null,
                errMsg,
                title,
                JOptionPane.ERROR_MESSAGE);
    }
}