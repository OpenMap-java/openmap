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
// $Source: /cvs/distapps/openmap/src/svg/com/bbn/openmap/tools/svg/SVG2GIF.java,v $
// $RCSfile: SVG2GIF.java,v $
// $Revision: 1.3 $
// $Date: 2005/08/09 21:07:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.batik.transcoder.TranscoderException;

import com.bbn.openmap.image.AcmeGifFormatter;
import com.bbn.openmap.image.ImageFormatter;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.wanderer.Wanderer;
import com.bbn.openmap.util.wanderer.WandererCallback;

/**
 * Wanders through a directory structure converting svg files to gifs.
 */
public class SVG2GIF implements WandererCallback {

    boolean DETAIL = false;

    protected ImageFormatter formatter;

    protected Dimension imageDimension;

    public SVG2GIF() {
        formatter = new AcmeGifFormatter();
    }

    // do nothing on directories
    public boolean handleDirectory(File directory) {
       return true;
    }

    // count the ; and } in each file.
    public boolean handleFile(File file) {
        if (!file.getName().endsWith(".svg")) {
            return true;
        }

        try {
            SVGRasterizer svgr;
            svgr = new SVGRasterizer(file.toURI().toURL());
            svgr.setBackgroundColor(new Color(128, 128, 128, 0));
            BufferedImage bi = svgr.createBufferedImage(imageDimension);
            byte[] imageBytes = formatter.formatImage(bi);

            String newFileName = file.toString().replaceAll(".svg", ".gif");
            if (Debug.debugging("svg")) {
                Debug.output("writing " + newFileName + " from " + file);
            }
            FileOutputStream fos = new FileOutputStream(newFileName);
            fos.write(imageBytes);
            fos.flush();
            fos.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (TranscoderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Given a set of files or directories, parade through them to
     * change their case.
     * 
     * @param argv paths to files or directories, use -h to get a
     *        usage statement.
     */
    public static void main(String[] argv) {
        Debug.init();

        ArgParser ap = new ArgParser("SVG2GIF");
        ap.add("dimension",
                "Dimension of output image, add height and width arguments separated by a space",
                2);

        if (argv.length == 0) {
            ap.bail("Usage: java com.bbn.openmap.util.wanderer.SVG <dir> (-help for options)",
                    false);
        }

        ap.parse(argv);

        Dimension dim = null;
        String[] arg = ap.getArgValues("dimension");
        if (arg != null) {

            String heightString = arg[0];
            String widthString = arg[1];

            Debug.output("Creating images with height (" + heightString
                    + ") width (" + widthString + ")");
            try {
                int width = Integer.parseInt(widthString);
                int height = Integer.parseInt(heightString);
                dim = new Dimension(width, height);
            } catch (NumberFormatException nfe) {
                String message = "Problem reading dimensions: " + nfe.getMessage();
                ap.bail(message, false);
            }
        }

        String[] dirs = ap.getRest();

        SVG2GIF svg2gif = new SVG2GIF();
        if (dim != null) {
            svg2gif.setDimension(dim);
        }
        Wanderer wanderer = new Wanderer(svg2gif);


        // Assume that the arguments are paths to directories or
        // files.
        for (int i = 0; i < dirs.length; i++) {
            wanderer.handleEntry(new File(dirs[i]));
        }

    }

    /**
     * @param dim
     */
    protected void setDimension(Dimension dim) {
        imageDimension = dim;
    }

    /**
     * @return Returns the formatter.
     */
    public ImageFormatter getFormatter() {
        return formatter;
    }

    /**
     * @param formatter The formatter to set.
     */
    public void setFormatter(ImageFormatter formatter) {
        this.formatter = formatter;
    }
}