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
// $Revision: 1.1 $
// $Date: 2004/12/08 01:23:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.svg;

import java.awt.Color;
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
 * Count the source lines of code but going through the directory and
 * counting ; and }.
 */
public class SVG2GIF implements WandererCallback {

    boolean DETAIL = false;

    protected ImageFormatter formatter;
    
    public SVG2GIF() {
        formatter = new AcmeGifFormatter();
    }
    
    // do nothing on directories
    public void handleDirectory(File directory) {}

    // count the ; and } in each file.
    public void handleFile(File file) {
        if (!file.getName().endsWith(".svg")) {
            return;
        }

        try {
            SVGRasterizer svgr;
            svgr = new SVGRasterizer(file.toURL());
            svgr.setBackgroundColor(new Color(128, 128, 128, 0));
            BufferedImage bi = svgr.createBufferedImage();
            byte[] imageBytes = formatter.formatImage(bi);
            
            String newFileName = file.toString().replaceAll(".svg", ".gif");
            Debug.output("writing " + newFileName + " from " + file);
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

        ArgParser ap = new ArgParser("SVG");

        if (argv.length == 0) {
            ap.bail("Usage: java com.bbn.openmap.util.wanderer.SVG <dir>",
                    false);
        }

        ap.parse(argv);

        String[] dirs = argv;

        SVG2GIF svg2gif = new SVG2GIF();
        Wanderer wanderer = new Wanderer(svg2gif);

        int runningTotal = 0;

        // Assume that the arguments are paths to directories or
        // files.
        for (int i = 0; i < dirs.length; i++) {
            wanderer.handleEntry(new File(dirs[i]));
        }

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