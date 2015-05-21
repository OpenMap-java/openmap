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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/Link.c,v $
 * $RCSfile: Link.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdio.h>
#include <stdlib.h>

#include "Link.h"
#include "LinkSocket.h"
#include "MapRequest.h"
#include "GlobalConstants.h"
#include "ActionRequest.h"
#include "Response.h"

/*Private method*/
/*
  @param Link: The Object used for communication with client over socket
  @paran char*: Buffer in which header will be read.
*/
static char ReadHeader(Link *link, char* header)
{
    int count = 0;
    
    ReadChars(link->socket, header, 1);
    if (NOK == CheckSocket(link->socket)){
        return HEADERERROR;
    }
        
    if(*header == END_TOTAL){
        return END_TOTAL;
    }
    if(*header == END_SECTION){
        return END_SECTION;
    }
    if(*header != '<'){
        return HEADERERROR; /*Error*/
    }
        
        
    while(*header++ != '>' && count < MAX_HEADER_LENGTH){
            
        ReadChars(link->socket, header,1);
        if (NOK == CheckSocket(link->socket)){
            return HEADERERROR;
        }
        count++;
    }
    
    *header = '\0';
    return HEADERSUCCESS; /*Success*/
}

/*
  @param Link: The Object used for communication with client over socket
*/
int CreateLink(Link *link)
{ 
  int check;
  
  check = InitSocket(link->socket);
  if (check == OK)
    {
      link->closeLink = LINK_FALSE; /*Link is open*/
      return OK; /*socket succesfully created*/
    }
  if (check == -1)
    return -1; /* Memory allocation error */
  return NOK; 
}

/*test fucntion*/
void printString(char *str)
{
    while(*str!=NULL)
        printf("%c",*str++);
}

/*
  @param Link: The Object used for communication with client over socket
*/
char ReadAndParseLink(Link *link)
{
    char header[MAX_HEADER_LENGTH];
    char check = END_SECTION;
    
    /** Clear out the old map request...*/
    if (link->mapRequest != NULL)
    {
        FreeMapRequest(link->mapRequest);  /* FreeMapRequest-- free memory used
                                              that was allocated while reading
                                              a map request*/
        link->mapRequest = NULL;
    }

    if(link->actionRequest != NULL) 
    {
        FreeActionRequest(link->actionRequest);
        link->actionRequest = NULL;
    }

    while (check != END_TOTAL )
    {      
        check = ReadHeader(link, header);
        if (HEADERSUCCESS == check)
        {
            if(0 == strcmp(header, MAP_REQUEST_HEADER))
            {
              if (ReadMapRequest(link) == -1)
                return MEMORYERROR; /* Memory allocation error */
              /*
                if all is well..continue, checksocket would exit otherwise
              */
              if (NOK == CheckSocket(link->socket)) {
                return HEADERERROR;
              }
            }
            if(0 == strcmp(header, ACTION_REQUEST_HEADER))
            {
                ReadActionRequest(link);
                /*
                  if all is well..continue, checksocket would exit otherwise
                */
                CheckSocket(link->socket);        
            }
         
        }
        else
        {
            return HEADERERROR;
        }
        check = ReadHeader(link, header);
    }
    return HEADERSUCCESS;
}

/*
  @param Link: The Object used for communication with client over socket
*/
void FreeLink(Link *link)
{
    if(link->socket != NULL)
    {
        FreeSocket(link->socket); /*To free memory allocated from heap*/
        free(link->socket); /*to free memory allocated on stack*/
    }
    if(link->mapRequest != NULL)
    {
        FreeMapRequest(link->mapRequest);
        free(link->mapRequest);
    }
    if(link->actionRequest != NULL)
    {
        FreeActionRequest(link->actionRequest);      
    }
}


/** Send a dummy response that will evoke no wrath. */
void SendHuh(Link *link){
    WriteChars(link->socket, HUH_HEADER, lHUH_HEADER); 
    EndTotal(link->socket);
}
