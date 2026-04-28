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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/MapBeanPrinter.java,v $
// $RCSfile: MapBeanPrinter.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.RepaintManager;

import com.bbn.openmap.MapBean;

/**
 * 
 * A simple utility class to print a MapBean and rescale it to fit the
 * printed page.
 *  
 */
public class MapBeanPrinter implements Printable {
    private MapBean MapBeanToBePrinted;
    private Dimension MapSize;

    public static void printMap(MapBean mapBean) {
        new MapBeanPrinter(mapBean).print();
    }

    public MapBeanPrinter(MapBean mapBean) {
        MapSize = mapBean.getSize();
        MapBeanToBePrinted = mapBean;
    }

    public void print() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        if (printJob.printDialog())
            try {
                printJob.print();
            } catch (PrinterException pe) {
                System.out.println("Error printing: " + pe);
            }
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return (NO_SUCH_PAGE);
        } else {

            // Compute size of component to print
            double frameHeight = MapSize.height;
            double frameWidth = MapSize.width;

            // Compute size of paper
            double pageHeight = pageFormat.getImageableHeight();
            double pageWidth = pageFormat.getImageableWidth();

            // Compute x and y scales
            double xScale = pageWidth / frameWidth;
            double yScale = pageHeight / frameHeight;

            // Retain smallest scale
            double scale = xScale;
            if (yScale < xScale)
                scale = yScale;

            /*
             * Scale and position the graphic Remark : I had to remove
             * 1 from getImageable() values in order to remove an ugly
             * border that appears on the left and top of the printed
             * map. bug in the JDK?
             */
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pageFormat.getImageableX() - 1.0,
                    pageFormat.getImageableY() - 1.0);
            g2d.scale(scale, scale);

            // Do the work now ...
            disableDoubleBuffering(MapBeanToBePrinted);
            MapBeanToBePrinted.paint(g2d);
            enableDoubleBuffering(MapBeanToBePrinted);

            return (PAGE_EXISTS);
        }
    }

    /**
     * The speed and quality of printing suffers dramatically if any
     * of the containers have double buffering turned on. So this
     * turns if off globally.
     */
    public static void disableDoubleBuffering(Component c) {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(false);
    }

    /** Re-enables double buffering globally. */
    public static void enableDoubleBuffering(Component c) {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(true);
    }
}