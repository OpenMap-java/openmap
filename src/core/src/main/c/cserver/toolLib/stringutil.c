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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/stringutil.c,v $
 * $RCSfile: stringutil.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* SYSTEM LEVEL HEADER FILES */
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/param.h>

/* #define MAC_OS_X */

#ifndef MAC_OS_X
#include <malloc.h>
#endif

#include <ctype.h>
#include <string.h>

/* OTHER HEADER FILES */
#include "compat.h"
#include "stringutil.h"
#include "style.h"

/*
 *      Find a string in a list given an index
 *
 *      RETURNS:
 *              A pointer to a character string.
 *              If the list did not contain the
 *              index, it returns the third argument.
 */

char *LookupString(int index,   /* The integer to look up */
                   StringList *list,    /* The list to look it up in */
                   char *defaultString) /* Return this if not found */
{
    if(list == NoStringList)
        return((char *) 0);
    
    while(list->string != (char *) 0)
    {
        if(list->index == index)
            return(list->string);
        list++;
    }
    return(defaultString);
}


/*
 *  Does the opposite of LookupString()
 * 
 * RETURNS:  
 *      The first index within the list where the string is found           
 */
int LookupIndex(
                char *s,                        /* Look up this string */
                StringList *list,               /* Use this list */
                int notFound)                   /* Return this if not found */
{
    while(list->string != (char *) 0)
    {
        if(strcmp(list->string, s) == 0)
            return(list->index);
        list++;
    }
    return(notFound);
}

/*
 *              Find a string in a list given an index
 *
 *              Format the output into a string
 *
 *      RETURNS:
 *              a pointer to a character string.
 *              If the list did not contain the
 *              index, it returns the third argument.
 */
char *FLookupString(char *resultString, /* The place to format to */
                    int index,          /* The integer to look up */
                    StringList *list,   /* The list to look in */
                    char *string,       /* The default if not found */
                    int width,          /* How wide the field is */
                    int justification)  /* Left or right */
{
    if(list == NoStringList 
       || resultString == (char *) 0 || string == (char *) 0)
        return((char *) 0);
    
    while(list->string != (char *) 0)
    {
        if(list->index == index)
        {
            sprintf(resultString,
                    "%*s",
                    width * justification,
                    list->string);
            
            return(resultString);
        }
        list++;
    }

    if(width > (int) strlen(string))
        width -= strlen(string);
    
    sprintf(resultString, 
            "%s%*d", 
            string, 
            width * justification,
            index);

    return(resultString);
}


/*
 *              Print out an array of text strings.
 *
 *              Newlines are appended to end of each line.
 *              Last string must be NULL
 *
 *      RETURNS:
 *              Nothing
 */

void PrintText(FILE *stream,    /* Where to put the resulting text */
               char **text)/* The text, as a list of strings */
{
    if(stream == (FILE *) 0 || text == (char **) 0 || *text == (char *) 0)
        return;

    while((*text != (char *) 0) && (*text[0] != '\0'))
        fprintf(stream, "%s\n", *text++);
}


/*
 *      Find the next non-blank character in a string
 *
 *      Returns a pointer to the next non-blank character in the string
 *      where blank is defined as space or tab.
 * 
 * RETURNS:  
 *      Pointer to the first non-blank character, null pointer if
 *      none found
 */
char *NextNonblank(char *s)     /* The string to look in */
{
    int temp;
    
    if(s == (char *) 0)
        return((char *) 0);
    
    temp = strspn(s, " \t");

    if(temp > (int) strlen(s))
        return((char *) 0);
    
    return(&s[temp]);
}



/*
 * Do the equivalent of basename(1)
 *
 *      Find the base name of the string (i.e. strip off the leading
 *      directory names in a pathname).
 *
 * RETURNS:  
 *      pointer to the beginning of the base name.
 */
const char *Basename(const char *s)     /* The string to look in */
{
    const char *filePart;
    
    if(s == (char *) 0)
        return((char *) 0);
    
    filePart = strrchr(s, '/');

    if(filePart == (char *) 0)
        return(s);
    
    return(filePart);
}



/*
 * Returns the length of the ascii string pointed to by "token".
 *
 *      RETURNS:
 *              how many characters the token has
 *              0 if the first char is whitespace or null
 *                or if the token ptr is null.
 *
 */
