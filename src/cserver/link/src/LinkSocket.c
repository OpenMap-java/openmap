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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkSocket.c,v $
 * $RCSfile: LinkSocket.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkSocket.h"
#include "GlobalConstants.h"

#define DEBUG_ME "LINKSERVER"
#include <toolLib/debugging.h>

DebugVariable(LINK,"LINK",0x01);
DebugVariable(LINKSOCKET,"LINKSOCKET",0x04);
/*
  flushes the socket's writebuffer.
  returns OK if it successfully wrote the entire buffer to socket,
  NOK otherwise.
*/
int Socketflush(LinkSocket *socket){
    
    if(socket->currentbuffer > 0){
        int nbyteswritten;
        int nbytestowrite = socket->currentbuffer;
        if (Debug(LINKSOCKET))
            printf("flushing socket writing %d bytes \n", nbytestowrite);
        
        nbyteswritten = socket_send(socket->sd, socket->writebuffer, nbytestowrite);
        
        if(nbytestowrite == nbyteswritten){
            socket->currentbuffer = 0;
        } else {
            socket->error = LINK_TRUE;
            return NOK;
        }
    }
    socket->error = LINK_FALSE;
    return OK;
}

/*
  copies all from parent to child except sd, since it is new for every child.
 */
int SocketCopyParentToChild(LinkSocket *parent, LinkSocket *child){
    
    child->host = parent->host;
    child->mainsd = parent->mainsd;
    child->port = parent->port;
    child->error = LINK_FALSE;
    child->maxbuffer = MAX_SOCKET_BUFFER_SIZE;
    child->writebuffer = (char *)malloc(child->maxbuffer);
    if (child->writebuffer == NULL)
      return -1; /* Memory allocation error. */
    child->currentbuffer = 0;
    child->isBuffered = parent->isBuffered;
    return OK;
}

int InitSocket(LinkSocket *socket){

    set_socket_domain(AF_INET);
    socket->host = NULL; /*Keep it Null for the time being*/
    if(-1 == open_socket_port(&socket->mainsd, socket->port, NULL, SERVER))
    {
        socket->error = LINK_TRUE;
        return NOK;
    }    
    socket->error = LINK_FALSE;
    socket->currentbuffer = 0;
    socket->maxbuffer = MAX_SOCKET_BUFFER_SIZE;
    socket->isBuffered = LINK_TRUE;
    socket->writebuffer = (char *)malloc(socket->maxbuffer);
    if (socket->writebuffer == NULL)
      return -1; /* Memory allocation error */
    return OK;
}

void FreeSocket(LinkSocket *socket){
    if(NULL != socket->host)
        free(socket->host);
    socket->host = NULL;

    if(NULL != socket->writebuffer)
        free(socket->writebuffer);
    socket->writebuffer = NULL;
}

/*Checks if the error flag is set return OK or NOK*/
int CheckSocket(LinkSocket* socket){
    if (socket->error){
        if (Debug(LINK)) printf("LinkSocket: problem with link socket...\n");
    }
    return (socket->error);
}

/*
  returns NOK if datasize is more than maxbuffer and flushes the socket.
  returns OK if datasize + socket->currentbuffer is less than maxbuffer 
  Flushes the buffer and returns OK, 
  if socket->currentbuffer + datasize(obviously < maxbuffer) is greater than maxbuffer
*/
/*Used internally by WriteInteger etc*/
static int WriteToBuffer(LinkSocket *socket, int datasize){
    if(datasize > socket->maxbuffer){
        Socketflush(socket);
        return NOK;
    }

    if((datasize + socket->currentbuffer) < socket->maxbuffer){
        return OK;
    } else {
        return Socketflush(socket);
    }
}

void IntegerToBytes(char *toBuffer, int ivalue){
    toBuffer[0] = (toBuffer[0] & 0x00) | (ivalue >> 24); /*MSB goes first*/
    toBuffer[1] = (toBuffer[1] & 0x00) | (ivalue >> 16); 
    toBuffer[2] = (toBuffer[2] & 0x00) | (ivalue >> 8);
    toBuffer[3] = (toBuffer[3] & 0x00) | (ivalue);       /*LSB goes last*/
}

