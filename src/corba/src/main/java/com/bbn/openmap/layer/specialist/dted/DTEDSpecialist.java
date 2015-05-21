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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/dted/DTEDSpecialist.java,v $
// $RCSfile: DTEDSpecialist.java,v $
// $Revision: 1.6 $
// $Date: 2009/02/23 22:37:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist.dted;

import java.util.StringTokenizer;

import org.omg.CORBA.StringHolder;

import com.bbn.openmap.corba.CSpecialist.CProjection;
import com.bbn.openmap.corba.CSpecialist.CTEntry;
import com.bbn.openmap.corba.CSpecialist.EComp;
import com.bbn.openmap.corba.CSpecialist.GraphicChange;
import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.MouseEvent;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.WidgetChange;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.CColorPackage.EColor;
import com.bbn.openmap.corba.CSpecialist.CStipplePackage.EStipple;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.DeclutterType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.EGraphic;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.RenderType;
import com.bbn.openmap.corba.CSpecialist.RasterPackage.ERaster;
import com.bbn.openmap.layer.dted.DTEDCacheManager;
import com.bbn.openmap.layer.dted.DTEDFrameSubframe;
import com.bbn.openmap.layer.dted.DTEDFrameSubframeInfo;
import com.bbn.openmap.layer.specialist.MakeProjection;
import com.bbn.openmap.layer.specialist.Specialist;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.util.Debug;

/**
 * Implement the Specialist interface so that we can serve graphics to
 * OpenMap via CORBA.
 */
public class DTEDSpecialist extends Specialist {

    protected String[] dtedpaths = new String[] { "/mnt/cdrom/dted",
            "/mnt/disk/dted" };

    protected int ncolors = 216;
    protected int opaque = 255;

    protected DTEDCacheManager cache_manager;

    protected int dtedLevel = DTEDFrameSubframe.LEVEL_0;
    protected int viewType = DTEDFrameSubframe.COLOREDSHADING;
    protected int bandHeight = DTEDFrameSubframe.DEFAULT_BANDHEIGHT;
    protected int slopeAdjust = DTEDFrameSubframe.DEFAULT_SLOPE_ADJUST;

    protected float minScale = 20000000f;

    final private static transient EColor nullColor = new EColor(null, (short) 0, (short) 0, (short) 0);
    final private static transient EStipple nullStipple = new EStipple(null, (short) 0, (short) 0, new byte[0]);
    final private static transient EComp nullComp = new EComp(null, "");
    final private static transient XYPoint nullP1 = new XYPoint((short) 0, (short) 0);

    /**
     * default constructor is called when we're loading the class
     * directly into OpenMap.
     */
    public DTEDSpecialist() {
        super("DTEDSpecialist", (short) 2, true);
    }

    protected void init() {
        cache_manager = new DTEDCacheManager(dtedpaths, ncolors, opaque);
        DTEDFrameSubframeInfo dfsi = new DTEDFrameSubframeInfo(viewType, bandHeight, dtedLevel, slopeAdjust);
        dfsi.colorModel = OMRasterObject.COLORMODEL_INDEXED;
        cache_manager.setSubframeInfo(dfsi);
    }

    public UGraphic[] fillRectangle(CProjection p, LLPoint ll1, LLPoint ll2,
                                    String staticArgs,
                                    StringHolder dynamicArgs,
                                    GraphicChange notifyOnChange,
                                    String uniqueID) {

        System.out.println("DTEDSpecialist.fillRectangle()");
        try {
            OMGraphicList omgraphics;

            if ((p.kind == MakeProjection.CADRGType) && (p.scale < minScale)) {
                omgraphics = cache_manager.getRectangle((CADRG) MakeProjection.getProjection(p));
            } else {
                System.out.println("DTEDSpecialist.fillRectangle(): wrong projection!");
                return new UGraphic[0];
            }

            UGraphic[] ugraphics = createUGraphics(omgraphics);

            System.out.println("DTEDSpecialist.fillRectangle(): got "
                    + ugraphics.length + " graphics");
            return ugraphics;
        } catch (Throwable t) {
            System.err.println("DTEDSpecialist.fillRectangle(): " + t);
            t.printStackTrace();
            throw new RuntimeException();
        }
    }

