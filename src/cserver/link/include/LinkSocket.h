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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkSocket.h,v $
 * $RCSfile: LinkSocket.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKSOCKET_H
#define LINKSOCKET_H

#include "toolLib/sockets.h"
#include <limits.h>

/**
 * The link socket.
 *
 * @param *host The host.
 * @param mainsd The main socket descriptor required to accept connections.
 * @param sd The socket descriptor returned when the connection is accepted.
 * @param port The port on which the socket will listen.
 * @param error Any errors.
 * @param *writebuffer
 * @param maxbuffer The maximum size of the buffer.
 * @param currentbuffer The pointer to the buffer, from where data needs to be sent.
 * @param isBuffered Boolean, indicating if the socket is buffered or not.  By default, buffering is on.  Write functions will attempt to write the entire data into the buffer.  If it's not successful, write functions will flush the buffer first and write data directly to the socket.  If successful, the data will remain in the buffer until the flush is called.  This should be set to LINK_TRUE if unbuffered write functions are used.  Set it to LINK_FALSE if buffered writes are used.  WARNING: The buffered versions are deprecated, and will be removed in a future release.
 */

struct LinkSocket{
    char *host;
    int mainsd; /*main socket descriptor required to accept connections*/
    int sd;  /*socket descriptor returned, when connection is accepted*/
    int port;  /*number on which socket will listen to */
    int error;  /*error if any*/
    /*
      Buffering within the socket.    
    */
    char *writebuffer;
    int maxbuffer; /*Maximum size of the buffer*/
    int currentbuffer; /*pointer to the buffer, from where data needs to be sent.*/
    int isBuffered; /*Boolean, indicating if socket is buffered or not*/
};
typedef struct LinkSocket LinkSocket;

/*
  Note: By Default Buffering is on.
  Write functions  will attempt to write entire data into the buffer.
  ..if not succesful, write functions would flush the buffer first and
  write data directly to the socket.
  ..if successful, data would remain untill flush is called.    
*/

/**
 * Initializes the internet domain socket as a server.
 *
 * @param *socket The socket to initialize.
 * @returns -1 if there was a memory allocation error.
 */

int InitSocket(LinkSocket *socket);

/**
 * Check to see if the error flag is set.
 *
 * @param *socket The link socket.
 */

int CheckSocket(LinkSocket* socket);


/**
 * Frees the link socket.
 *
 * @param *socket The link socket.
 */

void FreeSocket(LinkSocket *socket);

/**
 * Functions for interconversion between integer and char so that they
 * can be sent and read over the socket.
 *
 * @param *toBuffer
 * @param ivalue
 */

void IntegerToBytes(char *toBuffer, int ivalue);

/**
 * Functions for interconversion between integer and char so that they
 * can be sent and read over the socket.
 *
 * @param *ptrInteger
 * @param *fromBuffer
 */

void BytesToInteger(int *ptrInteger, char *fromBuffer);

/*
  Note:- User should allocate the required memory and pass pointers.          
*/

/**
 * Reads an integer off the socket in a user-provided buffer.
 *
 * @param *socket The link socket.
 * @param *ptrIntegerBuffer The integer buffer to read into.
 * @returns OK if successful.
 */

int ReadInteger(LinkSocket *socket, int *ptrIntegerBuffer);

/**
 * Writes an integer to the socket.
 *
 * @param *socket The link socket.
 * @param iValue The integer to write to the socket.
 * @returns OK if successful, NOK if failure.
 */

int WriteInteger(LinkSocket *socket, int iValue);

/**
 * Writes an integer to a buffer. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteInteger().
 *
 * @param *tobuffer Buffer to write to.
 * @param iValue Integer to write to the buffer.
 */

int BufferedWriteInteger(char *tobuffer,int iValue);

/**
 * Reads Unicode characters from the socket and stores them in a buffer
 * as ASCII characters.
 *
 * Returns OK and sets the socket's error flag to LINK_FALSE if it 
 * successfully wrote nchartoread.  Otherwise, it returns NOK and
 * sets the socket's error flag to LINK_TRUE.  This function blocks 
 * until it reads nchartoread characters.
 * @param *socket The link socket.
 * @param *ptrBuffer Buffer to store the characters in.
 * @param nchartoread Number of characters to read.
 * @returns -1 if there was a memory allocation error.
 */

int ReadUnicodeChars(LinkSocket *socket, char *ptrBuffer, int nchartoread);

/**
 * Writes ASCII characters in buffer as Unicode characters to the socket.
 * 
 * Returns OK and sets the socket's error flag to LINK_FALSE if it successfully
 * wrote nchartowrite characters.  Otherwise, it returns NOK and sets the
 * socket's error flag to LINK_TRUE.  This function blocks util it writes
 * nchartowrite characters.
 * @param *socket The link socket.
 * @param *ptrBuffer The buffer.
 * @param nchartowrite The number of characters to write.
 * @returns -1 if there was a memory allocation error.
 */

