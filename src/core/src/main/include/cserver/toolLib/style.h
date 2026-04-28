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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/style.h,v $
 * $RCSfile: style.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* cosmetic C definitions, "complex" */

#ifndef STYLE_H
#define STYLE_H 1

#ifdef __CENTERLINE__
#ifndef SABER
#define SABER 1
#endif
#endif

#define NormalReturn    0
#define ErrorReturn     -1

#ifndef NULL
#define NULL            0
#endif

#ifndef TRUE
#define TRUE            1
#define FALSE           0
#endif

#ifndef True
#define True            1
#define False           0
#endif

#define TrueString(i)    ((i) IS True ? "T" : "F")
#define plural(i)         ((i) IS 1 ? "" : "s")

#define endof(a)        ((a) + strlen (a))

#define bitset(w,b)     ((w) |= (b))
#define bitclear(w,b)   ((w) &= ~(b))

#define STREQ(a,b)      (strcmp ((a), (b)) IS 0)

#define Max2(a,b)       ((a) > (b) ? (a) : (b))
#define Min2(a,b)       ((a) < (b) ? (a) : (b))

struct complex
    {
    float real;
    float imag;
    };

typedef struct complex complex;

#ifndef Bool
#define Bool    int
#endif

#endif
