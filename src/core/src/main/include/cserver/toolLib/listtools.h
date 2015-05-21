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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/listtools.h,v $
 * $RCSfile: listtools.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/*
 *      Macros to manage a singly linked list.
 *
 *      First off define a structure that contains a "next" pointer. It
 *      need not be called 'next'. The structure type is always the 1st
 *      argument of the macro.
 *
 *      Then you can make any number of lists. You hand in the name as the
 *      second argument of each macro.
 *
 *      These examples use the following structure and the single instance
 *      of it.
 *
 *      typedef struct _foobar
 *      {
 *          int x;
 *          struct _foobar *nextPtr;
 *      } FooBar;
 *
 *      FooBar myFooBar;
 *
 *====> To Declare a list:
 *
 *      ListOf(FooBar, fooBarList, nextPtr);
 *
 *              This will set up a list called fooBarList, which will
 *              keep track of its elements using the "nextPtr" element of
 *              the FooBar typedef.
 *
 *
 *====> To add an element to the head of the list:
 *
 *      AddToHead(FooBar, fooBarList, &myFooBar);
 *
 *====> To remove an element:
 *
 *      RemoveFromList(FooBar, fooBarList, &myFooBar);
 *
 *====> To get the # of elements on the list (assuming no one else has
 *      mucked with it without using these macros:
 *
 *      GetListCount(FooBar, fooBarList)
 *
 *====> To make sure the count is right, in case someone did muck with it:
 *
 *      ConfirmListCount(FooBar, fooBarList)
 *
 * ------------------------------------------------------------------------ */

#ifndef list_include
#define list_include

#include "compat.h"

BEGIN_extern_C

#ifndef OffsetInStruct
#define OffsetInStruct(cast, element) \
    ((unsigned long)(&(((cast *)(0))->element)))
#endif

#define NextPtr(t, name, x) (*(t **)((unsigned long)(x) + STRCAT(name,Offset)))

#define ListOf(t, name, next) \
    t *name = 0; \
    int STRCAT(count,name) = 0; \
    static unsigned long STRCAT(name,Offset) = OffsetInStruct(t, next)

#define AddToHead(t, name, p) \
    NextPtr(t, name, p) = name; \
    name = p; \
    STRCAT(count,name)++
    
#define AddBefore(t, name, p, c) \
{ \
    t **prev = &name; \
    while(*prev) \
    { \
        if(NextPtr(t, name, (*prev)) == c) \
        { \
            NextPtr(t, name, (p)) = NextPtr(t, name, ((*prev))); \
            *prev = p; \
            break; \
        } \
    } \
}

#define RemoveFromList(t, name, p)                                            \
{                                                                             \
    t *temp = name;                                                           \
    if(name == p)                                                             \
    {                                                                         \
        name = NextPtr(t, name, p);                                           \
        STRCAT(count,name)--;                                                 \
    }                                                                         \
    else                                                                      \
    {                                                                         \
        while(temp)                                                           \
        {                                                                     \
            if(NextPtr(t, name, temp) == p)                                   \
            {                                                                 \
                NextPtr(t, name, temp) = NextPtr(t, name, p);                 \
                STRCAT(count,name)--;                                         \
                break;                                                        \
            }                                                                 \
        }                                                                     \
    }                                                                         \
}    

#define GetListCount(t, name) (STRCAT(count,name))
#define ConfirmListCount(t, name)                                             \
{                                                                             \
    t *temp = name;                                                           \
    STRCAT(count,name) = 0;                                                   \
    while(temp)                                                               \
    {                                                                         \
        STRCAT(count,name)++;                                                 \
        temp = NextPtr(t, name, temp);                                        \
    }                                                                         \
}    

END_extern_C

#endif
