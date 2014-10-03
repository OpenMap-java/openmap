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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/asrp/ASRPLayer.java,v $
// $RCSfile: ASRPLayer.java,v $
// $Revision: 1.8 $
// $Date: 2005/08/25 16:04:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.asrp;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.bbn.openmap.I18n;
import com.bbn.openmap.dataAccess.asrp.ASRPConstants;
import com.bbn.openmap.dataAccess.asrp.ASRPDirectory;
import com.bbn.openmap.dataAccess.asrp.ASRPDirectoryHandler;
import com.bbn.openmap.dataAccess.asrp.TransmittalHeaderFile;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The ASRPLayer displays ASRP data, which is an international,
 * seamless imagery format, kind of like CADRG. This data must be
 * displayed on either LLYX or CADRG projections, although the layer
 * will display coverage rectangles over areas with imagery for
 * projections that are not the right type, or if the user should zoom
 * in or out to view the images. The properties for this layer are:
 * <P>
 * 
 * <pre>
 *  
 *   
 *   
 *    # Paths to TRANSH01.THF files that organize sets of ASRP image directories.
 *    asrpLayer.thf=thf1;thf2
 *    # Alternatively, paths to ASRP directories can be used if the thf property is undefined.
 *    asrpLayer.asrpDirs=dir1;dir2
 *    # Flag to show coverages when images can't be displayed.
 *    asrpLayer.showCoverage
 *    # Lastly, the suite of DrawingAttributes properties can be provided to set 
 *    # the parameters for the coverage rectangles. See DrawingAttributes for more options.
 *    asrpLayer.lineColor=FFFF0000
 *    asrpLayer.fillColor=FFFF0000
 *   
 *    
 *   
 * </pre>
 */
