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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/error_hand.h,v $
 * $RCSfile: error_hand.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef error_hand_include
#define error_hand_include

#include "compat.h"

/* Include errno.h if EPERM (errno of 1) is not defined yet. */
#ifndef EPERM
#include <errno.h>
#endif


extern char msgBuf[];

#define SaveLevelBits           0x00FF
#define SaveModeBits            0xFF00

#define SaveInfoMessages        0x01
#define SaveDebugMessages       0x02
#define SaveWarningMessages     0x04
#define SaveFatalMessages       0x08
#define SaveStatusMessages      0x10

#define SyncAfterWritingError   0x8000
#define NoStderrOutput          0x4000

#define DefaultMessageLevel     (SaveWarningMessages \
                                 | SaveStatusMessages \
                                 | SaveFatalMessages)

#define DefaultStatusStringLength       10

#define EH_Error(l, s)  HandleErrorMessage(l, s, __FILE__, __LINE__, 0)
#define EH_PError(l, s) HandleErrorMessage(l, s, __FILE__, __LINE__, errno)

#define INFO_MESSAGE(s)         EH_Error(SaveInfoMessages, s)
#define DEBUG_MESSAGE(s)        EH_Error(SaveDebugMessages, s)
#define WARNING_MESSAGE(s)      EH_Error(SaveWarningMessages, s)
#define FATAL_MESSAGE(s)        EH_Error(SaveFatalMessages, s)
#define STATUS_MESSAGE(s)       EH_Error(SaveStatusMessages, s)

#define INFO_PERROR(s)          EH_PError(SaveInfoMessages, s)
#define DEBUG_PERROR(s)         EH_PError(SaveDebugMessages, s)
#define WARNING_PERROR(s)       EH_PError(SaveWarningMessages, s)
#define FATAL_PERROR(s)         EH_PError(SaveFatalMessages, s)

BEGIN_extern_C

extern int CustomizeErrorHandler(
#if NeedFunctionPrototypes
 const char *,                  /* string */
 const int *,                   /* integerPointer */
 const char *,                  /* fileName */
 int                            /* saveLevel */
#endif
);

extern void HandleErrorMessage(
#if NeedFunctionPrototypes
 int,                           /* level */
 const char *,                  /* string */
 const char *,                  /* file */
 int,                           /* line */
 int                            /* error */
#endif
);

#define BadMagic(func) \
    { \
        sprintf(msgBuf, "Corrupted data structure in %s()", func); \
        WARNING_MESSAGE(msgBuf); \
    }


#define customize_error_handler CustomizeErrorHandler
#define handle_error_message    HandleErrorMessage

#define NoMemory(func, size) \
    { \
        sprintf(msgBuf, "Not enough memory in %s(), needed %d bytes", \
                func, size); \
        WARNING_PERROR(msgBuf); \
    } 

END_extern_C

#endif
