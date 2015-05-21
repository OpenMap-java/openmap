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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/mapped_files.h,v $
 * $RCSfile: mapped_files.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef mapped_files_include
#define mapped_files_include

#include "compat.h"

BEGIN_extern_C

typedef struct fileEntry
{
    int size;
    int currOffset;
    int time;
    char *address;
    char filename[1024];
} FileEntry;

#define LRUEntries 25

#define NoIndexEntry ((IndexEntry *) 0)


extern Bool FileExists(
#if NeedFunctionPrototypes
 char *name
#endif
); 

int SeekMappedFile(
#if NeedFunctionPrototypes
                   FileEntry*, long offset, int ptrname
#endif
                   );

extern char* OpenMappedFile(
#if NeedFunctionPrototypes
                            char *name
#endif
                            );

extern FileEntry* FEOpenMappedFile(
#if NeedFunctionPrototypes
 char *name, int auto_closable
#endif
); 

void CloseMappedFile(
#if NeedFunctionPrototypes
 FileEntry *name
#endif
);

extern void FlushMappedFileTable(
#if NeedFunctionPrototypes
#endif
); 

extern int GetMappedFileSize(
#if NeedFunctionPrototypes
 char *name
#endif
); 

END_extern_C

#endif
