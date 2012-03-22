package com.bbn.openmap.tools.j3d.geometry;

import java.awt.Color;

import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.grid.SimpleColorGenerator;
import com.bbn.openmap.tools.j3d.MapContent;
import com.bbn.openmap.tools.j3d.OM3DGraphicHandler;

public class GridTester implements OM3DGraphicHandler {

    OMGrid grid;

    public GridTester() {

        int[][] data = new int[][] { new int[] { 10, 10, 10, 10, 10 },
                new int[] { 10, 51, 51, 51, 10 },
                new int[] { 10, 51, 102, 51, 10 },
                new int[] { 10, 51, 51, 51, 10 },
                new int[] { 10, 10, 10, 10, 10 } };

        //      int[][] data = new int[][] {
        //          new int[] {10, 10, 10, 10, 10},
        //          new int[] {10, 20, 20, 20, 10},
        //          new int[] {10, 20, 30, 20, 10},
        //          new int[] {10, 20, 20, 20, 10},
        //          new int[] {10, 10, 10, 10, 10}};

        grid = new OMGrid(43f, -72f, .5f, .5f, data);
        grid.setLinePaint(java.awt.Color.red);
        grid.setFillPaint(OMColor.clear);

        //      grid.setGenerator(new GridTesterGenerator());
    }

    /**
     * Provide a MapContent object for the OM3DGraphicHandler to add
     * objects to. The MapContent object as three add() methods, one
     * for OMGraphics, one for OMGrid specifically, and one for
     * Shape3D objects. The OM3DGraphicHandler should go through its
     * graphics and add them to this MapContext object.
     * 
     * @param mapContent The feature to be added to the
     *        GraphicsToScene attribute
     */
    public void addGraphicsToScene(MapContent mapContent) {
        mapContent.add(grid);
    }

    protected class GridTesterGenerator extends SimpleColorGenerator {

        public GridTesterGenerator() {}

        public int calibratePointValue(int source) {

            if (source > 200) {
                return Color.red.getRGB();
            }
            if (source > 150) {
                return Color.orange.getRGB();
            }
            if (source > 125) {
                return Color.yellow.getRGB();
            }
            if (source > 100) {
                return Color.green.getRGB();
            }
            if (source > 75) {
                return Color.blue.getRGB();
            }
            if (source > 50) {
                return Color.magenta.getRGB();
            }
            return Color.lightGray.getRGB();
        }
    }
}