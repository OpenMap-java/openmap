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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/error_hand.c,v $
 * $RCSfile: error_hand.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* SYSTEM LEVEL HEADER FILES */
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <time.h>
#ifdef c_plusplus
#include <sysent.h>
#endif

/* OTHER HEADER FILES */
#include "compat.h"
#include "error_hand.h"
#include "stringutil.h"

char            msgBuf[4096];
static char     errorString[4096];

static int      messageSaveLevel =      DefaultMessageLevel;
static const int *userInteger   =       (int *) 0;
static const char *userString   =       (char *) 0;
static const char *statusFile   =       (char *) 0;

/*+------------------------------------------------------------------------
 *      CustomizeErrorHandler()
 *
 *      Allows the user to set options for the error handler
 *
 *      RETURNS:
 *              Old save level
 *________________________________________________________________________*/

int CustomizeErrorHandler(const char *string,
                           const int *integerPointer,
                           const char *fileName,
                           int saveLevel)
{
    int temp = messageSaveLevel;
    
    userString       = string;
    userInteger      = integerPointer;
    statusFile       = fileName;
    messageSaveLevel = saveLevel;

    return(temp);
}

/*+------------------------------------------------------------------------
 *      ErrorMessage()
 *
 *      Builds a standard message of the form:
 *
 *      'xxx %d %s at Sat Mar  4 20:41:53 1989 - file:line %s\n'
 *      
 *              Where %s's are filled in with the strings given,
 *              and xxx is the string given with a call to set_error_ident(),
 *              and %d is the value in the int pointer given to 
 *              set_error_int_ptr().
 *
 *      RETURNS:
 *              Nothing
 *________________________________________________________________________*/

static StringList messageLevels[] =
{
 { SaveInfoMessages,            "INFO"},
 { SaveDebugMessages,           "DEBUG"},
 { SaveWarningMessages,         "WARNING"},
 { SaveFatalMessages,           "FATAL"},
 { SaveStatusMessages,          "STATUS"}, 
};
 
void HandleErrorMessage(int level,
                        const char *string, 
                        const char *file, 
                        int line,
                        int error)
{
    FILE *fp;
    char *levelString;
    const char *fileName;

/* #define MAC_OS_X */

#ifdef MAC_OS_X
    extern __const int sys_nerr;                /* perror(3) external variables */
    extern __const char *__const sys_errlist[];
#else
    extern char *sys_errlist[];
    extern int sys_nerr;
#endif
    
#ifdef USE_TIME
    long now;
    char *tempChar;
    char timeString[80];
#else
    static int which = 0;
#endif
    
    /*
     * Interpret the message level
     */
    levelString = LookupString(level & SaveLevelBits, messageLevels, "");

    /*
     * Look up the error value in the system error list
     */

    if(error > 0 && error < sys_nerr)
    {
        sprintf(errorString, ": %s (%d)", sys_errlist[error], error);
    }
    else
    {
        errorString[0] = '\0';
    }

    /*
     * Find the file name minus the preceding path name
     */

    fileName = Basename(file);
    
#ifdef USE_TIME
    /*
     * Construct a nice, ascii time. The ctime function returns a string
     *  containing a newline. It has to be stripped out.
     */

    now = time(NULL);
    strcpy(timeString, ctime(&now));
    tempChar = strrchr(timeString, '\n');
    if(tempChar)
        *tempChar = '\0';
#else
    which++;
#endif

    if(!(messageSaveLevel & NoStderrOutput))
    {
        /*
         * Print the message to the console/stderr
         */
        
        if(userString != NULL)
        {
            fprintf(stderr, "%s ", userString);
        }
        
        if(userInteger != NULL)
        {
            fprintf(stderr, "%d ", *userInteger);
        }
        
#ifdef USE_TIME
        fprintf(stderr,
                "%-*s at %s - %14s:%04d %s %s\n",
                DefaultStatusStringLength,
                levelString,
                timeString,
                fileName,
                line,
                string,
                errorString);
#else
        fprintf(stderr,
                "%-*s %04d %14s:%04d %s %s\n",
                DefaultStatusStringLength,
                levelString,
                which,
                fileName,
                line,
                string,
                errorString);
#endif
        
    }

    /*
     * Decide whether to open the status file
     */
    
    if(statusFile == NULL)
        return;
    
    /*
     * Check the level to see if it's
     * an INFO, DEBUG, FATAL, or WARNING message and whether to save
     * the sucker to the file.
     */
    
    if((level & messageSaveLevel) == 0)
        return;
    
    /*
     * Open the report file for appending.
     */

    fp = fopen(statusFile, "a+");
    if(fp == NULL)
        return;

    if(userString != NULL)
    {
        fprintf(fp, "%s ", userString);
    }

    if(userInteger != NULL)
    {
        fprintf(fp, "%d ", *userInteger);
    }
    
#ifdef USE_TIME
    fprintf(fp,
            "%-*s at %s - %14s:%d %s %s\n",
            DefaultStatusStringLength,
            levelString,
            timeString,
            fileName,
            line,
            string,
            errorString);
#else
    fprintf(fp,
            "%-*s %04d %14s:%d %s %s\n",
            DefaultStatusStringLength,
            levelString,
            which,
            fileName,
            line,
            string,
            errorString);
#endif

    /*
     * Close the file.
     * Syncing depends on the mode bit in the message save level
     */

    if(messageSaveLevel & SyncAfterWritingError)
        fsync(fileno(fp));
    
    fclose(fp);
}