void BytesToInteger(int *ptrIntegerBuffer, char *fromBuffer)
{
    *ptrIntegerBuffer = 0;
    *ptrIntegerBuffer = ((fromBuffer[0] & 0xFF) << 24) | ((fromBuffer[1] & 0xFF) << 16 ) |
        ((fromBuffer[2] & 0xFF) << 8) | ((fromBuffer[3] & 0xFF));    
}
/*
  Read an integer value in to provided buffer
*/
int ReadInteger(LinkSocket *socket, int *ptrIntegerBuffer){
    
    char intbuff[N_BYTES_PER_INTEGER]; /*Read integer in buffer*/
    int bytesRead;

    /*socket_receive takes void* as 2nd paramater and its ok to pass a char*  */
    bytesRead = socket_receive(socket->sd, intbuff, N_BYTES_PER_INTEGER); 
    
    if(N_BYTES_PER_INTEGER == bytesRead)
    {   
        /* All right, we got it*/
        BytesToInteger(ptrIntegerBuffer,intbuff);
    
        socket->error = LINK_FALSE;  
        return OK;
    }
    /* Oops..some error while receiving*/
    socket->error = LINK_TRUE;
    return NOK;
}

int WriteInteger(LinkSocket *socket, int iValue){
    char intbuff[N_BYTES_PER_INTEGER];
    int nbyteswritten;
    
    if(socket->isBuffered == LINK_TRUE && OK == WriteToBuffer(socket, N_BYTES_PER_INTEGER)){
        nbyteswritten = BufferedWriteInteger(&(socket->writebuffer[socket->currentbuffer]),
                                             iValue);
        socket->currentbuffer += nbyteswritten;
    } else {
        IntegerToBytes(intbuff,iValue);
        nbyteswritten = socket_send(socket->sd, intbuff, N_BYTES_PER_INTEGER);
    }
    
    if(N_BYTES_PER_INTEGER == nbyteswritten){
        socket->error = LINK_FALSE;
        return OK;
    }

    socket->error = LINK_TRUE;
    return NOK;
}

int BufferedWriteInteger(char *toBuffer, int iValue){
    IntegerToBytes(toBuffer, iValue);
    return N_BYTES_PER_INTEGER;
}

int ReadFloat(LinkSocket *socket, double *ptrDoubleBuffer){
    
    char buff[N_BYTES_PER_FLOAT]; /*Read float in this buffer*/
    int bytesRead;

    /*socket_receive takes void* as 2nd paramater and its ok to pass char*  */
    bytesRead = socket_receive(socket->sd, buff, N_BYTES_PER_FLOAT); 
   
    if(N_BYTES_PER_FLOAT == bytesRead)
    {   
        /* All right, we got it*/
        
        float *f;
        int i = 0;
        /*do it on integer first. */
        BytesToInteger(&i,buff);
        
        /*type cast just the pointer*/
        f = (float *)&i;
        
        /*type cast the float value to double*/
        *ptrDoubleBuffer = (double)*f;
        
        socket->error = LINK_FALSE;
        return OK;
    }
    /* Oops..some error while receiving*/
    socket->error = LINK_TRUE;
    return NOK;    
}


int WriteFloat(LinkSocket *socket, float fvalue){
    
    char buff[N_BYTES_PER_FLOAT];
    int nbyteswritten;
    int *iptr;
    iptr = (int *)&fvalue;
    
    if(socket->isBuffered == LINK_TRUE && OK == WriteToBuffer(socket,N_BYTES_PER_FLOAT)){
        nbyteswritten = BufferedWriteFloat(&(socket->writebuffer[socket->currentbuffer]), fvalue);
        socket->currentbuffer += nbyteswritten;
    } else {
        IntegerToBytes(buff, *iptr);
        nbyteswritten = socket_send(socket->sd, buff, N_BYTES_PER_FLOAT);
    }
    
    if(N_BYTES_PER_FLOAT == nbyteswritten){
        socket->error = LINK_FALSE;
        return OK;
    }
    
    socket->error = LINK_TRUE;
    return NOK;
}

int BufferedWriteFloat(char *toBuffer, float fvalue){
    int *iptr;
    iptr = (int *)&fvalue;
    IntegerToBytes(toBuffer, *iptr);
    return N_BYTES_PER_FLOAT;
}