public class ASRPLayer extends OMGraphicHandlerLayer implements
        DataBoundsProvider, ASRPConstants {

    protected ASRPDirectoryHandler asrpHandler;
    protected String[] thfPaths = null;
    protected String[] asrpDirs = null;

    protected boolean showCoverage = true;

    protected DrawingAttributes coverageDrawingAttributes;

    /**
     * Property describing a flag that can be set to show where image
     * files are when they cannot be displayed or aren't showing up.
     */
    public final static String ShowCoverageProperty = "showCoverage";
    /**
     * The ASRPDirectory can be used to view the images from ASRP
     * directories containing GEN, GER, SOU, QAL and IMG files. This
     * property is only checked if the THFProperty is not set
     * (asrpDir). Should contain a list of semi-colon separated paths
     * to ASRP directories containing the files specified above.
     */
    public final static String ASRPDirectoryProperty = "asrpDirs";
    /**
     * A semi-colon separated set of paths to TRANSH01.THF files
     * containing information about ASRP images stored in directories
     * next to the TRANSH01.THF file.
     */
    public final static String THFProperty = "thf";

    public ASRPLayer() {
        setName("ASRP");
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
        coverageDrawingAttributes = DrawingAttributes.getDefaultClone();
    }

    public synchronized OMGraphicList prepare() {

        OMGraphicList ret = null;
        Projection proj = getProjection();

        if (proj == null) {
            return ret;
        }

        try {

            if (asrpHandler == null) {
                asrpHandler = initialize();
            }

            if (!(proj instanceof EqualArc)) {
                fireRequestInfoLine("ASRP data requires an Equal Arc projection (CADRG/LLXY)");
            } else {
                ret = asrpHandler.getImagesForProjection((EqualArc) proj);
            }

            if (ret == null && showCoverage) {
                ret = asrpHandler.getCoverageBounds(proj,
                        coverageDrawingAttributes);
            }

        } catch (IOException ioe) {
            Debug.error("ASRPLayer(" + getName()
                    + ") caught exception fetching images:\n"
                    + ioe.getMessage());
        }

        return ret;
    }

    protected ASRPDirectoryHandler initialize() {
        ASRPDirectoryHandler asrpDirHandler = new ASRPDirectoryHandler();
        int i;
        if (thfPaths != null) {
            for (i = 0; i < thfPaths.length; i++) {
                try {
                    asrpDirHandler.add(new TransmittalHeaderFile(thfPaths[i]));
                } catch (IOException ioe) {
                    Debug.error("ASRPLayer (" + getName()
                            + ") caught exception trying to read "
                            + ASRPConstants.TRANS + ": " + ioe.getMessage());
                }
            }
        } else if (asrpDirs != null) {
            for (i = 0; i < asrpDirs.length; i++) {
                asrpDirHandler.add(new ASRPDirectory(asrpDirs[i]));
            }
        }

        return asrpDirHandler;
    }

    public ASRPDirectoryHandler getASRPHandler() {
        return asrpHandler;
    }

    public void setASRPHandler(ASRPDirectoryHandler asrpHandlerIn) {
        asrpHandler = asrpHandlerIn;
    }

    /**
     * Set the paths used by the layer. Clears out the ASRP List
     * currently set. The contents of the provided paths will be
     * checked to see if they are paths to TRANSH01.THF files, and
     * will be set to the ASRP directory files if they aren't. Call
     * doPrepare() on this layer if you want the changes to take
     * effect immediately.
     */
    public void setPaths(String[] pathsIn) {
        if (pathsIn != null) {
            if (pathsIn[0].indexOf(ASRPConstants.TRANS) != -1) {
                thfPaths = pathsIn;
            } else {
                asrpDirs = pathsIn;
            }

            asrpHandler = null;// next projection change, we reload.
        }
    }

    /**
     * Returns THF paths if they are set, otherwise returns ASRP
     * directory paths.
     */
    public String[] getPaths() {
        if (thfPaths != null) {
            return thfPaths;
        } else {
            return asrpDirs;
        }
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        coverageDrawingAttributes.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        thfPaths = PropUtils.initPathsFromProperties(props, prefix
                + THFProperty, thfPaths);
        if (thfPaths == null) {
            asrpDirs = PropUtils.initPathsFromProperties(props, prefix
                    + ASRPDirectoryProperty, asrpDirs);
        }

        showCoverage = PropUtils.booleanFromProperties(props, prefix
                + ShowCoverageProperty, showCoverage);

    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        // find out paths...
        if (thfPaths != null) {
            String[] p = thfPaths;
            StringBuffer pathString = new StringBuffer();
            if (p != null) {
                for (int i = 0; i < p.length; i++) {
                    if (p[i] != null) {
                        pathString.append(p[i]);
                        if (i < p.length - 1) {
                            pathString.append(";"); // separate paths
                            // with ;
                        }
                    }
                }
            }
            props.put(prefix + THFProperty, pathString.toString());
        } else {
            props.put(prefix + THFProperty, "");
        }

        if (asrpDirs != null) {
            String[] p = asrpDirs;
            StringBuffer pathString = new StringBuffer();
            if (p != null) {
                for (int i = 0; i < p.length; i++) {
                    if (p[i] != null) {
                        pathString.append(p[i]);
                        if (i < p.length - 1) {
                            pathString.append(";"); // separate paths
                            // with ;
                        }
                    }
                }
            }
            props.put(prefix + ASRPDirectoryProperty, pathString.toString());
        } else {
            props.put(prefix + ASRPDirectoryProperty, "");
        }

        props.put(prefix + ShowCoverageProperty,
                new Boolean(showCoverage).toString());

        coverageDrawingAttributes.setPropertyPrefix(prefix);
        coverageDrawingAttributes.getProperties(props);

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        String interString;

        interString = i18n.get(ASRPLayer.class,
                THFProperty,
                I18n.TOOLTIP,
                "Paths to TRANSH01.THF files, takes precedence over ASRP property.  Separated by ;");
        props.put(THFProperty, interString);
        interString = i18n.get(ASRPLayer.class,
                THFProperty,
                "TRANSH01.THF files");
        props.put(THFProperty + LabelEditorProperty, interString);
        props.put(THFProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.MultiDirFilePropertyEditor");

        interString = i18n.get(ASRPLayer.class,
                ASRPDirectoryProperty,
                I18n.TOOLTIP,
                "Paths to ASRP Directories (if no TRANSH01.THF files)");
        props.put(ASRPDirectoryProperty, interString);
        interString = i18n.get(ASRPLayer.class,
                ASRPDirectoryProperty,
                "ASRP directories");
        props.put(ASRPDirectoryProperty + LabelEditorProperty, interString);
        props.put(ASRPDirectoryProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.MultiDirectoryPropertyEditor");

        interString = i18n.get(ASRPLayer.class,
                ShowCoverageProperty,
                I18n.TOOLTIP,
                "Show coverage areas when images can't be displayed.");
        props.put(ShowCoverageProperty, interString);
        interString = i18n.get(ASRPLayer.class,
                ShowCoverageProperty,
                "Show Coverage Areas");
        props.put(ShowCoverageProperty + LabelEditorProperty, interString);
        props.put(ShowCoverageProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        coverageDrawingAttributes.getPropertyInfo(props);

        interString = i18n.get(ASRPLayer.class,
                DrawingAttributes.linePaintProperty,
                I18n.TOOLTIP,
                "Edge color for coverage area markings.");
        props.put(DrawingAttributes.linePaintProperty, interString);
        
        interString = i18n.get(ASRPLayer.class,
                DrawingAttributes.lineWidthProperty,
                I18n.TOOLTIP,
                "Line Width for coverage area markings.");
        props.put(DrawingAttributes.lineWidthProperty, interString);
        
        interString = i18n.get(ASRPLayer.class,
                DrawingAttributes.fillPaintProperty,
                I18n.TOOLTIP,
                "Fill color for coverage area markings.");
        props.put(DrawingAttributes.fillPaintProperty, interString);
        
        props.put(initPropertiesProperty, THFProperty + " "
                + ASRPDirectoryProperty + " " + ShowCoverageProperty + " "
                + DrawingAttributes.linePaintProperty + " "
                + DrawingAttributes.lineWidthProperty + " "
                + DrawingAttributes.fillPaintProperty);

        return props;
    }

    /**
     * DataBoundsInformer interface.
     */
    public DataBounds getDataBounds() {
        DataBounds box = null;

        if (asrpHandler != null) {
            box = asrpHandler.getDataBounds();
        }

        return box;
    }

    public void setShowCoverage(boolean showCoverageIn) {
        showCoverage = showCoverageIn;
    }

    public boolean getShowCoverage() {
        return showCoverage;
    }

    protected JPanel guiPanel = null;

    public Component getGUI() {
        if (guiPanel == null) {
            JPanel gp = new JPanel();
            gp.setLayout(new GridLayout(0, 1));
            String interString = i18n.get(ASRPLayer.class,
                    "showCoverageCheck",
                    "Show Coverage");
            JCheckBox coverageCheck = new JCheckBox(interString, getShowCoverage());
            interString = i18n.get(ASRPLayer.class,
                    "showCoverageCheck",
                    I18n.TOOLTIP,
                    "Show coverage areas when images can't be displayed.");
            coverageCheck.setToolTipText(interString);
            coverageCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox jcb = (JCheckBox) ae.getSource();
                    setShowCoverage(jcb.isSelected());
                    doPrepare();
                }
            });

            coverageDrawingAttributes.getPropertyChangeSupport()
                    .addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent pce) {
                            doPrepare();
                        }
                    });

            interString = i18n.get(ASRPLayer.class,
                    "coveragePanelTitle",
                    "Coverage Controls");
            JPanel covPanel = com.bbn.openmap.util.PaletteHelper.createVerticalPanel(interString);

            covPanel.add(coverageCheck);
            covPanel.add(coverageDrawingAttributes.getGUI());
            gp.add(covPanel);
            guiPanel = gp;
        }

        return guiPanel;
    }

}