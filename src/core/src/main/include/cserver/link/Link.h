/* **********************************************************************
 * 
 * <copyright>
 * 
 *  BBN Technologies, a Verizon Company
 *  10 Moulton Street
 *  Cambridge, MA 02138
 *  (617) 873-8000
 * 
 *  Copyright (C) BBNT Solutions LLC. All rights reserved.
 * 
 * </copyright>
 * **********************************************************************
 * 
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/Link.h,v $
 * $RCSfile: Link.h,v $
 * $Revision: 1.4 $
 * $Date: 2007/06/21 21:43:44 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/**
 * The Link object is the main mechanism for communications between a
 * LinkClient (most liekly a LinkLayer) and a LinkServer. This object 
 * defines the communications that either side can make
 *
 * The server should remain connected until the client is finished.  
 * The server can request to be disconnected.
 */

#ifndef LINK_H
#define LINK_H

#define MAX_HEADER_LENGTH 80

/* Used when reading from the link and comparing symbols. */
#define END_TOTAL '\r'
#define END_SECTION '\n'

/* Used when writing to the link. */
#define END_TOTAL_STRING "\r"
#define END_SECTION_STRING "\n"

#define UNKNOWN -1

#define VERSION 0.6

/*see protocol*/

/**
 * Line types.
 *
 * @param LUNKNOWN Unknown line type.
 * @param LSTRAIGHT Straight line.
 * @param LRHUMB Rhumb line.
 * @param LGREATCIRCLE Great circle line.
 */

enum LINETYPE {LUNKNOWN, LSTRAIGHT, LRHUMB, LGREATCIRCLE};

/**
 * Raster image types.
 */

enum RASTERIMAGE {IMAGE_DIRECT_COLOR_MODEL, IMAGE_INDEX_COLOR_MODEL, IMAGE_URL};

/**
 * Render types.
 */
enum RENDERTYPE {RENDERTYPE_UNKNOWN, RENDERTYPE_LATLON, RENDERTYPE_XY, RENDERTYPE_OFFSET};

/**
 * Graphic types.
 */

enum GRAPHICTYPE {GRAPHICTYPE_GRAPHIC, GRAPHICTYPE_BITMAP, GRAPHICTYPE_TEXT, 
                  GRAPHICTYPE_POLY, GRAPHICTYPE_LINE, GRAPHICTYPE_RECTANGLE,
                  GRAPHICTYPE_CIRCLE, GRAPHICTYPE_RASTER,
                  GRAPHICTYPE_GRID, GRAPHICTYPE_POINT, GRAPHICTYPE_ELLIPSE};

/**
 * Geographic units.
 */

enum GEOUNITS {DECIMAL_DEGREES, RADIANS};

/**
 * Distance units.
 */

enum DIST_UNITS {CIRCLE_DECIMAL_DEGREES = -1,KM,MILES,NMILES};

/**
 * Coordinate mode.
 */

enum COORDMODE {COORDMODE_ORIGIN, COORDMODE_PREVIOUS};

/**
 * Justification when writing to the map.
 */
enum JUSTIFICATION {JUSTIFY_LEFT, JUSTIFY_CENTER, JUSTIFY_RIGHT};

/**
 * The various action types.
 */

enum ACTIONTYPE {ACTIONGRAPHIC, ACTIONGUI};

/*  REQUEST HEADERS*/
#define MAP_REQUEST_HEADER "<MR>"
#define lMAP_REQUEST_HEADER strlen(MAP_REQUEST_HEADER)
#define ACTION_REQUEST_HEADER "<AR>"
#define lACTION_REQUEST_HEADER strlen(ACTION_REQUEST_HEADER)
#define GUI_REQUEST_HEADER "<GUIR>"
#define lGUI_REQUEST_HEADER strlen(GUI_REQUEST_HEADER)

/*  RESPONSE HEADERS*/
#define GRAPHICS_RESPONSE_HEADER "<G>"
#define lGRAPHICS_RESPONSE_HEADER strlen(GRAPHICS_RESPONSE_HEADER)
#define GESTURE_RESPONSE_HEADER "<A>"
#define lGESTURE_RESPONSE_HEADER strlen(GESTURE_RESPONSE_HEADER)
#define GUI_RESPONSE_HEADER "<GUI>"
#define lGUI_RESPONSE_HEADER strlen(GUI_RESPONSE_HEADER)
#define CLOSE_LINK_HEADER "<Q>"
#define lCLOSE_LINK_HEADER strlen(CLOSE_LINK_HEADER)
#define HUH_HEADER "<?>"
#define lHUH_HEADER strlen(HUH_HEADER)

