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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/ActionRequest.h,v $
 * $RCSfile: ActionRequest.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef ACTIONREQUEST_H
#define ACTIONREQUEST_H

#include "Request.h"
#include "LinkArgs.h"
#include "LinkSocket.h"

struct LinkString;

/*
  This Object provides structures and functions for reading 
  Action Objects from Link.
*/

/**
 * The MouseEvent.
 *
 * @param x Horizontal location of the event on the map, measured from the top left corner, in pixels.
 * @param y Vertical location of the event on the map, measured from the top left corner, in pixels.
 * @param clickcount The number of times the mouse is clicked.
 * @param modifier Modifiers associated with MouseEvents.
 * @param latitude The latitude of the MouseEvent, in decimal degrees.
 * @param longitude The longitude of the MouseEvent, in decimal degrees.
 * @param linkargs The link arguments.
 */

struct MouseEvent
{
    int x;  /*Horizontal pixel location of event on map from top left corner*/
    int y;  /*Vertical pixel location of event on map from top left corner*/
    int clickcount; /*number of times mouse is clicked*/
    Modifier modifier; /*Modifiers associated with Mouse events*/ 
    double latitude;  /*in Decimal Degrees*/
    double longitude; /*in Decimal Degrees*/
};
typedef struct MouseEvent MouseEvent; 


/**
 * The KeyboardEvent.
 * 
 * @param keypressed The key pressed.
 * @param modifier Additional keys pressed (ctrl, meta, etc.)
 */

struct KeyboardEvent
{
    char keypressed; 
    Modifier modifier; /*additional keys pressed Control, Meta etc..*/
};
typedef struct KeyboardEvent KeyboardEvent;

/**
 * The ActionRequest
 *
 * @param version The version of the incoming object.
 * @param descriptor The descriptor.
 * @param *mouseEvent The MouseEvent.
 * @param *keyboardEvent The KeyboardEvent.
 * @param linkargs A list of key value pairs for attribute passing.
 */

struct ActionRequest
{
    double version; /*version of the incoming Object */
    Descriptor descriptor;  /*An integer*/
    MouseEvent *mouseEvent;
    KeyboardEvent *keyboardEvent; 
    LinkArgs linkargs; /*A list of key value pairs for attribute passing*/
};
typedef struct ActionRequest ActionRequest; 

struct Link;

/**
 * Reads an action event from the link socket.
 *
 * @param *link The link socket.
 * @returns OK if all read operations return OK, NOK if it failed.
 * @returns -1 if there was a memory allocation error.
 */

int ReadActionRequest(struct Link *link);

/**
 * Frees the memory from an action request.
 *
 * @param *actionrequest The action request to free memory from.
 */

void FreeActionRequest(ActionRequest *actionrequest);

/**
 * Reads mouse event-related information from the socket.
 *
 * @param *linkSocket The link socket.
 * @param *mouseEvent The mouse event.
 * @returns OK if successful.
 */

int ReadMouseEvent(LinkSocket *linkSocket, MouseEvent *mouseEvent);

/**
 * Reads the keyboard event-related information from the socket.
 *
 * @param *linkSocket The link socket.
 * @param *keyboardEvent The keyboard event.
 * @returns OK if successful, -1 if there was a memory allocation error.
y */

int ReadKeyboardEvent(LinkSocket *linkSocket, KeyboardEvent *keyboardEvent);

/**
 * Sends a message to the client; the server is interested in receiving
 * events described in the descriptor.
 *
 * @param *linkSocket The link socket.
 * @param *descriptor The descriptor to receive events about.
 */
int SendServerInterest(LinkSocket *linkSocket, Descriptor *descriptor);

#endif 













