// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/Assert.java,v $
// $RCSfile: Assert.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

/**
 * Assert provides an assertion facility in Java comparable to the
 * assert macros in C/C++. This class was taken from the Java FAQ
 * maintained by Perter Van Der Linden at <A
 * HREF=http://www.afu.com/intro.html>http://www.afu.com/intro.html
 * </A>, section 17, question 6. Here is the original entry in the
 * FAQ:
 * <p>
 * 
 * <pre>
 * 
 *  11.6 How can I write C/C++ style assertions in Java?
 * 
 *  A.  The two classes shown below provide an assertion facility in Java.
 *      Set Assert.enabled to true to enable the assertions, and to false to
 *      disable assertions in production code. The AssertionException is not
 *      meant to be caught--instead, let it print a trace.
 * 
 *      With a good optimizing compiler there will be no run time overhead
 *      for many uses of these assertions when Assert.enabled is set to false.
 *      However, if the condition in the assertion may have side effects, the
 *      condition code cannot be optimized away. For example, in the assertion
 *       
 * <code>
 * Assert.assertExp(size() &lt;= maxSize, &quot;Maximum size exceeded&quot;);
 * </code>
 * 
 *      the call to size() cannot be optimized away unless the compiler can
 *      see that the call has no side effects. C and C++ use the preprocessor
 *      to guarantee that assertions will never cause overhead in production
 *      code. Without a preprocessor, it seems the best we can do in Java is
 *      to write
 *       
 * <code>
 * Assert.assertExp(Assert.enabled &amp;&amp; size() &lt;= maxSize, &quot;Too big&quot;);
 * </code>
 * 
 *      In this case, when Assert.enabled is false, the method call can always
 *      be optimized away, even if it has side effects.
 *  
 * </pre>
 * 
 * </p>
 * 
 * @author Peter Van Der Linden
 * @author Maintained by: Tom Mitchell (tmitchell@bbn.com)
 * @version $Revision: 1.4 $, $Date: 2004/10/14 18:06:29 $
 */
public final class Assert {

    /**
     * Don't allow construction, all methods are static.
     */
    private Assert() {}

    /**
     * Globally enable or disable assertions.
     */
    public static final boolean enabled = true;

    /**
     * Assert a condition to be true. If it is not true, an exception
     * is thrown.
     * 
     * @param b An expression expected to be true
     * @param s Exception string if expression is false
     * @exception AssertionException if expression is false
     */
    public static final void assertExp(boolean b, String s) {
        if (enabled && !b)
            throw new AssertionException(s);
    }

    /**
     * Assert a condition to be true. If it is not true, an exception
     * is thrown.
     * 
     * @param b An expression expected to be true
     * @exception AssertionException if expression is false
     */
    public static final void assertExp(boolean b) {
        assertExp(b, "");
    }
}