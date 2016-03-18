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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDCoverageManager.java,v $
// $RCSfile: DTEDCoverageManager.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

/*  Java Core  */
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.wanderer.Wanderer;
import com.bbn.openmap.util.wanderer.WandererCallback;

/**
 * A DTEDCoverageManager knows how to look at DTED data and figure out what
 * coverage is available.
 */
public class DTEDCoverageManager extends OMGraphicList implements PropertyConsumer {

    protected I18n i18n = Environment.getI18n();

    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.dted.DTEDCoverageManager");

    protected String[] paths;

    /** The default line color for level 0. */
    public final static String DEFAULT_LEVEL0_COLOR_STRING = "CE4F3F"; // redish
    /** The default line color for level 1. */
    public final static String DEFAULT_LEVEL1_COLOR_STRING = "339159"; // greenish
    /** The default line color for level 2. */
    public final static String DEFAULT_LEVEL2_COLOR_STRING = "0C75D3"; // bluish

    public final static String COVERAGE_FILE_PROPERTY = "coverageFile";

    /** Coverage for level 0, 1, 2 frames */
    protected boolean[][][] coverage = null;
    /**
     * CoverageDataFile object handling pre-cached coverage
     */
    protected CoverageDataFile coverageFile = null;

    protected DrawingAttributes[] attributes = null;
    protected OMGraphicList[] levelRects = null;

    public DTEDCoverageManager(String[] paths) {
        this.paths = paths;

        attributes = new DrawingAttributes[3];
        attributes[0] = DrawingAttributes.getDefaultClone();
        attributes[0].setLinePaint(PropUtils.parseColor(DEFAULT_LEVEL0_COLOR_STRING));
        attributes[1] = DrawingAttributes.getDefaultClone();
        attributes[1].setLinePaint(PropUtils.parseColor(DEFAULT_LEVEL1_COLOR_STRING));
        attributes[2] = DrawingAttributes.getDefaultClone();
        attributes[2].setLinePaint(PropUtils.parseColor(DEFAULT_LEVEL2_COLOR_STRING));

        levelRects = new OMGraphicList[3];
        levelRects[0] = new OMGraphicList();
        levelRects[1] = new OMGraphicList();
        levelRects[2] = new OMGraphicList();
    }

    public void reset() {
        coverage = null;
        clear();
    }

