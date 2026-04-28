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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/stringutil.h,v $
 * $RCSfile: stringutil.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef stringutil_include
#define stringutil_include

#include <stdio.h>

#include "compat.h"
BEGIN_extern_C

typedef struct _stringList
{
    int         index;
    char        *string;
} StringList;
#define NoStringList ((StringList *) 0)

#define EmptyStringListEntry { 0, (char *) 0 }

#define EndOfText ((char *) 0)

#define RightJustify    1
#define LeftJustify     -1

#ifndef Min
#define Min(x,y)  (((x)>(y))? (y) : (x))
#endif

#define LeftJustified   RightJustify
#define RightJustified LeftJustify

#define lookup_string   LookupString
#define flookup_string  FLookupString
#define print_text      PrintText
#define next_nonblank   NextNonblank
#define basename        Basename
#define token_length    TokenLength
#define get_token       GetTokenFromString
#define lowercase       Lowercase

/* Note: the comma operator evaluates left to right, (K&R C, ANSI C, C++) */
#define nstrncpy(to, from, l) (strncpy((to), (from), (l) - 1), \
                               (to)[(l) - 1] = '\0', (to))

extern char *LookupString(
#if NeedFunctionPrototypes
 int,                           /* index */
 StringList *,                  /* list */
 char *                         /* defaultString */
#endif
);

extern int LookupIndex(
#if NeedFunctionPrototypes
 char *,                        /* string */
 StringList *,                  /* list */
 int                            /* notFound */
#endif
);

extern char *FLookupString(
#if NeedFunctionPrototypes
 char *,                        /* resultString */
 int,                           /* index */
 StringList *,                  /* list */
 char *,                        /* string */
 int,                           /* width */
 int                            /* justification */
#endif
);

extern char *NextNonblank(
#if NeedFunctionPrototypes
 char *                         /* s */
#endif
);

/*
 * Do the equivalent of basename(1)
 *
 *      Find the base name of the string (i.e. strip off the leading
 *      directory names in a pathname).
 *
 * RETURNS:  
 *      pointer to the beginning of the base name.
 */

extern const char *Basename(
#if NeedFunctionPrototypes
 const char *                           /* s */
#endif
);

extern void PrintText(
#if NeedFunctionPrototypes
 FILE *,                        /* stream */
 char **                        /* text */
#endif
);

extern int TokenLength(
#if NeedFunctionPrototypes
 char *                 /* token */
#endif
);

extern char *GetTokenFromString(
#if NeedFunctionPrototypes
 char *,                        /* s */
 char,                          /* separator */
 int *,                         /* lengthp */
 char **                        /* separatorp */
#endif
);

extern char *Lowercase(
#if NeedFunctionPrototypes
 char *                         /* str */
#endif
);

extern char *FindWord(
#if NeedFunctionPrototypes
 char *,                        /* word */
 char *                         /* string */
#endif
);

extern char *NormalizePath(
#if NeedFunctionPrototypes
 char *                         /* path */
#endif
);
    
extern char *ExecDir(
#if NeedFunctionPrototypes
 char *,                        /* path */
 char *                         /* argv0 */
#endif
);

char *SetEnvVar(
#if NeedFunctionPrototypes
 char *,                        /* name */
 char *                         /* value */
#endif
);

END_extern_C

#endif

