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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/compat.h,v $
 * $RCSfile: compat.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef compatInclude
#define compatInclude

#ifndef NeedFunctionPrototypes
#  if defined(__STDC__) || defined(__cplusplus)
#    define NeedFunctionPrototypes 1
#  else
#    define NeedFunctionPrototypes 0
#  endif
#endif /* NeedFunctionPrototypes */

/*
 * Traditional C compilers tend to replace the string consisting
 * of begin comment/endcomment with nothing, other C compilers replace
 * it with a single space. Luckily, those seem to use the ## as a non-spacing
 * concatenation operator.
 */

#if !defined(__OBJY_DDL__) && (defined(__GNUC__) || defined(__STDC__) || defined(__cplusplus))
#define STRCAT(A,B) A##B
#else
#define STRCAT(A,B) A/**/B
#endif

/*
 * This is to allow the use of const in both ansi/C++ and non-ansi C
 */

#if defined(__GNUC__) || defined(__STDC__) || defined(__cplusplus)

#else
#define const
#endif

#if defined(__cplusplus)
#define BEGIN_extern_C extern "C" {
#define END_extern_C }
#else
#define BEGIN_extern_C
#define END_extern_C
#endif

#endif
