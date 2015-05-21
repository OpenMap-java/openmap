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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkServer.c,v $
 * $RCSfile: LinkServer.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "Link.h"
#include "LinkSocket.h"
#include "MapRequest.h"
#include "Request.h"
#include "LinkLine.h"
#include "LinkRaster.h"
#include "GlobalConstants.h"
#include "LinkImage.h"
#include "LinkText.h"
#include "LinkRectangle.h"
#include "LinkPoint.h"
#include "LinkPoly.h"
#include "LinkCircle.h"
#include "LinkBitmap.h"
#include "LinkArgs.h"
#include "ActionRequest.h"
#include "Response.h"
#include <sys/wait.h>

/*
  Test Server
*/

/*for debugging*/
#define DEBUG_ME "LINKSERVER"
#include <toolLib/debugging.h>
DebugVariable(LINK, "LINK", 0x01); /* setenv LINKSERVER "LINK"*/

/* You should set this to 1 if you want the client to go
   'interactive'. This server doesn't maintain a list of objects that
   it sends across to the client, so interactivity is overrated in
   this example.  You could receved the action events, though, and
   change graphics, move them, provide information about them, etc. */
#define CLIENT_NOTIFICATION_SENT 0

void HandleClient(Link *link);
void HandleMapRequest(Link* link);
void HandleActionRequest(Link* link);
void SendActionPreferences(Link* link);
void SendTestRaster(Link *link, LinkArgs*);
void SendTestLine(Link *link, LinkArgs*);
void SendTestText(Link *link, LinkArgs*);
void SendTestPoly(Link *link, LinkArgs*);
int MIN_ARGS_REQ = 2;

int main(int argc, char **argv)
{
    Link *link;
    LinkSocket *parentSocket;

    if(argc < MIN_ARGS_REQ)
      {
        printf("usage: filename port_number\n");
        exit(1);
      }
    
    parentSocket = (LinkSocket*) malloc(sizeof(LinkSocket));      
    parentSocket->port = atoi(argv[1]);

    if (Debug(LINK)) printf("LinkServerStarter\n");
    
    if (Debug(LINK)) printf("Creating socket\n");
    InitSocket(parentSocket);
    parentSocket->isBuffered = LINK_FALSE;
    while (1) 
    {
        LinkSocket *childSocket = (LinkSocket*) malloc(sizeof(LinkSocket));
        SocketCopyParentToChild(parentSocket,childSocket);      
        
        if (Debug(LINK)) printf("accepting socket\n");
        accept_socket(parentSocket->mainsd, &(childSocket->sd));
            
        /*if(0 == (forkresult = fork()))*/
        {
            printf("\nChild process\n");
            link = (Link *) malloc(sizeof(Link));
            link->mapRequest = NULL;
            link->actionRequest = NULL;
            link->socket = childSocket;
            
            HandleClient(link);
        }
        /*waitpid(-1,
          NULL,
          WNOHANG);*/
    }
}

void HandleClient(Link* link)
{

    if(CLIENT_NOTIFICATION_SENT)
        SendActionPreferences(link);

    while(1)
    {
        
        if (Debug(LINK)) printf("Reading and parsing\n"); 
        if (ReadAndParseLink(link) == HEADERERROR){
            break;
        }
        if (link->mapRequest != NULL){

            HandleMapRequest(link);
        }
        if(link->actionRequest != NULL)
        {
            printf("Handling Acton Request\n");
            HandleActionRequest(link);
        }
    }
    printf("Child finished processing\nexiting\n");   
    close_socket(link->socket->sd);
    FreeLink(link);
    free(link);
    exit(0);
}

void SendActionPreferences(Link* link)
{
    Descriptor *descriptor;
    int des = 0;
    printf("des before %x\n",des);
    descriptor = (Descriptor *)&des;

    /* Just list the types of events that should be reacted upon. */
    descriptor->MOUSE_CLICK = 1;
    descriptor->MOUSE_PRESSED = 1;
    descriptor->MOUSE_RELEASED = 1;
    descriptor->MOUSE_MOVE = 0;
    descriptor->MOUSE_ENTER = 1;
    descriptor->MOUSE_EXIT = 1;
    descriptor->MOUSE_DRAGGED = 1;
    descriptor->KEY_PRESSED = 1;
    descriptor->KEY_RELEASED = 1;

    /* This just says that the client should note mouse events.  Some
       clients know how to handle things internally. */
    descriptor->CLIENT_NOTIFICATION = 1;

    /* You would set this if you want the server to be notified of
       events. Setting this variable will cause the client to sent
       action events to the server.*/
    /* descriptor->SERVER_NOTIFICATION = 1; */

    printf("des after %x\n",des);

    /* This screws things up, the header for this section is written
       in SendServerInterest. */
/*     WriteActionResponseHeader(link->socket, CreateLinkArgs()); */
    
    SendServerInterest(link->socket, descriptor);
    EndSection(link->socket);
}


