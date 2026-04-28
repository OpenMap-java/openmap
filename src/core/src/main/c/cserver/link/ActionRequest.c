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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/ActionRequest.c,v $
 * $RCSfile: ActionRequest.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "ActionRequest.h"
#include "Link.h"
#include "GlobalConstants.h"

/*for debugging*/
#define DEBUG_ME "LINKSERVER"
#include "toolLib/debugging.h"
DebugVariable(LINK, "LINK", 0x01); /* setenv LINKSERVER "LINK"*/

/*
  @param Link: The Object used for communication with client over socket
*/
int ReadActionRequest(Link *link)
{
    int check = OK; /*OK if all read operations returned OK, NOK otherwise*/
    
    ActionRequest *action = (ActionRequest *)malloc(sizeof(ActionRequest));
    if(action == NULL)
      return -1;  /* Memory allocation error */

    action->mouseEvent = NULL;
    action->keyboardEvent = NULL;
    
    if (Debug(LINK)) printf("ReadActionRequest: reading version\n");
    check = check || ReadFloat(link->socket,&(action->version));
    if (Debug(LINK)) printf("ReadActionRequest: version read %f\n", action->version);
    
    check = check || ReadDescriptor(link->socket, &(action->descriptor));
    if (Debug(LINK)) printf("ReadActionRequest: Descriptor Read %x\n", action->descriptor);
    
    /*check which event occured*/
    if(action->descriptor.MOUSE_CLICK || action->descriptor.MOUSE_PRESSED
       || action->descriptor.MOUSE_RELEASED || action->descriptor.MOUSE_MOVE
       || action->descriptor.MOUSE_ENTER || action->descriptor.MOUSE_EXIT
       || action->descriptor.MOUSE_DRAGGED)
    {
        if (Debug(LINK)) printf("ReadActionRequest: Mouse event\n");
     
        action->mouseEvent = (MouseEvent *)malloc(sizeof(MouseEvent));
        if (NULL == action->mouseEvent)
          return -1; /* Memory allocation error */
        check = check || ReadMouseEvent(link->socket, action->mouseEvent);
    }
    else  /*Either a mouse event will come or keyboard event...but not both*/
    {
        if (Debug(LINK)) printf("ReadActionRequest: Key event\n");

        if(action->descriptor.KEY_PRESSED || action->descriptor.KEY_RELEASED)
        {
            action->keyboardEvent = (KeyboardEvent *)malloc(sizeof(KeyboardEvent));
            if (NULL == action->keyboardEvent)
              return -1; /* Memory allocation error */
            check = check || ReadKeyboardEvent(link->socket, action->keyboardEvent);
            if (check == -1)
              return -1; /* Memory allocation error */
        }
        else
        {
            if (Debug(LINK)) printf("Error: Client broke Gesture contract\n");
        }     
    }

    if (Debug(LINK)) printf("ReadActionRequest: Reading Args.\n");
    check = check || ReadLinkArgs(link->socket, &action->linkargs);
    if (check == -1)
      return -1; /* Memory allocation error */

    if (Debug(LINK)) printf("ReadActionRequest: Done Reading Args.\n");
    link->actionRequest = action;
    return check;
}

void FreeActionRequest(ActionRequest *actionrequest)
{
    
    if(actionrequest->keyboardEvent)
    {
        free(actionrequest->keyboardEvent);
    }
    if(actionrequest->mouseEvent)
    {
        free(actionrequest->mouseEvent);
    }

    FreeLinkArgs(&actionrequest->linkargs);
}

int ReadMouseEvent(LinkSocket *linkSocket, MouseEvent *mouseEvent)
{
    int check = OK;

    check = check || ReadInteger(linkSocket,&(mouseEvent->x));
    check = check || ReadInteger(linkSocket,&(mouseEvent->y));
    check = check || ReadInteger(linkSocket,&(mouseEvent->clickcount));
    check = check || ReadModifier(linkSocket,&(mouseEvent->modifier));
    check = check || ReadFloat(linkSocket,&(mouseEvent->latitude));
    check = check || ReadFloat(linkSocket, &(mouseEvent->longitude));

    return check;
}

int ReadKeyboardEvent(LinkSocket *linkSocket, KeyboardEvent *keyboardEvent)
{
    int check = OK;
    /*only 1 char*/
    check = check || ReadUnicodeChars(linkSocket,&(keyboardEvent->keypressed),1);
    if (check == -1)
      return -1; /* Memory allocation error */
    check = check || ReadModifier(linkSocket, &(keyboardEvent->modifier));
    return check; /* -1 if there was a memory allocation error */
}

int SendServerInterest(LinkSocket *linkSocket, Descriptor *descriptor)
{
    int des;
    /** Write the ActionRequestHeader here... */
    if (Debug(LINK))
        printf("SendServerInterest: writing ACTION Request header %s\n",
               GESTURE_RESPONSE_HEADER);
    WriteChars(linkSocket, ACTION_REQUEST_HEADER, lACTION_REQUEST_HEADER); 
    
    if (Debug(LINK))
        printf("SendServerInterest: writing version %f\n", VERSION);
    WriteFloat(linkSocket, VERSION); /*writing version 0.2*/

    des =  *((int *)descriptor);
    if (Debug(LINK)) printf("SendServerInterest: Sending %x Descriptor\n",des );
    return WriteInteger(linkSocket, des);
}
