package com.bbn.openmap.tools.j3d.geometry;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;

import javax.media.j3d.Shape3D;

import com.bbn.openmap.MapHandlerChild;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.j3d.MapContent;
import com.bbn.openmap.tools.j3d.OM3DGraphicHandler;
import com.bbn.openmap.tools.j3d.OMGraphicUtil;

/**
 * The curtain class is just a set of planes around the projection
 * boundaries in the map. OpenMap depends on the MapBean clipping for
 * not drawing things on the map. Unfortunately, this means that some
 * leftovers can appear in the 3D scene. Use this class to create a 3D
 * barrier that will hide those leftovers.
 * 
 * @author dietrick
 */
public class Curtain extends MapHandlerChild implements OM3DGraphicHandler {

    /**
     * The color of the curtains.
     */
    protected Color color = Color.lightGray;

    Iterator curtains = null;

    /**
     * An amount to add (or subtract) from the projection boundaries.
     */
    protected double extra = 0;

    /**
     * The height of the box. Default is 500.
     */
    protected double curtainHeight = 500;

    public Curtain() {}

    public void addGraphicsToScene(MapContent mapContent) {

        if (curtains == null) {
            curtains = init(mapContent.getProjection());
        }

        while (curtains.hasNext()) {
            mapContent.add((Shape3D) curtains.next());
        }

        curtains = null;
    }

    protected Iterator init(Projection proj) {

        HashSet set = new HashSet();

        double width = (double) proj.getWidth() + extra;
        double depth = (double) proj.getHeight() + extra;
        double height = curtainHeight + extra;

        // The lower left corner value, or the value of zero.
        double origin = 0 - extra;

        double[] data = new double[60];

        data[0] = origin;
        //width
        data[1] = origin;
        //depth
        data[2] = origin;
        //height
        data[3] = width;
        data[4] = origin;
        data[5] = origin;
        data[6] = width;
        data[7] = depth;
        data[8] = origin;
        data[9] = origin;
        data[10] = depth;
        data[11] = origin;

        data[12] = width;
        data[13] = origin;
        data[14] = origin;
        data[15] = width;
        data[16] = depth;
        data[17] = origin;
        data[18] = width;
        data[19] = depth;
        data[20] = height;
        data[21] = width;
        data[22] = origin;
        data[23] = height;

        data[24] = width;
        data[25] = depth;
        data[26] = origin;
        data[27] = width;
        data[28] = depth;
        data[29] = height;
        data[30] = origin;
        data[31] = depth;
        data[32] = height;
        data[33] = origin;
        data[34] = depth;
        data[35] = origin;

        data[36] = origin;
        data[37] = origin;
        data[38] = origin;
        data[39] = origin;
        data[40] = origin;
        data[41] = height;
        data[42] = origin;
        data[43] = depth;
        data[44] = height;
        data[45] = origin;
        data[46] = depth;
        data[47] = origin;

        data[48] = origin;
        data[49] = origin;
        data[50] = height;
        data[51] = width;
        data[52] = origin;
        data[53] = height;
        data[54] = width;
        data[55] = depth;
        data[56] = height;
        data[57] = origin;
        data[58] = depth;
        data[59] = height;

        int[] stripCount = new int[5];
        stripCount[0] = 4;
        stripCount[1] = 4;
        stripCount[2] = 4;
        stripCount[3] = 4;
        stripCount[4] = 4;

        int dataIndex = 0;
        int[] sc = new int[1];

        for (int i = 0; i < stripCount.length; i++) {

            double[] temp = new double[12];
            sc[0] = stripCount[i];

            System.arraycopy(data, dataIndex, temp, 0, 12);

            set.add(OMGraphicUtil.createFilled(temp, sc, color));

            dataIndex += 12;
        }

        return set.iterator();
    }

    public void setColor(Color curtainColor) {
        color = curtainColor;
    }

    public Color getColor() {
        return color;
    }

    public void setCurtainHeight(double height) {
        curtainHeight = height;
    }

    public double getCurtainHeight() {
        return curtainHeight;
    }
}