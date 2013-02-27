/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.cgm;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * A color table for indexed colors.
 * 
 * @author dietrick
 */
public class ColorTable extends Command {

    protected Color[] colors;
    protected boolean drawColorTable = false;
    int startingIndex = 0;

    public ColorTable(int ec, int eid, int l, DataInputStream in) throws IOException {
        super(ec, eid, l, in);

        if (args != null) {

            startingIndex = args[0];

            logger.fine("starting table index: " + startingIndex);

            // Assuming RGB!
            // Subtracting one because the first argument is starting index.

            colors = new Color[args.length - 1 / 3];
            for (int i = 1; i < args.length - 3; i += 3) {
                int index = i / 3;
                int r = args[i];
                int g = args[i + 1];
                int b = args[i + 2];

                logger.fine("Color[" + index + "] r: " + r + ", g: " + g + ", b: " + b);

                colors[index] = new Color(r, g, b);
            }
        }
    }

    public String toString() {
        return "ColorTable has " + args.length + " colors";
    }

    public Color get(int index) {
        index -= startingIndex; // Not sure what starting index means - the
                                // number
                                // of colors to get to 0th color or something to
                                // subtract from provided index to get to front
                                // of
                                // actual array. I think second.
        if (colors != null && index < colors.length) {
            return colors[index];
        } else {
            return Color.GRAY;
        }
    }

    public void paint(CGMDisplay d) {

        if (logger.isLoggable(Level.FINE)) {
            int dim = 30;
            int x = 0;
            int y = 0;
            int width = dim;
            int height = dim;

            int count = 0;

            for (Color c : colors) {

                if (count > 4) {
                    count = 0;
                    y += height;
                    x = 0;
                }

                d.graphics().setColor(c);
                d.graphics().fillRect(x, y, width, height);

                x += width;
                count++;
            }
        }

    }

}
