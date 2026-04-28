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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/Magic.c,v $
 * $RCSfile: Magic.c,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include "Magic.h"

/* ------------------------------------------------------------------------
 * 
 * NewMagicNumber - Returns the argument as a magic number.
 * 
 * RETURNS:  The argument given as a magic number.
 *
 * The intent is to give all structures a Magic element that is filled
 * with a magic number unique to that structure type. Then during debugging,
 * the structure can be verified to be what it claims to be.
 *           
 * ------------------------------------------------------------------------ */

const Magic NewMagicNumber(const char *ident)
{
    return((Magic) ident);
}
