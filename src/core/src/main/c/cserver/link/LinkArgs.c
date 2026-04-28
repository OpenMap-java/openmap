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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkArgs.c,v $
 * $RCSfile: LinkArgs.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkArgs.h"
#include "Request.h"
#include "GlobalConstants.h"

#define DEFAULT_BUFFER_SIZE 80

/*for debugging*/
#define DEBUG_ME "LINKSERVER"
#include <toolLib/debugging.h>
DebugVariable(LINKARGS, "LINKARGS", 0x04); /* setenv LINKSERVER "LINKARGS"*/

LinkArgs *CreateLinkArgs(){
    LinkArgs *args = (LinkArgs*)malloc(sizeof(LinkArgs));
    if (NULL == args)
      return NULL; /* Memory allocation error */

    args->numberOfArgs = 0;
    args->args = NULL;
    return args;
}

/* 
 * The args list given is assumed to be a list of key-value pairs in
 * an array of LinkArgs, alternating key, value, etc.  If the key
 * matches one of the keys in the list, a pointer to the value for the
 * key is returned.  If a match is not found, a NULL is returned. 
 *
 * Note that all key-value pairs are stored internally as Unicode for
 * simpler transmission, as well as to allow users to send over Unicode
 * on their own.  Whenever we get a value for a key, we translate the
 * user-supplied key from ASCII to Unicode and then perform the look-up.
 */

char* GetValueForKey(LinkArgs *args, char* key) {
  char *temp_key = ASCIIToEnglishUnicode(key);

  LinkString *ls = args->args;

  while (ls - (args->args) < args->numberOfArgs){
    if (unicodecmp(temp_key, ls->text, strlen(key) * 2)) {
      return (++ls)->text;
    }
    ls+=2;
  }
  return (char*)NULL;
}

int SetKeyIntegerPairInLinkArgs(LinkArgs *linkArgs, char *key, int value){
    char *buf = (char*)malloc(DEFAULT_BUFFER_SIZE);
    if (buf == NULL) return -1; /* Memory allocation error */
    sprintf(buf, "%d", value);
    SetKeyValuePairInLinkArgs(linkArgs, key, buf, 0, 0);
    free(buf);
    return OK;
}

int SetKeyDoublePairInLinkArgs(LinkArgs *linkArgs, char *key, double value){
    char *buf = (char*)malloc(DEFAULT_BUFFER_SIZE);
    if (buf == NULL) return -1; /* Memory allocation error */
    sprintf(buf, "%f", value);
    SetKeyValuePairInLinkArgs(linkArgs, key, buf, 0, 0);
    free(buf);
    return OK;
}

int SetKeyValuePairInLinkArgs(LinkArgs *linkArgs, char *key, char *value, 
                              int is_unicode, int num_unicode_chars){
    int count, numberOfArgs;
    LinkString *oldStrings;
    int keyExists = 0;
    char *temp_key, *temp_value;
    int i;


    /* 
     * First, check and make sure that the key doesn't exist for another
     * value. If it does, replace the value for that key with the new
     * value.
     */

    LinkString *ls = linkArgs->args;
    numberOfArgs = linkArgs->numberOfArgs;

    /*
     * Translate the key and value into Unicode for matching and storage.
     */

    temp_key = ASCIIToEnglishUnicode(key);
    temp_value = is_unicode ? value : ASCIIToEnglishUnicode(value);
    
    while (ls - (linkArgs->args) < numberOfArgs){
      /*
       * We use unicodecmp() here to compare two Unicode strings.
       * See the comments in LinkSocket.c for more information.
       */
        if (!unicodecmp(temp_key, ls->text, strlen(key) * 2)) {
          keyExists = 1;
          (++ls)->numberOfChars = strlen(value) * 2;
          free(ls->text); /* Dump the old text */
          ls->text = (char*)malloc(ls->numberOfChars + 1);
          if (ls->text == NULL) 
            return -1; /* Memory allocation error */
          
          /*
           * This was originally handled with a strcpy(ls->text, temp_value),
           * and would have failed for any Unicode strings as values.
           * Note that if Unicode chars are passed as values in the first
           * place, we need to pass through the number of characters they
           * take up; strlen() on that value would fail miserably.
           */

          for (i = 0; 
               i < (is_unicode ? num_unicode_chars : strlen(value) * 2);
               i++) {
            ls->text[0] = temp_value[i];
          }
        } 
        else {
          ls+=2;
        }
    }

    free(temp_key); /* Free up the memory used to translate into Unicode */
    free(temp_value);


    /*
     * If the key didn't exist in the link arguments, create storage for
     * the new key-value pair.
     */


    if (!keyExists){
      temp_key =   ASCIIToEnglishUnicode(key);
      temp_value = is_unicode ? value : ASCIIToEnglishUnicode(value);

      /*Keep track of the old strings.*/
      oldStrings = linkArgs->args;

      /*Allocate memory for the addition.*/
      linkArgs->args = (LinkString *)malloc(sizeof(LinkString) * (numberOfArgs + 2));

      if (NULL == linkArgs->args) 
        return -1; /* Memory allocation error. */
        
      /** Copy the old strings to the new memory.*/
      memcpy(linkArgs->args, oldStrings, sizeof(LinkString)*numberOfArgs);

      /** Save the new pair of in the linkArgs. First the key,*/

      count = strlen(key) * 2; /* Unicode uses twice as many chars as ASCII */

      linkArgs->args[numberOfArgs].numberOfChars = count;
      linkArgs->args[numberOfArgs].text = (char *)malloc(count + 1);

      if (linkArgs->args[numberOfArgs].text == NULL)
        return -1; /* Memory allocation error */

      /*
       * Ideally, we should be able to use strcpy() to copy the contents of 
       * temp_key into linkArgs->args[numberOfArgs].text.  However, since the
       * first character of our Unicoded key is 0x0, strcpy() will stop 
       * immediately.
       */

      for (i = 0; i < count; i++)
        linkArgs->args[numberOfArgs].text[i] = temp_key[i];

      /** And now the value. */
      numberOfArgs++;

      /* Unicode uses twice as many chars as ASCII*/
      count = is_unicode ? num_unicode_chars : strlen(value) * 2; 

      linkArgs->args[numberOfArgs].numberOfChars = count;
      linkArgs->args[numberOfArgs].text = (char *)malloc(count + 1);

      if (NULL == linkArgs->args[numberOfArgs].text) 
        return -1; /* Memory allocation error */

      for (i = 0; i < count; i++)
        linkArgs->args[numberOfArgs].text[i] = temp_value[i];

      /** Modify the count to include the new key and value. */
      linkArgs->numberOfArgs+=2;
      /** Free up the structure holding the old pointers.*/
      free(oldStrings);

      /* 
       * And free up the memory for the Unicode versions of the key-value
       * pairs.
       */

      free(temp_key);
      free(temp_value);
    }
    return OK;
}

