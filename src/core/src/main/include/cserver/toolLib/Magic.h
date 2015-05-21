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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/Magic.h,v $
 * $RCSfile: Magic.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef MagicInclude
#define MagicInclude

#include "compat.h"

typedef char *Magic;
BEGIN_extern_C

extern const Magic NewMagicNumber(
#if NeedFunctionPrototypes
 const char *ident
#endif
);

END_extern_C
#endif
