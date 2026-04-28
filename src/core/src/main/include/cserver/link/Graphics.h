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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/Graphics.h,v $
 * $RCSfile: Graphics.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef GRAPHICS_H
#define GRAPHICS_H

/*deprecated...use constants in link.h*/

/*
  Constant definations for Graphics Objects
*/
#define RENDERTYPE_UNKNOWN 0
#define RENDERTYPE_LATLON 1
#define RENDERTYPE_XY 2
#define RENDERTYPE_OFFSET 3

#define GRAPHICTYPE_GRAPHIC 0
#define GRAPHICTYPE_BITMAP 1
#define GRAPHICTYPE_TEXT 2
#define GRAPHICTYPE_POLY 3
#define GRAPHICTYPE_LINE 4
#define GRAPHICTYPE_RECTANGLE 5
#define GRAPHICTYPE_CIRCLE 6
#define GRAPHICTYPE_RASTER 7
#define GRAPHICTYPE_GRID 8

#define DECIMAL_DEGREES 0
#define RADIANS 1

#define KM 0
#define MILES 1
#define NMILES 2

#define COORDMODE_ORIGIN 0
#define COORDMODE_PREVIOUS 1

#define JUSTIFY_LEFT 0
#define JUSTIFY_CENTER 1
#define JUSTIFY_RIGHT 2

/* For the Grid objects, defining the primary dimension of the data. */
#define COLUMN_MAJOR 0
#define ROW_MAJOR 1


#endif