    protected UGraphic[] createUGraphics(OMGraphicList omgraphics) {
        int len = omgraphics.size();
        UGraphic[] ugraphics = new UGraphic[len];
        ERaster er;
        EGraphic eg;
        UGraphic ug;
        CTEntry[] ct;
        for (int i = 0; i < len; i++) {
            OMRaster omr = (OMRaster) omgraphics.getOMGraphicAt(i);
            eg = createEGraphic();
            ct = createColorTable(omr.getColors());
            er = new ERaster(eg, nullP1, new LLPoint((float) omr.getLat(), (float) omr.getLon()), omr.getBits(), (short) omr.getWidth(), (short) omr.getHeight(), (short) 0, (short) 0, (short) ct.length, ct, (short) omr.getTransparent());
            ug = new UGraphic();
            ug.eras(er);
            ugraphics[i] = ug;
        }
        return ugraphics;
    }

    private static int gid = 0;

    protected EGraphic createEGraphic() {
        EGraphic eg = new EGraphic();
        eg.graph = null;
        eg.obj = nullComp;
        eg.gType = GraphicType.GT_Raster;
        eg.rType = RenderType.RT_LatLon;
        eg.lType = com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType.LT_Unknown;
        eg.dcType = DeclutterType.DC_None;
        eg.lineWidth = 1;
        eg.gID = Long.toString(gid++);
        eg.color = nullColor;
        eg.fillColor = nullColor;
        eg.stipple = nullStipple;
        eg.fillStipple = nullStipple;
        return eg;
    }

    protected CTEntry[] createColorTable(int[] colors) {
        int len = colors.length;
        CTEntry[] colorTable = new CTEntry[len];
        for (int i = 0; i < len; i++) {
            colorTable[i] = new CTEntry((short) ((colors[i] >> 16) & 0xff), (short) ((colors[i] >> 8) & 0xff), (short) ((colors[i]) & 0xff), (short) ((colors[i] >> 24) & 0xff));
        }
        return colorTable;
    }

    public void signOff(String uniqueID) {
        System.out.println("DTEDSpecialist.signOff()");
    }

    public void receiveGesture(MouseEvent gesture, String uniqueID) {}

    public void makePalette(WidgetChange notifyOnChange, String staticArgs,
                            org.omg.CORBA.StringHolder dynamicArgs,
                            String uniqueID) {}

    public void printHelp() {
        System.err.println("usage: java [java/vbj args] <specialist class> [specialist args]");
        System.err.println("");
        System.err.println("       Java Args:");
        System.err.println("       -mx<NUM>m               Set max Java heap in Megs");
        System.err.println("");
        System.err.println("       VBJ Args:");
        System.err.println("       -DORBmbufSize=8388608   Define the VBJ buffer size");
        System.err.println("       -DORBdebug              Enable VBJ debugging");
        System.err.println("");
        System.err.println("       Specialist Args:");
        System.err.println("       -ior <iorfile>                  IOR file");
        System.err.println("       -dtedpaths \"<path1> ...\"      Path to search for DTED data");
        System.err.println("       -level <0|1|2>                  DTED level (default is 0)");
    }

    private String[] getPaths(String str) {
        StringTokenizer tok = new StringTokenizer(str);
        int len = tok.countTokens();
        String[] paths = new String[len];
        for (int j = 0; j < len; j++) {
            paths[j] = tok.nextToken();
        }
        return paths;
    }

    public void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-dtedpaths")) {
                dtedpaths = getPaths(args[++i]);
            } else if (args[i].equalsIgnoreCase("-level")) {
                dtedLevel = Integer.parseInt(args[++i]);
                System.out.println("Setting level " + dtedLevel);
            }
        }
        super.parseArgs(args);
    }

    public static void main(String[] args) {
        Debug.init(System.getProperties());

        // Create the specialist server
        DTEDSpecialist srv = new DTEDSpecialist();
        srv.parseArgs(args);
        srv.init();
        srv.start(args);
    }
}