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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NodeColor.java,v $
// $RCSfile: NodeColor.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 17:46:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.awt.Color;

/**
 * A utility class that contains pre-determined node colors that mean
 * something in the NetMap world.
 */
public class NodeColor {

    private static final Color colorPurple = new Color(160, 32, 240);
    private static final Color colorRosyBrown = new Color(188, 143, 143);

    private static final Color[] nodeColors = { Color.cyan, Color.green,
            Color.red, Color.blue, Color.yellow, colorRosyBrown, Color.gray,
            colorPurple, };

    public static Color colorOf(int color) {
        return nodeColors[maptocolor(color)];
    }

    public static int valueOf(Color color) {
        int rgb = color.getRGB();
        for (int i = 0; i < nodeColors.length; i++) {
            if (nodeColors[i].getRGB() == rgb)
                return (colortomap(i));
        }

        return 0;
    }

    private static int colortomap(int i) {
        if (i == 0)
            return 0;
        if (i == 1)
            return 1;
        if (i == 2)
            return 2;
        if (i == 3)
            return 3;
        if (i == 4)
            return 4;
        if (i == 5)
            return 12;
        if (i == 6)
            return 13;
        if (i == 7)
            return 14;

        return 0;
    }

    private static int maptocolor(int i) {
        if (i == 1)
            return 1;
        if (i == 2)
            return 2;
        if (i == 3)
            return 3;
        if (i == 4)
            return 4;
        if (i == 12)
            return 5;
        if (i == 13)
            return 6;
        if (i == 14)
            return 7;

        return 0;
    }
}