void HandleActionRequest(Link* link)
{
    char *msg = "Wow this is working";

    LinkArgs *args = CreateLinkArgs();
    SetKeyValuePairInLinkArgs(args, LPC_INFO, msg, 0, 0);
    
    WriteActionResponseHeader(link->socket, args);

    printf("writing infoline %s\n", msg);

    EndTotal(link->socket);
    free(args);
}

void HandleMapRequest(Link* link)
{
    char *result;
    LinkArgs *la, *args;
    MapRequest *map;

    map = link->mapRequest;
    la = &(map->linkargs);
    result = GetValueForKey(la, "datatype");

    args = CreateLinkArgs();
    
    if (result != NULL){
        printf("Found value for datatype => %s\n", result);
    } else printf("No value found for datatype key.\n");
    
    WriteMapResponseHeader(link->socket, args);

    if (Debug(LINK)) printf("Writing graphics\n"); 
     
    SendTestLine(link,args);
    SendTestPoly(link,args);
    SendTestCircle(link,args);     
    SendTestRectangle(link,args);

    SendTestText(link, args);
    /* SendTestRaster(link, args);*/
     
    EndTotal(link->socket);

    FreeMapRequest(link->mapRequest);
    free(link->mapRequest);
    link->mapRequest = NULL;
    FreeLinkArgs(args); /* Reduce memory leakage. */
    free(args);
}

void SendTestText(Link *link, LinkArgs *args)
{
    char *text = "this is just a test";
    double lat = 20.0, lon = 10.0;

    WriteLinkTextLatLon(link->socket, lat, lon, text, "", JUSTIFY_LEFT, args, 0, 0);
    WriteLinkTextXY(link->socket, (int)lat, (int)lon, text, "", JUSTIFY_LEFT, args, 0, 0);
    WriteLinkTextOffset(link->socket, lat, lon, (int)lat, (int)lon,
                        text,"", JUSTIFY_LEFT, args, 0, 0);
    
/*     BufferedWriteLinkTextLatLon(link->socket, lat, lon, */
/*                              text, "", JUSTIFY_LEFT, args); */
/*     BufferedWriteLinkTextXY(link->socket, (int)lat, (int)lon, */
/*                          text, "", JUSTIFY_LEFT,args); */
/*     BufferedWriteLinkTextOffset(link->socket, lat, lon, (int)lat, (int)lon, */
/*                              text, "", JUSTIFY_LEFT, args); */
}

void SendTestRectangle(Link *link, LinkArgs *args)
{
    double nwlat = 50.0, nwlon = -50.0, selat = -50.0, selon = 50.0;
    int ulx = 128, uly = 137, lrx = 294, lry = 248;

    WriteLinkRectangleLatLon(link->socket,LGREATCIRCLE,nwlat,nwlon,selat,
                             selon,-1,args);
    WriteLinkRectangleXY(link->socket,ulx,uly,lrx,lry,args);
    WriteLinkRectangleOffset(link->socket,nwlat,nwlon,ulx,uly,lrx,lry,args);

/*     BufferedWriteLinkRectangleLatLon(link->socket,LGREATCIRCLE, */
/*                                   nwlat,nwlon,selat,selon,-1,args); */
/*     BufferedWriteLinkRectangleXY(link->socket,ulx,uly,lrx,lry,args); */
/*     BufferedWriteLinkRectangleOffset(link->socket,nwlat,nwlon, */
/*                                   ulx,uly,lrx,lry,args); */
}

void SendTestCircle(Link *link, LinkArgs *args)
{
    double center_lat = 0.0,center_lon = 0.0 ,radius = 10.0;
    int x = 100, y = 100, w = 100, h = 200;

    WriteLinkCircleLatLon(link->socket,center_lat,center_lon,radius,
                          CIRCLE_DECIMAL_DEGREES,-1,args);
    WriteLinkCircleXY(link->socket,x,y,w,h,args);
    WriteLinkCircleOffset(link->socket,center_lat,center_lon,x,y,w,h,args);

/*     BufferedWriteLinkCircleLatLon(link->socket,center_lat,center_lon,radius, */
/*                                CIRCLE_DECIMAL_DEGREES,-1,args); */
/*     BufferedWriteLinkCircleXY(link->socket,x,y,w,h,args); */
/*     BufferedWriteLinkCircleOffset(link->socket,center_lat,center_lon,x,y,w,h,args); */
  
}