int ReadChars(LinkSocket *socket, char buffer[], int nchartoread){
    
    int nbytesread;
    
    nbytesread = socket_receive(socket->sd, buffer, nchartoread); 
    
    if(nchartoread == nbytesread)
      {
        buffer[nchartoread] = '\0';
        socket->error = LINK_FALSE;
        return OK;
      }
    /*system error. */
    socket->error = LINK_TRUE;
    return NOK;
}

int ReadChar(LinkSocket *socket, char *c){
    return ReadChars(socket, c, 1);
}

int WriteChars(LinkSocket *socket, char buffer[], int nchartowrite){    
    int nbyteswritten;
    
    if(Debug(LINKSOCKET))
    {
        printf("LinkSocket.c WriteChars-- %s %d\n", buffer, nchartowrite);
    }

    if (nchartowrite == 0){
        socket->error = LINK_FALSE;
        return OK;
    }
        
    if((LINK_TRUE == socket->isBuffered) && (OK == WriteToBuffer(socket, nchartowrite)) )
    {
        nbyteswritten = BufferedWriteChars(&(socket->writebuffer[socket->currentbuffer]),
                                           buffer, nchartowrite);
        socket->currentbuffer += nbyteswritten;
    }
    else
        nbyteswritten = socket_send(socket->sd, buffer, nchartowrite); 
    
    if(nchartowrite == nbyteswritten)
    {   
        socket->error = LINK_FALSE;
        return OK;
    }
    /*system error */
    socket->error = LINK_TRUE;
    return NOK;
}

int WriteChar(LinkSocket *socket, char *c){
    return WriteChars(socket, c, 1);
}

int BufferedWriteChars(char *toBuffer, char *fromBuffer, int nchartowrite){
    
    memcpy(toBuffer,fromBuffer,nchartowrite);
    /*
      int i = 0;
      while(i < nchartowrite)
      {
      toBuffer[i] = fromBuffer[i];
      i++;
      }
    */
    return nchartowrite;
}

/*
 * bmackiew: This function originally translated the Unicode characters
 * to ASCII for use by the calling function.  Since we deal with stored
 * strings in Unicode only, there's no need to do the translation.
 */

int ReadUnicodeChars(LinkSocket *socket, char *buffer, int nchartoread){
    int ncharread = 0;
    int i;
    char *inbuffer;
    
    /*
     * ### bmackiew: Make sure we read in the proper number of characters!
     */

    /* bmackiew: The numbers of characters to read was originally multiplied
     * by two since strings were stored in ASCII.  We've stored them as 
     * Unicode already, so we know how many characters were needed for
     * storage and readback.
     */

#if 0
    nchartoread = nchartoread*2;
#endif

    inbuffer = (char *)malloc(sizeof(char) * nchartoread);
    if (inbuffer == NULL)
      return -1; /* Memory allocation error */

    ncharread = socket_receive(socket->sd, inbuffer, nchartoread);
    if(nchartoread == ncharread)
    {
      /*
       * This was originally done by a strcpy(), but that fails with
       * English Unicode characters.
       */

      for (i = 0; i < nchartoread; i++) {
        buffer[i] = inbuffer[i];
      }

      buffer[ncharread] = '\0';
      free(inbuffer);
      socket->error = LINK_FALSE;
      return OK;
    }        
    /*error condition */
    free(inbuffer);
    socket->error = LINK_TRUE;
    return NOK;
}

/*
 * bmackiew: Modified to not use ASCIIToUnicode(), as by the time
 * characters are sent here, they are already in Unicode.
 */

int WriteUnicodeChars(LinkSocket *socket, char buffer[], int nchartowrite){
    int ncharwritten = 0;
    /*Internal buffer for holding unicode characters*/   
    char *inbuffer;
 
    if (nchartowrite == 0){
        socket->error = LINK_FALSE;
        return OK;
    }
    
    inbuffer = (char *)malloc(nchartowrite);
    
    if (inbuffer == NULL)
      return -1; /* Memory allocation error */
             
    if((LINK_TRUE == socket->isBuffered) && (OK == WriteToBuffer(socket, nchartowrite)) ) {
      ncharwritten = BufferedWriteUnicodeChars(&(socket->writebuffer[socket->currentbuffer]),
                                               buffer, nchartowrite);   
      socket->currentbuffer += ncharwritten;
      nchartowrite = N_CHARS_PER_UNICODE_CHAR * nchartowrite;
    } 
    else {
      ncharwritten = socket_send(socket->sd, buffer, nchartowrite);
    }

    if(nchartowrite == ncharwritten){
        /*error condition */            
        socket->error = LINK_FALSE;
        free(inbuffer);
        return OK;
    }
       
    socket->error = LINK_TRUE;
    free(inbuffer);

    return NOK;
}