    /**
     * The method that cycles through all the paths, looking for the frames.
     * This takes time, so it's only done when a coverage file can't be found.
     * 
     * @param paths paths to the level 0, 1 and 2 dted root directory.
     */
    public boolean[][][] checkOutCoverage(String[] paths) {

        if (paths == null || paths.length == 0) {
            logger.warning("No paths for DTED data given.");
            return null;
        }

        logger.fine("checking out DTED at paths:");
        for (int d1 = 0; d1 < paths.length; d1++) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("       " + paths[d1]);
            }
            if (!BinaryFile.exists(paths[d1])) {
                paths[d1] = null;
                logger.fine("       - path invalid, ignoring.");
            }
        }

        CoverageWandererCallback callback = new CoverageWandererCallback();
        Wanderer wanderer = new Wanderer(callback);

        for (int pathNum = 0; pathNum < paths.length; pathNum++) {
            wanderer.handleEntry(new File(paths[pathNum]));
        }

        return callback.getCoverage();
    }

    /**
     * Method organizes the query based on the projection, and returns the
     * applicable rectangles representing the frame coverage. If the coverage
     * spans over the date line, then two queries are performed, one for each
     * side of the date line.
     * 
     * @param proj the projection of the screen
     * @return an array of lists, one for each level of dted data.
     */
    public OMGraphicList getCoverageRects(Projection proj) {

        if (coverage == null) {

            if (coverageFile != null) {
                coverage = coverageFile.readCoverage();
            }

            if (coverage == null) {
                logger.fine("Scanning for frames - This could take several minutes!");
                coverage = checkOutCoverage(paths);

                if (coverageFile != null) {
                    coverageFile.writeFile(coverage);
                }
            }
        }

        if (isEmpty()) {
            getCoverageRects(-180, -90, 179, 89, OMGraphic.LINETYPE_RHUMB, proj);
        } else {
            generate(proj);
        }
        return this;
    }

    /**
     * Get a percentage value of how much of the map is covered for a
     * projection.
     * 
     * @param proj
     * @return float[] with percentages, float[0] is level 0 coverage, 1 is
     *         level 1, 2 is level 2.
     */
    public float[] getCoverage(Projection proj) {
        float[] ret = new float[3];
        if (coverage != null) {
            Point pnt1 = new Point();
            Point pnt2 = new Point();
            int height = proj.getHeight();
            int width = proj.getWidth();
            // Number frames possible on map
            int total = 0;
            for (int x = -180; x < 180; x++) {
                for (int y = -90; y < 89; y++) {
                    proj.forward((float) y, (float) x, pnt1);
                    proj.forward((float) (y + 1), (float) (x + 1), pnt2);

                    double x1 = pnt1.getX();
                    double y1 = pnt1.getY();
                    double x2 = pnt2.getX();
                    double y2 = pnt2.getY();

                    boolean someX = (x1 >= 0 && x1 <= width) || (x2 >= 0 && x2 <= width);
                    boolean someY = (y1 >= 0 && y1 <= height) || (y2 >= 0 && y2 <= height);

                    boolean onMap = someX && someY;

                    if (onMap) {
                        int xIndex = x + 180;
                        int yIndex = y + 90;
                        total++;
                        if (coverage[0][yIndex][xIndex])
                            ret[0] += 1f;
                        if (coverage[1][yIndex][xIndex])
                            ret[1] += 1f;
                        if (coverage[2][yIndex][xIndex])
                            ret[2] += 1f;
                    }
                }
            }

            logger.info("Total frames: " + total + " " + ret[0] + ", " + ret[1] + ", " + ret[2]);

            ret[0] = ret[0] / total * 100f;
            ret[1] = ret[1] / total * 100f;
            ret[2] = ret[2] / total * 100f;
        }
        return ret;
    }

    /**
     * Method looks at the coverage arrays, and returns the applicable
     * rectangles representing the frame coverages.
     * 
     * @param startx the western-most longitude.
     * @param starty the southern-most latitude.
     * @param endx the eastern-most longitude.
     * @param endy the northern-most latitude.
     * @param lineType the type of line to use on the rectangles - Cylindrical
     *        projections can use straight lines, but other projections should
     *        use Rhumb lines.
     * @return an array of lists, one for each level of dted data.
     */
    public OMGraphicList getCoverageRects(int startx, int starty, int endx, int endy, int lineType,
                                          Projection proj) {
        clear();
        OMRect rect;

        for (int level = 0; level < 3; level++) {

            OMGraphicList rectangles = levelRects[level];
            rectangles.clear();
            rectangles.setVague(true);

            for (int lat = starty; lat <= endy && lat < 90; lat++) {
                for (int lon = startx; lon <= endx && lon < 180; lon++) {
                    if (coverage[level][lat + 90][lon + 180]) {

                        double offset = level * .1;
                        double up = lat + offset;
                        double left = lon + offset;
                        double down = lat + 1.0 - offset;
                        double right = lon + 1.0 - offset;

                        rect = new OMRect(up, left, down, right, lineType);
                        attributes[level].setTo(rect);
                        rect.generate(proj);
                        rectangles.add(rect);
                    }
                }
            }
            add(rectangles);
        }

        return this;
    }

    ////// PropertyConsumer methods

    protected String prefix;

    public Properties getProperties(Properties getList) {
        if (getList == null) {
            getList = new Properties();
        }

        for (DrawingAttributes atts : attributes) {
            atts.getProperties(getList);
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        if (coverageFile != null) {
            getList.put(prefix + COVERAGE_FILE_PROPERTY, coverageFile.getAbsolutePath());
        }

        if (paths != null) {
            StringBuilder sBuilder = new StringBuilder();
            for (String path : paths) {
                if (sBuilder.length() != 0) {
                    sBuilder.append(";");
                }
                sBuilder.append(path);
            }
            getList.put(prefix + DTEDLayer.DTEDPathsProperty, sBuilder.toString());
        }

        return getList;
    }

    public Properties getPropertyInfo(Properties list) {
        return list;
    }

    public String getPropertyPrefix() {
        return prefix;
    }

    public void setProperties(Properties setList) {
        setProperties(null, setList);
    }

    public void setProperties(String prefix, Properties setList) {
        setPropertyPrefix(prefix);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        attributes[0].setProperties(prefix + "0", setList);
        attributes[1].setProperties(prefix + "1", setList);
        attributes[2].setProperties(prefix + "2", setList);

        String coverageFileString = setList.getProperty(prefix + COVERAGE_FILE_PROPERTY);
        if (coverageFileString != null) {
            coverageFile = new CoverageDataFile(coverageFileString);
        }
    }

    public void setPropertyPrefix(String prefix) {
        this.prefix = prefix;
    }

    ////// end of PropertyConsumer methods

    /**
     * @return the coverageFile
     */
    public CoverageDataFile getCoverageFile() {
        return coverageFile;
    }

    /**
     * @param coverageFile the coverageFile to set
     */
    public void setCoverageFile(CoverageDataFile coverageFile) {
        this.coverageFile = coverageFile;
    }

    protected JPanel panel;

    public Component getGUI(final OMGraphicHandlerLayer layer) {
        if (panel == null) {
            panel = new JPanel();

            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            panel.setLayout(gridbag);

            ActionListener aListener = new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int level = Integer.parseInt(ae.getActionCommand());
                    levelRects[level].setVisible(((JCheckBox) ae.getSource()).isSelected());
                    layer.doPrepare();
                }
            };

            for (int level = 0; level < 3; level++) {

                JPanel pane = new JPanel();
                String interString = i18n.get(DTEDCoverageManager.class, "level" + level
                        + "title", "Level " + level + ": ");
                pane.add(new JLabel(interString));
                String showString = i18n.get(DTEDCoverageManager.class, "show", "Show");
                JCheckBox jcb = new JCheckBox(showString, levelRects[level].isVisible());
                jcb.addActionListener(aListener);
                jcb.setActionCommand(Integer.toString(level));
                pane.add(jcb);
                pane.add(attributes[level].getGUI());
                c.gridy = level;
                gridbag.setConstraints(pane, c);
                panel.add(pane);
            }
        }

        return panel;
    }

    /**
     * WandererCallback class that provides coverage array based on existance of
     * DTED frames.
     *
     * @author dietrick
     */
    static class CoverageWandererCallback implements WandererCallback {

        boolean[][][] cov;
        protected int curLon = Integer.MAX_VALUE;

        CoverageWandererCallback() {
            cov = new boolean[3][180][360];
        }

        public boolean handleDirectory(File directory) {
            String name = directory.getName().toLowerCase();
            char hemi = name.charAt(0);
            if (name.length() == 4 && (hemi == 'e' || hemi == 'w')) {
                try {
                    // Get the longitude index right, use hemi to set the +/-,
                    // and
                    // then add 180 to get indexy.
                    curLon = (hemi == 'w' ? -1 : 1) * Integer.parseInt(name.substring(1)) + 180;

                } catch (NumberFormatException nfe) {
                    curLon = Integer.MAX_VALUE;
                    logger.warning("Can't process " + name);
                }
            }
            return true;
        }

        public boolean handleFile(File file) {
            if (curLon != Integer.MAX_VALUE) {
                String name = file.getName().toLowerCase();
                char hemi = name.charAt(0);
                char level = name.charAt(name.length() - 1);
                if (name.length() == 7 && name.charAt(name.length() - 4) == '.'
                        && (hemi == 'n' || hemi == 's')) {

                    try {
                        int curLat = (hemi == 's' ? -1 : 1)
                                * Integer.parseInt(name.substring(1, name.length() - 4)) + 90;

                        if (level == '0') {
                            cov[0][curLat][curLon] = true;
                        } else if (level == '1') {
                            cov[1][curLat][curLon] = true;
                        } else if (level == '2') {
                            cov[2][curLat][curLon] = true;
                        }

                    } catch (NumberFormatException nfe) {
                        logger.warning("Can't process " + name);
                    }

                }

            }

            return true;
        }

        boolean[][][] getCoverage() {
            return cov;
        }

    }

    /**
     * Inner class that handles reading and writing coverage cache file.
     *
     * @author dietrick
     */
    public static class CoverageDataFile {

        File coverageFile = null;

        public CoverageDataFile(String path) {
            coverageFile = new File(path);
        }

        public boolean exists() {
            return coverageFile != null && coverageFile.exists();
        }

        public String getAbsolutePath() {
            if (coverageFile != null) {
                return coverageFile.getAbsolutePath();
            }

            return "";
        }

        public boolean[][][] readCoverage() {
            if (exists()) {

                try {
                    RandomAccessFile raf = new RandomAccessFile(coverageFile, "rw");

                    boolean[][][] coverage = new boolean[3][180][360];
                    for (int level = 0; level < 3; level++) {
                        for (int y = 0; y < 180; y++) {
                            for (int x = 0; x < 360; x++) {
                                coverage[level][y][x] = raf.readBoolean();
                            }
                        }
                    }
                    raf.close();
                    return coverage;
                } catch (FileNotFoundException e) {

                } catch (IOException ioe) {

                }
            }
            return null;
        }

        public void writeFile(boolean[][][] coverage) {
            try {
                RandomAccessFile raf = new RandomAccessFile(coverageFile, "rw");

                for (int level = 0; level < 3; level++) {
                    for (int y = 0; y < 180; y++) {
                        for (int x = 0; x < 360; x++) {
                            raf.writeBoolean(coverage[level][y][x]);
                        }
                    }
                }
                raf.close();
            } catch (FileNotFoundException e) {

            } catch (IOException ioe) {

            } catch (ArrayIndexOutOfBoundsException aioobe) {

            }
        }

    }

}