/* Graphic Headers*/
#define BITMAP_HEADER "<B>"
#define lBITMAP_HEADER strlen(BITMAP_HEADER)
#define CIRCLE_HEADER "<C>"
#define lCIRCLE_HEADER strlen(CIRCLE_HEADER)
#define ELLIPSE_HEADER "<E>"
#define lELLIPSE_HEADER strlen(ELLIPSE_HEADER)
#define GRID_HEADER "<GD>"
#define lGRID_HEADER strlen(GRID_HEADER)
#define LINE_HEADER "<L>"
#define lLINE_HEADER strlen(LINE_HEADER)
#define POLY_HEADER "<PY>"
#define lPOLY_HEADER strlen(POLY_HEADER)
#define TEXT_HEADER "<T>"
#define lTEXT_HEADER strlen(TEXT_HEADER)
#define RASTER_HEADER "<RA>"    
#define lRASTER_HEADER strlen(RASTER_HEADER)
#define RECTANGLE_HEADER "<RE>"
#define lRECTANGLE_HEADER strlen(RECTANGLE_HEADER) 
#define POINT_HEADER "<PO>"
#define lPOINT_HEADER strlen(POINT_HEADER)
#define GRAPHIC_HEADER "<GD>"
#define lGRAPHIC_HEADER strlen(GRAPHIC_HEADER)
  
/* Gesture Response Headers*/
#define UPDATE_GRAPHICS "<UG>"
#define lUPDATE_GRAPHICS strlen(UPDATE_GRAPHICS)
#define UPDATE_GUI "<UGUI>"
#define lUPDATE_GUI strlen(UPDATE_GUI)
#define UPDATE_MAP "<UM>"
#define lUPDATE_MAP strlen(UPDATE_MAP)

/*
  Predefined keys for key value pairs stored in the LinkArgs.
*/

/* For graphic attributes. */
/** The line color attribute name. */
#define LPC_LINECOLOR "lc"
/** The highlight color attribute name. */
#define LPC_HIGHLIGHTCOLOR "hc"
/** The fill color attribute name. */
#define LPC_FILLCOLOR "fc"
/** The line width attribute name. */
#define LPC_LINEWIDTH "lw"
/** The text graphic contents attribute name. */
#define LPC_LINKTEXTSTRING "ts"
/** The font representation attribute name. */
#define LPC_LINKTEXTFONT "tf"
/** The attribute name for a URL for an image in LinkRaster.  Only
    used in some circumstances. */
#define LPC_LINKRASTERIMAGEURL "riu"
/** The graphic identifier attribute name. */
#define LPC_GRAPHICID "gid"

/* Constants that can fire a information delegator action.*/
/** The URL text attribute name. */
#define LPC_URL "url"
/** The HTML text (displayed in a browser) attribute name. */
#define LPC_HTML "html"
/** The Information Line (status line) attribute name. */
#define LPC_INFO "info"
/** The Message text (in a pop-up window) attribute name. */
#define LPC_MESSAGE "mess"
    
struct MapRequest;
struct ActionRequest;
struct LinkSocket;
/**
 * The link.
 *
 * @param *socket The link socket.
 * @param *mapRequest The map request.
 * @param *actionRequest The action request.
 * @param closeLink LINK_TRUE if the link is closed.  LINK_FALSE, otherwise.
 */

struct Link {  
    struct LinkSocket *socket;
    struct MapRequest *mapRequest;
    struct ActionRequest *actionRequest;
    int closeLink; /*LINK_TRUE if link is closed, LINK_FALSE otheriwse*/
};
typedef struct Link Link;

/**
 * Initializes the socket and makes it ready for communication.
 *
 * @param *link The link socket.
 * @returns OK if the socket was successfully created, NOK if it failed.
 * @returns -1 if there was a memory allocation error. 
 */

int CreateLink(Link *link);

/**
 * Reads the headers and creates corresponding objects.  Fills data from the
 * socket into Request Objects.
 *
 * @param *link The link object.
 * @returns HEADERSUCCESS if successful, HEADERERROR if it fails.
 * @returns MEMORYERROR if there was a memory allocation error.

 */

char ReadAndParseLink(Link *link);

/**
 * Deallocates the memory used internally by the link.
 * Note: The user should still call free(link) to free other memory.
 *
 * @param *link The link object.
 */

void FreeLink(Link *link);

void SendHuh(Link *link);

#endif







