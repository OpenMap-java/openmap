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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkArgs.h,v $
 * $RCSfile: LinkArgs.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKARGS_H
#define LINKARGS_H

#include "LinkSocket.h"
#include "Request.h"

/*
  This file defines structures and methods that are used in the
  LinkArgs.  LinkArgs are used in all requests and responses, and in
  the graphic structures sent to the client.
 */

/**
 * The link string structure.
 *
 * @param numberOfChars The number of characters in the string.
 * @param *text The string.
 */

struct LinkString{
    int numberOfChars; 
    char *text;     
};
typedef struct LinkString LinkString;

/**
 * The link arguments.
 * 
 * @param numberOfArgs The number of arguments.
 * @param *args The arguments.
 */

struct LinkArgs {
    int numberOfArgs;
    LinkString *args;
};
typedef struct LinkArgs LinkArgs;

/**
 * Reads the link string off the link socket.
 *
 * @param *linkSocket The link socket.
 * @param *string The string to read.
 */

int ReadLinkString(LinkSocket *linkSocket, LinkString *string);

/**
 * Writes the link string to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param *string The string to write.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkString(LinkSocket *linkSocket, LinkString *string);

/**
 * Writes the link string to a buffer.
 *
 * @param *toBuffer Buffer to write to.
 * @param *string Link string to write.
 */

int BufferedWriteLinkString(char *toBuffer, LinkString *string);

/**
 * Frees the memory used by a link string.
 *
 * @param *string The link string to free memory from.
 */

void FreeLinkString(LinkString *string);

/**
 * Reads link arguments from the socket.
 *
 * @param *linkSocket The link socket.
 * @param * The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int ReadLinkArgs(LinkSocket *linkSocket, LinkArgs *);

/**
 * Writes link arguments to the socket.
 *
 * @param *linkSocket The link socket.
 * @param * The link arguments.
 *
 */

int WriteLinkArgs(LinkSocket *linkSocket, LinkArgs *);

/**
 * Writes buffered link arguments. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkArgs().
 *
 * @param *toBuffer The buffer to write to.
 * @param * The link arguments.
 *
 */

int BufferedWriteLinkArgs(char *toBuffer, LinkArgs *);

/**
 * Writes string link arguments to the socket.
 *
 * @param *linkSocket The link socket.
 * @param * The string.
 * @returns -1 if there was a memory allocation error
 */

int WriteLinkArgString(LinkSocket *linkSocket, LinkString *string);

/**
 * Creates the link arguments.
 *
 * @returns NULL if there was a memory allocation error.
 */

LinkArgs* CreateLinkArgs();

/**
 * Gets the value for a given key.  The argument list given is assumed
 * to be a list of key-value pairs in an array of LinkArgs, alternating
 * key, value, etc.  If the key matches one of the keys in the list, a 
 * pointer to the value for the key is returned.
 *
 * @param *args The link arguments.
 * @param *key The key to get a value for.
 * @returns The value for the key, if found. Otherwise, NULL.
 */

char* GetValueForKey(LinkArgs *args, char* key);

/**
 * Frees the memory from the link arguments.  Does not free the
 * pointer, in case the pointer to the strcuture was not allocated
 * using malloc.  So you'll still have to free linkArgs structure, but
 * not the contents.
 *
 * @param *linkArgs The link arguments.
 */

void FreeLinkArgs(LinkArgs *linkArgs);

/**
 * Sets a key/value pair in the link arguments, for where the value 
 * is a char.
 *
 * @param *linkArgs The link arguments.
 * @param *key The key.
 * @param *value The char value to set.
 * @param is_unicode Set to 1 if the value you're passing is already in Unicode, and does not need to be translated.
 * @param num_unicode_chars If Unicode is being passed, how many characters does the string take?
 * @returns -1 if there was a memory allocation error.
 */

int SetKeyValuePairInLinkArgs(LinkArgs *linkArgs, char *key, char *value, int is_unicode, int num_unicode_chars);

/**
 * Sets a key/value pair in the link arguments, for where the value is
 * an int.
 *
 * @param *linkArgs The link arguments.
 * @param *key The key.
 * @param value The integer value to set.
 * @returns -1 if there was a memory allocation error. 
 */

int SetKeyIntegerPairInLinkArgs(LinkArgs *linkArgs, char *key, int value);

/**
 * Sets a key/value pair in the link arguments, for where the value is
 * a double.
 *
 * @param *linkArgs The link arguments.
 * @param *key The key.
 * @param value The double value to set.
 * @returns -1 if there was a memory allocation error.
 *
 */
int SetKeyDoublePairInLinkArgs(LinkArgs *linkArgs, char *key, double value);

/**
 * Removes a key/value pair from the link arguments.
 *
 * @param *linkArgs The link arguments.
 * @param *key The key of the key/value pair to remove.
 * @returns -1 if there was a memory allocation error.
 */

int RemoveKeyFromLinkArgs(LinkArgs* linkArgs, char *key);

/**
 * Returns the number of bytes used by the link arguments.
 *
 * @param *linkArgs.
 * @return The number of bytes used by the link arguments.
 */

int LinkSizeOfLinkArgs(LinkArgs *linkArgs);


#endif

