#ifndef REQUEST_H
#define REQUEST_H

/*
This file defines structures and methods that are common to MapRequest, 
ActionRequest and GuiRequest
*/

#include "LatLonPoint.h"

/**
 * The polygon structure.
 *
 * @param numberOfPoints Number of points in the polygon.
 * @param *points Array of lat-long point objects that describe the polygon.
 */

struct Polygon {
    int numberOfPoints;
    LatLonPoint *points;
};
typedef struct Polygon Polygon;

/**
 * The bounding polygon structure.
 *
 * @param numberOfPolygons The number of polygons.
 * @param *polygons The polygons.
 */

struct BoundingPolygon {
    int numberOfPolygons;
    Polygon *polygons;
};
typedef struct BoundingPolygon BoundingPolygon;

/**
 * Reads the bounding polygon from a socket.
 *
 * @param *linkSocket The link socket.
 * @param *bpoly The bounding polygon.
 * @returns -1 if there was a memory allocation error.
 */

int ReadBoundingPolygons(LinkSocket *linkSocket, BoundingPolygon *bpoly);

/**
 * Reads polygons off the socket.
 *
 * @param *linkSocket The link socket.
 * @param *poly The polygon to be read.
 * @returns -1 if there was a memory allocation error.
 */

int ReadPolygons(LinkSocket *linkSocket, Polygon *poly);

/**
 * Frees the memory from a given bounding polygon.
 *
 * @param *bpoly The bounding polygon to free memory from.
 */

void FreeBoundingPolygons(BoundingPolygon *bpoly);

/**
 * Frees the memory from a given polygon.
 * 
 * @param *poly The polygon to free memory from.
 */

void FreePolygons(Polygon *poly);

/*--------------------------------------------------*/

/**
 * The descriptor.
 *
 * @param :20 Unused bits.
 * @param SERVER_NOTIFICATION Server should be notified even if the client acts on some property in a graphic during gesture handling.
 * @param CLIENT_NOTIFICATION The server is interested only in messages whose bit value is set.
 * @param IS_GRAPHIC_INVOLVED
 * @param KEY_RELEASED On the keyboard.
 * @param KEY_PRESSED On the keyboard.
 * @param MOUSE_DRAGGED With the left button down.
 * @param MOUSE_EXIT THe mouse exiting the area of interest (map).
 * @param MOUSE_ENTER The mouse entering the area of interest.
 * @param MOUSE_MOVE A simple move over the area of interest.
 * @param MOUSE_RELEASED The mouse button is released (second half of a click).
 * @param MOUSE_PRESSED The mouse button is pressed (first half of a click).
 * @param MOUSE_CLICK The mouse button is pressed and released.
 */

struct Descriptor
{
/**@#-*/
    unsigned :20;  /*unused bits*/
/**@#+*/
    unsigned SERVER_NOTIFICATION  :1; /*Server should be notified even
                                        if the client acts on some
                                        property in a graphic during
                                        gesture handling. */
    unsigned CLIENT_NOTIFICATION  :1; /*server is interested only in 
                                        messages whose bit value is set*/
    unsigned IS_GRAPHIC_INVOLVED  :1; 
    unsigned KEY_RELEASED         :1; /*ON KEYBOARD*/
    unsigned KEY_PRESSED          :1; /*ON KEYBOARD*/
    unsigned MOUSE_DRAGGED        :1; /*WITH LEFT BUTTON DOWN*/
    unsigned MOUSE_EXIT           :1; /*Mouse Exit area of interest (MAP)*/
    unsigned MOUSE_ENTER          :1; /*Mouse Enter area of interest (MAP)*/
    unsigned MOUSE_MOVE           :1; /* Simple move over area of interest(MAP) */
    unsigned MOUSE_RELEASED       :1; /*mouse button released .. 2nd half of click*/
    unsigned MOUSE_PRESSED        :1; /*mouse button pressed..half click */  
    unsigned MOUSE_CLICK          :1;/*mouse button pressed and released*/    
};
typedef struct Descriptor Descriptor;

/**
 * The modifier
 *
 * @param :27 Unused bits.
 * @param KEY_SHIFT_PRESSED
 * @param KEY_CONTROL_PRESSED
 * @param META_CHANGE The meta key is pressed OR the mouse's third button changed its state.
 * @param ALT_CHANGE The alt key is pressed OR the mouse's second button changed its state.
 * @param KEY_ALT_GRAPH_PRESSED The alt graph key's state changed.
 *
 */

struct Modifier
{
/**@#-*/
    unsigned :27;   /*unused bits....*/
/**@#+*/
    unsigned KEY_SHIFT_PRESSED          :1; 
    unsigned KEY_CONTROL_PRESSED        :1;
    unsigned META_CHANGE                :1; /*Meta key pressed OR Mouse's 3rd button 
                                              changed its state*/
    unsigned ALT_CHANGE                 :1; /*Alt key down OR Mosue's 2nd button 
                                              changed its state*/
    unsigned KEY_ALT_GRAPH_PRESSED      :1; /*State changed of alt graph key*/
};
typedef struct Modifier Modifier;

/**
 * Reads the descriptor from the socket.
 *
 * @param *linkSocket The link socket.
 * @param *descriptor The descriptor to read.
 */

int ReadDescriptor(LinkSocket *linkSocket, Descriptor *descriptor);

/**
 * Reads the modifier from the socket.
 *
 * @param *linkSocket The link socket.
 * @param *modifier The modifier to read.
 */

int ReadModifier(LinkSocket *linkSocket, Modifier *modifier);

/*---------------------------------------------------------*/
#endif