int TokenLength(char *token)    /* The token to look at */
{
    int length = 0;
    
    if (token == (char *) 0)
        return (0);

    while(*token && !isspace(*token) )
    {
        length++;
        token++;
    }
    return(length);
}

/*
 * Find a token
 *
 *   The token is defined to begin with the first non-blank character
 *   in s, up through the last non-blank character in s that
 *   comes before the first occurrence of the separator or \n or \0 in s.
 *
 *      RETURNS:
 *              Ptr to the token and sets whatever lengthp points to
 *              to the length of the token.
 * .LP
 *              If lengthp is NULL, returns ptr to statically allocated ""
 *              If s is NULL, returns ptr to statically allocated ""
 *              If token is zero long, returns ptr.
 * .LP
 *              separatorp gets set to point at the separator.
 *              
 */

char *GetTokenFromString(char *s,               /* The string to tokenize */
               char separator,  /* The separator character (i.e. : or \t) */
               int *lengthp,    /* Length of token returned through this */
               char **separatorp) /* Pointer to next separator found
                                   * i.e. starting point for next call */
{
    static char *nullToken = "";
    int done = FALSE;
    char *first;
    char *last;
    char *sep;
    int length = 0;
    
    /*
     * Check the args for null pointers,
     */

    if(lengthp == (int *) 0)
    {
        if(separatorp)
            *separatorp = s;
        return (nullToken);
    }     
    
    if(s == (char *) 0 || *s == '\0')
    {
        *lengthp = 0;   
        if(separatorp)
            *separatorp = s;
        return(s);
    }
    
    if(*lengthp == -1)
    {
        /*
         * Figure out how long the token is, even the leading
         * whitespace. We'll get to the whitespace later.
         * If we try to strip whitespace now, and the separator
         * character is ' ' or '\t', we do the wrong thing.
         */
        
        sep = s;
        while(*sep && !done)
        {
            if(*sep == separator || *sep == '\n')
            {
                done = TRUE;
            }
            else
            {
                length++;
                sep++;
            }
        }
    }
    else
    {
        length = *lengthp;
        sep = s + length;
    }
    
    /*
     * sep points to the separator,
     * Now move backwards to find the last real char of the token.
     */

    *separatorp = sep;

    /*
     * If the separator was at the beginning, sep will be at s
     */

    if(sep == s)
    {
        *lengthp = 0;
        return(s);
    }
    
    /*
     * If we get to here, then there was at least on character
     * before the separator. Point 'first' at the beginning.
     * Point 'last' at the end.
     */

    first = s;
    last = sep - 1;

    /*
     * Move first toward last, as long as they have not met
     * and as long as first points to a piece of whitespace.
     *
     * Then back last up toward first...
     */

    while(first < last && isspace(*first))
        first++;
    
    while(first < last && isspace(*last))
        last--;
    
    /*
     * If first and last point to the same spot, and that
     * spot is no whitespace, then the length is 1.
     * Otherwise the length is zero.
     */
    
    if(first == last)
    {
        if(isspace(*first))
            *lengthp = 0;
        else
            *lengthp = 1;
        return(first);
    }
    
    /*
     * Compute the length from first and last and
     * return the pointer to the beginning of the token.
     *
     * The length is (last - first) + 1  since if first is 1
     * and last is 2, then there are two characters in the string...
     */

    *lengthp = (last - first) + 1;
    return(first);
}


/*
 *      Turn a string into its lowercase equivalent (in place).
 * 
 * RETURNS
 *      Pointer to the same string, but now lower case letters
 *           
 */

char *Lowercase(char *str)      /* The string to convert */
{
    char *temp = str;

    if(str)
    {
        while(*str)
        {
            *str = tolower(*str);
            str++;
        }
    }
    return(temp);
}


/*
 *      Looks for the occurrence of a word within a string
 * 
 * RETURNS
 *      Null pointer if not found, pointer withing string if found.
 *           
 */

char *FindWord(char *word,      /* Look for this word */
               char *string)    /* Look in this string */
{
    char *temp = string;
    int wlength;
    int slength;
    
    if(string == (char *) 0 || word == (char *) 0)
        return((char *) 0);
    
    if(*word == '\0' || *string == '\0')
        return((char *) 0);

    wlength = strlen(word);
    slength = strlen(string);
    
     while(*temp && slength >= wlength)
     {
         if(strncmp(temp, word, wlength) == 0)
             return(temp);
         
         temp++;
         slength--;
     }

    return((char *) 0);
}


