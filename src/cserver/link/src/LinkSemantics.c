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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkSemantics.c,v $
 * $RCSfile: LinkSemantics.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include "LinkSemantics.h"
#include "LinkSocket.h"
#include "GlobalConstants.h"

#define DEBUG_ME "LINKSERVER"
#include <toolLib/debugging.h>
DebugVariable(LINKGRAPHICS, "LINKGRAPHICS", 0x02); /* setenv LINKSERVER "LINKGRAPHICS"*/

void InitLinkSemantics(LinkSemantics *linksemantics)
{
    linksemantics->linecolor = COLORBLACK;
    linksemantics->highlightcolor = COLORBLACK;
    linksemantics->fillcolor =(int) 0x00000000;
    linksemantics->linewidth = DEFAULT_LINE_WIDTH;
}

void SetSemanticsLineColor(LinkSemantics *linksemantics, int linecolor)
{
    linksemantics->linecolor = linecolor;
}

void SetSemanticsHighlightColor(LinkSemantics *linksemantics, int highlightcolor)
{
    linksemantics->highlightcolor = highlightcolor;
}

void SetSemanticsFillColorLink(LinkSemantics *linksemantics, int fillColor)
{
    linksemantics->fillcolor = fillColor;
}

void SetSemanticsLineWidth(LinkSemantics *linksemantics, int linewidth)
{
    linksemantics->linewidth = linewidth;
}

int WriteLinkSemantics(LinkSocket *linkSocket, LinkSemantics *semantics)
{
  int check = OK;
  check = check || WriteInteger(linkSocket, semantics->linecolor);
  check = check || WriteInteger(linkSocket, semantics->highlightcolor);
  check = check || WriteInteger(linkSocket, semantics->fillcolor);
  check = check || WriteInteger(linkSocket, semantics->linewidth);
    
  if (Debug(LINKGRAPHICS)) printf("LinkSemantics: Writing:\nline color = %lx\n\
hightlight color = %lx\nfillcolor = %lx\nlinewidth = %d\n",
                                  semantics->linecolor, semantics->highlightcolor,
                                  semantics->fillcolor, semantics->linewidth);
    return check;
}

int BufferedWriteLinkSemantics(char *toBuffer, LinkSemantics *semantics)
{
    int byteswritten = 0;
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten], semantics->linecolor);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten], semantics->highlightcolor);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten], semantics->fillcolor);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten], semantics->linewidth);
    return byteswritten;
}
