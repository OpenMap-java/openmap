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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/debugging.h,v $
 * $RCSfile: debugging.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */


/*
 *      debugging.h - Header file to use in aid of debugging
 *
 *      Allows debugging printout to be controlled from environment
 *      with minimal overhead to program.
 *
 *
 *      Basic usage:
 *
 *
 *      #define DEBUG_ME "DEBUG_ME"
 *      #include "debugging.h"
 *
 *
 *      DebugVariable(x, "X", 0x01);
 *      DebugVariable(y, "Y", 0x02);
 *              ...
 *
 *      if(Debug(x))
 *      {
 *              ...
 *              some debugging code here
 *              ...
 *      
 *      }
 * 
 *
 *      In the environment, if "DEBUG_ME" is set properly,
 *      it will cause the debug code to run.
 *      Otherwise, it will not. Different source files can
 *      use different environment variables.
 *
 *      E.g.
 *      setenv DEBUG_ME "X Y"
 *        would cause Debug(x) and Debug(y) both to evaluate to true.
 *      setenv DEBUG_ME "X"
 *        would cause only Debug(x) to evaluate to true.
 *
 *
 *      The overhead consists of a getenv() and some test code for
 *      the first occurrence of a Debug(x) test and then if
 *      the environment contains the variable, a single test
 *      otherwise two tests per subsequent occurrence. The key is that
 *      the getenv() section does not run more than once per variable.
 *
 *
 *      For extra speed, bracket the if(Debug(x)) blocks with
 *      #ifdef DEBUG_ME
 *              ...
 *      #endif
 *
 * ------------------------------------------------------------------------ */


#ifndef _debugging
#define _debugging

#include <stdlib.h>

#include "compat.h"
#include "stringutil.h"

BEGIN_extern_C

#if defined(SABER) || defined(__CENTERLINE__)
/*SUPPRESS 592*//*Saber Empty body for 'if' statement */
#endif


#ifdef DEBUG_ME

static unsigned int ___init  = 0;
static unsigned int ___debug = 0;

#if defined(__GNUC__) || defined(__STDC__) || defined(__cplusplus)
#define DebugVariable(x, s, bits) \
static unsigned int __##x = bits; \
static char *x##__str = s

#define Debug(x)                                                      \
(                                                                     \
 (___debug & __##x)                                                   \
 ||                                                                   \
 (                                                                    \
  !(___init & __##x)                                                  \
  &&                                                                  \
  ( (FindWord(x##__str, getenv(DEBUG_ME)) == (char *) 0) ?            \
   (___init |= __##x)?0:1 : (___init |= __##x, ___debug |= __##x))            \
  )                                                                   \
 )                                                                    
#else

#define DebugVariable(x, s, bits) \
static unsigned int __/**/x = bits; \
static char *x/**/__str = s

#define Debug(x)                                                      \
(                                                                     \
 (___debug & __/**/x)                                                 \
 ||                                                                   \
 (                                                                    \
  !(___init & __/**/x)                                                \
  &&                                                                  \
  ( (FindWord(x/**/__str, getenv(DEBUG_ME)) == (char *) 0) ?          \
   (___init |= __/**/x)?0:1 : (___init |= __/**/x, ___debug |= __/**/x)) \
  )                                                                   \
 )                                                                    
#endif

#else

#define Debug(x)        0

#endif                          /* ifdef DEBUG_ME */

END_extern_C

#endif                          /* ifndef _debugging */