char *NormalizePath(char *path)
{
    int i, f, x;
    char path0;
    
    if(path == (char *) 0)
        return(path);

    path0 = path[0];

    /* Collapse all the /./ and /.(null) combos */
    for(i = 0; i < (int) strlen(path); i++)
    {
        if(path[i] == '/'
            && (path[i+1] && path[i+1] == '.')
            && ((path[i+2] && path[i+2] == '/')
                || path[i+2] == '\0'))
        {
            for(x=i, f = i+2, i=x-1; f <= (int) strlen(path); x++, f++)
                path[x] = path[f];
            if(i < 0) i++;
        }
    }

    /* Collapse all the // and /(null) combos */
    for(i = 0; i < (int) strlen(path); i++)
    {
        if(path[i] == '/'
            && ((path[i+1] && path[i+1] == '/')
                || path[i+1] == '\0'))
        {
            for(x=i, f = i+1, i=x-1; f <= (int) strlen(path); x++, f++)
                path[x] = path[f];
            if(i < 0) i++;
        }
    }

    /* Collapse all the /../ and /..(null) combos */
    for(i = 0; i < (int) strlen(path); i++)
    {
        if (path[i] == '/'
            && (path[i+1] && path[i+1] == '.')
            && (path[i+2] && path[i+2] == '.')
            && ((path[i+3] && path[i+3] == '/')
                || path[i+3] == '\0'))
        {
            for(x = i-1; x >= 0; x--)
            {
                if(path[x] == '/')
                {
                    for(f = i+3,i=x-1; f <= (int) strlen(path); x++, f++)
                        path[x] = path[f];
                    if(i < 0) i++;
                    break;
                }
            }
        }
    }

    if(path[0] == '\0' && path0 == '/' || strcmp(path, "/..") == 0)
        strcpy(path, "/");

    return (path);
}


char *ExecDir(char *path,
              char *argv0)
{
    char *temp;
    char *p;
    int result;
    char cwd[MAXPATHLEN];
    static char dirbuf[MAXPATHLEN];
    static char *dir;
    struct stat statbuf;
    
    /* reset the pointer */
    dir = (char *) 0;
    
    /* If it starts with '/', then it's already an absolute path */
    if(argv0[0] == '/')
    {
        /* If it's too long to fit, then return error */
        if((int) strlen(argv0) < MAXPATHLEN)
        {
            strcpy(dirbuf, argv0);
            dir = dirbuf;
        }
        return(dir);
    }
    
    temp = strdup(path);
    if(temp == (char *) 0)
        return(dir);

    p = strtok(temp, ":");

    while(p && dir == (char *) 0)
    {
        if((int) (strlen(p) + strlen(argv0)) + 2 > MAXPATHLEN)
            return((char *) 0);
        
        sprintf(dirbuf, "%s/%s", p, argv0);
        result = stat(dirbuf, &statbuf);
        if(result >= 0)
        {
            if(S_ISREG(statbuf.st_mode)
               && ((statbuf.st_mode & S_IXUSR)
                   || (statbuf.st_mode & S_IXGRP)
                   || (statbuf.st_mode & S_IXOTH)))
            {
                dir = dirbuf;
            }
        }

        p = strtok((char *) 0, ":");
    }
    
    if(temp) free(temp);

    if(dir && dirbuf[0] != '/')
    {
#ifdef SVR4
        getcwd(cwd, sizeof(cwd) - 1);
#else
        getwd(cwd);
#endif
        
        if((int) (strlen(dirbuf) + strlen(cwd)) + 2 > MAXPATHLEN)
        {
            dir = (char *) 0;
            return(dir);
        }
        
        strcat(cwd, "/");
        strcat(cwd, dirbuf);
        strcpy(dirbuf, cwd);
    }
    
    return(NormalizePath(dir));
}


char *SetEnvVar(char *name, char *value)
{
    char *temp = (char *) 0;
    int len;
    
    len = strlen(name) + strlen(value) + 2;
    temp = malloc(len);

    if(temp != (char *) 0)
    {
        sprintf(temp, "%s=%s", name, value);
    
        if(putenv(temp) != 0)
        {
            return((char *) 0);
        }
    }

    return(temp);
}
    