void SendTestPoly(Link *link, LinkArgs *args)
{
    int nlatlon = 6;
    double latlon[6], lat[3],lon[3];
    int XY[6],X[3],Y[3];

    lat[0] = latlon[0] = 10.0;
    lon[0] = latlon[1] = 20.0;
    lat[1] = latlon[2] = 50.0;
    lon[1] = latlon[3] = 50.0;
    lat[2] = latlon[4] = 70.0;
    lon[2] = latlon[5] = 150.0;
  
    X[0] = XY[0] = 10;
    Y[0] = XY[1] = 30;
    X[1] = XY[2] = 60;
    Y[1] = XY[3] = 100;
    X[2] = XY[4] = 145;
    Y[2] = XY[5] = 225;

    WriteLinkPolyLatLon(link->socket,LGREATCIRCLE, nlatlon, latlon,
                        DECIMAL_DEGREES,-1,args);
    WriteLinkPolyXY(link->socket,nlatlon, XY ,args);
    WriteLinkPolyOffset(link->socket,10.0,10.0,nlatlon, XY ,
                        COORDMODE_ORIGIN,args);
    
/*     BufferedWriteLinkPolyLatLon(link->socket,LGREATCIRCLE, */
/*                              nlatlon, latlon,DECIMAL_DEGREES,-1,args); */
/*     BufferedWriteLinkPolyXY(link->socket,nlatlon, XY ,args); */
/*     BufferedWriteLinkPolyOffset(link->socket,10.0,10.0,nlatlon, */
/*                              XY ,COORDMODE_ORIGIN,args); */
    
    WriteLinkPolyLatLon2D(link->socket,LGREATCIRCLE, nlatlon/2,
                         lat, lon,DECIMAL_DEGREES,-1,args);
    WriteLinkPolyXY2(link->socket,nlatlon/2, X, Y ,args);
    WriteLinkPolyOffset2(link->socket,10.0,10.0,nlatlon/2,
                         X, Y ,COORDMODE_ORIGIN,args);
    
/*     BufferedWriteLinkPolyLatLon2(link->socket,LGREATCIRCLE, */
/*                               nlatlon/2, lat, lon,DECIMAL_DEGREES,-1,args); */
/*     BufferedWriteLinkPolyXY2(link->socket,nlatlon/2, X, Y ,args); */
/*     BufferedWriteLinkPolyOffset2(link->socket,10.0,10.0,nlatlon/2, */
/*                               X, Y ,COORDMODE_ORIGIN,args); */
}

void SendTestLine(Link *link, LinkArgs *args)
{
    WriteLinkLineXY(link->socket,0,0,100,100, args); 
    WriteLinkLineLatLon(link->socket, 50, -60, 40, -80, 
                                LSTRAIGHT,-1, args); 
    WriteLinkLineOffset(link->socket,30,20,50,75,200,150, args);
}

void SendTestRaster(Link *link, LinkArgs *args)
{
    DirectImage *directimage;
    IndexedImage *indexedimage;
    double lat,lon;
    int w,h;
    int i;
    int colortablesize = 256, trans = 100;
    lat = 40.0;
    lon = 70.0;
    w = 40; h = 100;

    
    /* allocate memory and create a raster*/
    directimage = (DirectImage*) malloc(sizeof(DirectImage));
    directimage->numberOfPixels = w*h;
    directimage->image = (int *)malloc(sizeof(int)*w*h);
    for(i=0;i<w*h;i++){
        directimage->image[i] = rand();
    }

    indexedimage = (IndexedImage *)malloc(sizeof(IndexedImage));
    indexedimage->numberOfPixels = w*h;
    indexedimage->image = (char *)malloc(w*h);

    for(i=0;i<w*h;i++){
        indexedimage->image[i] = (char)(rand() % 256);
    }
    indexedimage->colorTableSize = colortablesize;
    indexedimage->colorTable = (int *)malloc(sizeof(int)*colortablesize);
    
    for(i=0;i<colortablesize;i++){
        indexedimage->colorTable[i] = rand();
    }

    WriteLinkRasterDirectLatLon(link->socket, lat, lon, w, h,
                                directimage, args);
    WriteLinkRasterDirectXY(link->socket, (int)lat, (int)lon, w, h,
                            directimage, args);
    WriteLinkRasterDirectOffset(link->socket, lat, lon, lat, lon, w, h,
                                directimage, args);
    
    WriteLinkRasterIndexedLatLon(link->socket, lat, lon, w, h, trans,
                                 indexedimage, args);
    WriteLinkRasterIndexedXY(link->socket,(int)lat, (int)lon, w, h, trans,
                             indexedimage, args);
    WriteLinkRasterIndexedOffset(link->socket, lat, lon,
                                 lat, lon, w, h,trans, indexedimage, args);
    
/*     WriteLinkBitmapLatLon(link->socket,lat,lon,w,h, */
/*                        indexedimage->numberOfPixels, */
/*                        indexedimage->image, */
/*                        args); */

/*     WriteLinkBitmapXY(link->socket,(int)lat,(int)lon,w,h, */
/*                    indexedimage->numberOfPixels, */
/*                    indexedimage->image, */
/*                    args); */

/*     WriteLinkBitmapOffset(link->socket,lat,lon,(int)lat,(int)lon,w,h, */
/*                        indexedimage->numberOfPixels, */
/*                        indexedimage->image, */
/*                        args); */

    free(indexedimage->image);
    free(indexedimage->colorTable);
    free(indexedimage);
    free(directimage->image);
    free(directimage);
}