int WriteUnicodeChars(LinkSocket *socket, char *ptrBuffer, int nchartowrite);

/**
 * Writes Unicode characters to a buffer instead of the socket.<b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteUnicodeChars().
 *
 * @param *toBuffer Buffer to write to.
 * @param *fromBuffer Buffer to read from.
 * @param nchartowrite Number of characters to write.
 */

int BufferedWriteUnicodeChars(char *toBuffer, char *fromBuffer, int nchartowrite);

/**
 * Reads nchartoread ASCII characters from the socket into a buffer.
 * 
 * Returns OK and sets the socket's error flag to LINK_FALSE if it successfully
 * read nchartoread characters.  Otherwise, it returns NOK and sets the 
 * socket's error flag to LINK_TRUE.  This function blocks until it reads
 * nchartoread characters.
 * @param *socket The link socket.
 * @param *ptrBuffer Buffer to read into.
 * @param nchartoread Number of characters o read.
 */

int ReadChars(LinkSocket *socket, char *ptrBuffer, int nchartoread);

/**
 * Writes nchartowrite characters from a buffer to the socket.
 *
 * Returns OK and sets the socket's error flag to LINK_FALSE if it
 * successfully wrote nchartowrite characters.  Otherwise, it will
 * return NOK and set the socket's error flag to LINK_TRUE.  This
 * function blocks util it writes nchartowrite characters.
 * @param *socket The link socket.
 * @param *fromBuffer The buffer to read from.
 * @param nchartowrite The number of characters to write.
 */

int WriteChars(LinkSocket *socket, char *fromBuffer, int nchartowrite);

/**
 * Writes characters to a buffer instead of the socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteChars().
 *
 * @param *toBuffer Buffer to write to.
 * @param *fromBuffer Buffer to read from.
 * @param nchartowrite Number of characters to write.
 */

int BufferedWriteChars(char *toBuffer, char *fromBuffer, int nchartowrite);

/**
 * Reads a 4-byte float value from a socket into a user-provided buffer.
 * 
 * The float value read in will be typecasted into a double.
 * @param *socket The link socket.
 * @param *ptrDouble The float value to be read in.
 * @returns OK if successful, NOK if it fails.
 */

int ReadFloat(LinkSocket *socket, double *ptrDouble);

/**
 * Writes a float value to the socket.
 *
 * @param *socket The link socket.
 * @param fvalue The float value to write.
 * @returns OK if successful, NOK if it fails.
 */

int WriteFloat(LinkSocket *socket, float fvalue);

/**
 * Writes a float to a buffer instead of a socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteFloat().
 *
 * @param *toBuffer Buffer to write to.
 * @param fvalue The float value to write.
 */

int BufferedWriteFloat(char *toBuffer, float fvalue);

/**
 * Flushes the socket's write buffer.
 *
 * @param *socket The link socket.
 * @returns OK if it successfully wrote the entire buffer to the socket.  Returns NOK if it fails.
 */
int Socketflush(LinkSocket *socket);

/**
 * Copies all from the parent to the child except sd, since it is new for
 * every child.
 *
 * @param *parent The parent link socket.
 * @param *child The child link socket.
 * @returns -1 if there was a memory allocation error.
 */

int SocketCopyParentToChild(LinkSocket *parent, LinkSocket *child);

/**
 * Translates an English Unicode string to ASCII by stripping the first
 * byte from each two-byte pair per character.  This is NOT a comprehensive
 * solution for translating from Unicode to ASCII.
 *
 * @param unicodechar[] The Unicode string to translate.
 * @param length The length of the Unicode string, in characters
 * @returns The translated ASCII string.
 */

char *EnglishUnicodeToASCII(char unicodechar[], int length);

/**
 * Translates an ASCII string to Unicode by prefixing each ASCII character
 * byte with 0x0; note that this does not work for any other language than
 * English.
 *
 * @param cchar[] The ASCII string to translate.
 * @returns The translated Unicode string.
 */

char *ASCIIToEnglishUnicode(char cchar[]);

#endif

/**
 * Compares two Unicode strings.  Ordinarily, strcmp() would be used to
 * handle the comparison of two strings, but it fails when faced with 
 * the first character of an English ASCII character in Unicode (\0).
 * This is not as full-featured as strcmp().
 *
 * @param *s1 The first string to compare.
 * @param *s2 The second string to compare.
 * @param length The length of the strings.  (You need to know this ahead of time.)
 * @returns 0 if the strings are equal.
 */

int unicodecmp(const char *s1, const char *s2, int length);