int RemoveKeyFromLinkArgs(LinkArgs *linkArgs, char *key){
    int numberOfArgs;
    int keyExists = 0;
    char *temp_key = ASCIIToEnglishUnicode(key);

    /* First, make sure the key actually exists in the arguments.*/
    LinkString *ls = linkArgs->args;
    numberOfArgs = linkArgs->numberOfArgs;

    while (ls - (linkArgs->args) < numberOfArgs){
      if (!unicodecmp(temp_key, ls->text, strlen(key) * 2)) {
        keyExists = 1;
        break;
      } 
      else {
        ls+=2;
      }
    }

    if (keyExists){
      int i, k;
      int j = 0;
      LinkString *oldStrings;

      /*Keep track of the old strings.*/
      oldStrings = linkArgs->args;
      /*Allocate memory for the newer, smaller array link arguments. */
      linkArgs->args = (LinkString *)malloc(sizeof(LinkString) * (numberOfArgs - 2));
      
      if(NULL == linkArgs->args) 
          return -1; /* Memory allocation error */

      j = 0;
      for (i = 0; i < numberOfArgs; i++){
        if (!unicodecmp(oldStrings[i].text, temp_key, strlen(key) *2)) {
          free(oldStrings[i++].text);
          free(oldStrings[i++].text);
        } 
        else {
          /* Copy the good pair.... */
          linkArgs->args[j].numberOfChars = oldStrings[i].numberOfChars;
          for (k = 0; k < oldStrings[i].numberOfChars; k++) {
            linkArgs->args[j].text[k] = oldStrings[i].text[k];
          }
          i++;
          j++;

          linkArgs->args[j].numberOfChars = oldStrings[i].numberOfChars;
          for (k = 0; k < oldStrings[i].numberOfChars; k++) {
            linkArgs->args[j].text[k] = oldStrings[i].text[k];
          }
          i++;
          j++;
        }
      }
        
        /** Modify the count to remove the specified key and value. */
        linkArgs->numberOfArgs-=2;
        /** Free up the structure holding the old pointers.*/
        free(oldStrings);
    }
    free(temp_key);
    return OK;
}

int WriteLinkArgString(LinkSocket *linkSocket, LinkString *str)
{
    int check = OK;
    /*Write number of characters in string*/
    
/*     if (Debug(LINKARGS)){ */
/*      printf("WriteLinkArgString: %d characters: %s\n", str->numberOfChars, str->text); */
/*     } */

    check = check || WriteInteger(linkSocket, str->numberOfChars / 2 );
    check = check || WriteUnicodeChars(linkSocket, str->text, str->numberOfChars);
    return check; /* Returns -1 if there was a memory allocation error */
}