int BufferedWriteUnicodeChars(char *toBuffer,
                              char *fromBuffer,
                              int nchartowrite){
    
    return ASCIIToUnicode(fromBuffer,toBuffer, nchartowrite);
}


/*
 * Converts Unicode characters to _English_ ASCII characters by
 * stripping them of their first byte.  Note that this is not a
 * comprehensive solution for other language stored in ASCII.
 *
 */
char *EnglishUnicodeToASCII(char unicodechar[], int len) {
    int i, length;
    char *cchar;
    
    cchar = (char *)malloc(length/2);
    for( i = 0; i< length/2;i++){
        cchar[i] = unicodechar[i*2 + 1];      
    }
    return cchar;
}

/*
 * Converts ASCII characters to Unicode Chars by prefixing them with 0x00;
 */

char *ASCIIToEnglishUnicode(char cchar[]) {
  
  int i, length;
  char *unicodechar;

  length = strlen(cchar);
  unicodechar = (char *)malloc(length * 2);

  if(Debug(LINKSOCKET))
    printf("LinkSocket.c::ASCIITOUnicode \n");
  for(i=0;i<length;i++)
    {
      unicodechar[i*2]=0x0;
      unicodechar[i*2 + 1] = cchar[i];
      if(Debug(LINKSOCKET))
        {
          printf("Unicode char %d = %c %0x\n",i*2,unicodechar[i*2],
                 unicodechar[i*2]);
          printf("Unicode char %d = %c %0x\n",(i*2 + 1),unicodechar[i*2+1],
                 unicodechar[i*2+1]);
        }
    }
  return unicodechar;
}

/*
 * unicodecmp()
 *
 * This was written to supplement the strcmp() standard C function.
 * Instead of comparing two strings for to see if there is a match,
 * it is designed to take two strings which may be Unicode strings,
 * and compares character by character.  
 *
 * This function was written to support OpenMap's C-side link server
 * code.  The protocol requires that Unicode be sent across the link 
 * so the Java-side client can easily parse the data being sent.
 * However, our server implementation takes in ASCII strings and
 * then converts them to Unicode.  For the time being, we make the
 * assumption that we are passed English ASCII only, which may be
 * converted to Unicode by prepending the 0x0 byte to each ASCII
 * character.
 *
 * Unfortunately, C's standard strcmp() function will fail when
 * comparing two of our Unicode'd strings, as the first character
 * read in will be the \0 character, and strcmp() will terminate
 * immediately.  This is a quick fix to get around that problem.
 *
 */

int unicodecmp(const char *s1, const char *s2, int length) {
  int i;

  for (i = 0; i < length; i++) {
    if (s1[i] != s2[i]) {
      return 1;
    }
  }
  return 0;
}

/*
 * The functions listed below are deprecated, and are included only until
 * the redundant buffering code has been eliminated from the link server.
 *
 */

/*
  Converts Unicode(ASCII in 2 bytes) Characters to ASCII Characters 
  by stripping them of their 1st byte
*/
int UnicodeToASCII(char unicodechar[], char cchar[], int length){
    int i;
    for( i = 0; i< length/2;i++){
        cchar[i] = unicodechar[i*2 + 1];      
    }
    return length/2;
}

/*
  Converts ASCII Charecters to Unicode Chars by prefixing them with 0x00;
*/
int ASCIIToUnicode(const char cchar[], char unicodechar[], int length ){
    int i;
    if(Debug(LINKSOCKET))
        printf("LinkSocket.c::ASCIITOUnicode \n");
    for(i=0;i<length;i++)
    {
        unicodechar[i*2]=0x0;
        unicodechar[i*2 + 1] = cchar[i];
        if(Debug(LINKSOCKET))
        {
            printf("Unicode char %d = %c %0x\n",i*2,unicodechar[i*2],unicodechar[i*2]);
            printf("Unicode char %d = %c %0x\n",(i*2 + 1),unicodechar[i*2+1],unicodechar[i*2+1]);
        }
    }
    return length*2;
}