int ReadLinkString(LinkSocket *linkSocket, LinkString *args) 
{ 
    /* How many characters in a string*/
    int check = OK;
    check = check || ReadInteger(linkSocket, &(args->numberOfChars));

    /*Allocate memory. Keep space for NULL*/
    args->text = (char *)malloc(args->numberOfChars + 1);
    if(NULL == args->text)
      return -1; /* Memory allocation error */

    /*Read them from socket. */
    if(args->numberOfChars > 0)
    {
        check = check || ReadChars(linkSocket, args->text, args->numberOfChars);
        args->text[args->numberOfChars] = NULL;
    }

    return check;
} 

int WriteLinkString(LinkSocket *linkSocket, LinkString *str)
{
    int check = OK;
    /*Write number of characters in string*/
    check = check || WriteInteger(linkSocket, str->numberOfChars);
    check = check || WriteChars(linkSocket, str->text, str->numberOfChars);
    return check;
}

int BufferedWriteLinkString(char *toBuffer, LinkString *str)
{
    int byteswritten = 0;
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         str->numberOfChars);
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       str->text, str->numberOfChars);
    return byteswritten;
}

void FreeLinkString(LinkString *string)
{
    free(string->text);
    string->text = NULL;  
}

int ReadLinkArgs(LinkSocket *linkSocket, LinkArgs *linkargs) 
{ 
    int i;
    int check = OK;

    /*
     * ### bmackiew: Remember that Unicode is being read in, not ASCII.
     */

    /* How many arguments are coming*/
    check = check || ReadInteger(linkSocket, &(linkargs->numberOfArgs)); 

    if (Debug(LINKARGS)) printf ("ReadLinkArgs: reading %d args: \n",
                                 linkargs->numberOfArgs);

    /*Allocate memory*/
    linkargs->args = (LinkString *)malloc(sizeof(LinkString) *
                                          linkargs->numberOfArgs);
    if(NULL == linkargs->args)
      return -1; /* Memory allocation error */

    /* Read arguments*/    
    for(i=0; i < linkargs->numberOfArgs; i++ )
    {

        /*
          though args is a string object, we do not use readlinkstring as below,
          because readlinkstring reads only chars, where as we need unicode chars
       
          check = check & ReadLinkString(linkSocket, &linkargs->args[i]);
        */     
        check = check || ReadInteger(linkSocket, &(linkargs->args[i].numberOfChars));   
        linkargs->args[i].text = (char *)malloc(linkargs->args[i].numberOfChars + 1);   
        if(NULL == linkargs->args[i].text)
          return -1; /* Memory allocation error */

        /*Read them from socket. */
        check = check || ReadUnicodeChars(linkSocket, linkargs->args[i].text, 
                                          linkargs->args[i].numberOfChars);
        if (check == -1)
          return -1; /* Memory allocation error */

        linkargs->args[i].text[linkargs->args[i].numberOfChars] = '\0'; 
        if (Debug(LINKARGS)) printf ("ReadLinkArgs: reading arg %d|%s\n", i, 
                                     linkargs->args[i].text);
    }
    
    return check;
}

int WriteLinkArgs(LinkSocket *linkSocket, LinkArgs *linkArgs)
{
    int i;
    int check = OK;
    LinkString *ls;

    if (Debug(LINKARGS)){
      printf("WriteLinkArgs: Writing args %d\n", linkArgs->numberOfArgs);
    }
    
    check = check || WriteInteger(linkSocket, linkArgs->numberOfArgs); 

    for (i = 0; i < linkArgs->numberOfArgs; i++){
        ls = (linkArgs->args) + i;
        check = check || WriteLinkArgString(linkSocket, ls);
        if (check == -1)
          return -1; /* Memory allocation error */
    }
    return check;
}

int BufferedWriteLinkArgs(char *toBuffer, LinkArgs *linkArgs)
{
    int i;
    int byteswritten = 0;
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         linkArgs->numberOfArgs);
    for (i = 0; i < linkArgs->numberOfArgs; i++){
        byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                             linkArgs->args[i].numberOfChars);
        byteswritten += BufferedWriteUnicodeChars(&toBuffer[byteswritten],
                                                  linkArgs->args[i].text,
                                                  linkArgs->args[i].numberOfChars);
    }
    return byteswritten;
}

int LinkSizeOfLinkArgs(LinkArgs *linkArgs)
{
    int i;
    int size = 0;

    /*
     * ### bmackiew: This should be updated for Unicodeness, too.
     */

    size += N_BYTES_PER_INTEGER;
    for (i = 0; i < linkArgs->numberOfArgs; i++)
    {
        size += N_BYTES_PER_INTEGER;

        /*
         * ### bmackiew: Now that this data is stored in Unicode, we need
         *               only count the number of characters stored.
         */
#if 0
        size += N_CHARS_PER_UNICODE_CHAR*linkArgs->args[i].numberOfChars;
#else
        size += linkArgs->args[i].numberOfChars;
#endif
    }
    return size;
}

void FreeLinkArgs(LinkArgs *linkargs)
{
    int i;
    for(i=0; i < linkargs->numberOfArgs; i++){
        FreeLinkString(&linkargs->args[i]);
    }
    free(linkargs->args);
}

