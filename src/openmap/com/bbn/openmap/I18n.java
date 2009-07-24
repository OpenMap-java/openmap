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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/I18n.java,v $
// $RCSfile: I18n.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Interface for Internationalization support in Openamp. The I18n
 * implementation will define where the resources for the given class
 * are stored.
 * <P>
 * <B>Intended Usage: </B>
 * <P>
 * There are several possible uses of this interface, outlined below:
 * 
 * <UL>
 * <LI><I><B>Simple Usage: </B> </I> <BR>
 * The simplest case is where you just want to get the possibly
 * internationalized version of a given string. This would look like
 * the following:
 * <P>
 * <code>
 * I18n i18n = Environment.getI18n();<br>
 * String message = i18n.get(this, "message", "Please enter a number: ");<br>
 * </code>
 * <P>
 * There is also similar version of <code>get(...)</code> that takes
 * a <code>Class</code> instead, and thefore can be used in static
 * contexts. If the Strings you are looking for pertain to specific
 * types (i.e. uses) you can you the form of <code>get(...)</code>
 * that specifies this, as the following example illustrates:
 * <P>
 * <code>
 * I18n i18n = Environment.getI18n();<br>
 * JButton okButton = new JButton(i18n.get(this, "okButton", i18n.TEXT, "Ok"));
 * <br>
 * okButton.setTooltipText(i18n.get(this, "okButton", i18n.TOOLTOP, "Ok"));<br>
 * </code>
 * <P>
 * Again there is a similar version of this method that takes a
 * <code>Class</code> instead of an object so it can be used in
 * static contexts.
 * <P>
 * Note that in both examples given, the <code>field</code>
 * parameter to the <code>get(...)</code> methods are the name of
 * the variable that holds the string returned. For the these methods,
 * this is a convention rather than a requirement. However, it
 * suggested that you maintain this convention because doing so makes
 * it clearer what is going on and because it is most similar to how
 * the calls are made in the reflective case below.
 * <P>
 * Note also that there are several additional <code>get(...)</code>
 * methods that can be used to make getting paramaterized (as if by
 * <code>MessageFormat</code>) Strings easier.
 * 
 * <LI><I><B>Swing Usage: </B> </I> <BR>
 * If you are setting the text fields on Swing objects, the
 * <code>set(...)</code> methods of this class can help you. Here is
 * an example:
 * <P>
 * <code>
 * I18n i18n = Environment.getI18n();<br>
 * JButton myButton = new JButton();<br>
 * I18n.set(this, "myButton", myButton);<br>
 * </code>
 * <P>
 * This will get the string information (both tool tip and text) from
 * the appropriate ResourceBundle and set the button's slots with it.
 * <P>
 * Note that in this example the <code>field</code> parameter to the
 * <code>set(...)</code> method is the name of the field that holds
 * the object being setup. For these methods this is a convention
 * rather than a requirement. However, it suggested that you maintain
 * this convention because doing so makes it clearer what is going on
 * and because it is most similar to how the calls are made in the
 * reflective case below.
 * <LI><I><B>Reflective Calls: </B> </I> <BR>
 * For the typical uses of this class, there are two calls that can
 * make things very easy, by doing a bunch of the work for you. In
 * most implementations if this interface, they will accomplish this
 * via reflection under-the-hood. Here are some examples:
 * <P>
 * <code>
 * public class MyClass {<BR>
 * &nbsp;&nbsp;JButton myButton = new JButton();<BR>
 * &nbsp;&nbsp;public MyClass() {<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;Environment.getI18n().set(this, "myButton");<BR>
 * &nbsp;&nbsp;}<BR>
 * }<BR>
 * </code>
 * <P>
 * This code will setup the properties of the object held by the
 * <code>myButton</code> variable, without the programmer having to
 * do much at all. Here is an example of the other reflective method:
 * <P>
 * <code>
 * public class MyClass {<BR>
 * &nbsp;&nbsp;JButton myButton = new JButton();<BR>
 * &nbsp;&nbsp;JLabel myLabel = new JLabel();
 * &nbsp;&nbsp;public MyClass() {<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;Environment.getI18n().fill(this);<BR>
 * &nbsp;&nbsp;}<BR>
 * }<BR>
 * </code>
 * <P>
 * Here we can see that a single call is filling in all of the text
 * for all of the relevant objects, in this case both
 * <code>myButton</code> and <code>myLabel</code>.
 * </UL>
 * <P>
 * <SMALL>In case you were wondering, the oft used I18n abbreviation
 * comes from the 18 characters between the 'I' and the 'n' in the
 * word Internationalization. </SMALL>
 * 
 * @see com.bbn.openmap.Environment
 * @see com.bbn.openmap.BasicI18n
 */
public interface I18n {

    //Types:
    ////////

    /**
     * Primary type for a given field (default if types aren't
     * applicable).
     */
    public final int TEXT = 1;
    /** Title for components where that is appropriate. */
    public final int TITLE = 2;
    /** Tooltip for a given field. */
    public final int TOOLTIP = 3;
    /** Mnemonic for a given field. */
    public final int MNEMONIC = 4;

    /**
     * Get the string associated with the given object/field (Defaults
     * to TEXT for the type).
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the String or the Swing object that
     * uses the String (like a button or a label).
     * @param defaultString what to use if the resource can't be found.
     */
    public String get(Object requestor, String field, String defaultString);

    /**
     * Get the string associated with the given object/field/type.
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the Swing object that uses the String
     * (like a button or a label).
     * @param type which specific slot in the Swing object the string
     * pertains to.  One of TEXT, TOOLTIP or MNEMONIC.
     * @param defaultString what to use if the resource can't be found.
     */
    public String get(Object requestor, String field, int type,
                      String defaultString);

    /**
     * Get the string associated with the given class/field (Defaults
     * to TEXT for the type).
     * 
     * @param requestor the class of the object containing the code
     *        requesting the String (typically <code>Foo.class</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the String or the Swing object that
     * uses the String (like a button or a label).
     * @param defaultString what to use if the resource can't be found.
     */
    public String get(Class requestor, String field, String defaultString);

    /**
     * Get the string associated with the given class/field/type.
     * 
     * @param requestor the classof the object containing the code
     *        requesting the String (typically <code>Foo.class</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the Swing object that uses the String
     * (like a button or a label).
     * @param type which specific slot in the Swing object the string
     * pertains to.  One of TEXT, TOOLTIP or MNEMONIC.
     * @param defaultString what to use if the resource can't be found.
     */
    public String get(Class requestor, String field, int type,
                      String defaultString);

    //Methods making it easier to use MessageFormat:
    ////////////////////////////////////////////////

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Object requestor, String field, String defaultString,
                      Object param1);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Object requestor, String field, int type,
                      String defaultString, Object param1);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Class requestor, String field, String defaultString,
                      Object param1);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Class requestor, String field, int type,
                      String defaultString, Object param1);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Object requestor, String field, String defaultString,
                      Object param1, Object param2);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Object requestor, String field, int type,
                      String defaultString, Object param1, Object param2);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Class requestor, String field, String defaultString,
                      Object param1, Object param2);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Class requestor, String field, int type,
                      String defaultString, Object param1, Object param2);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Object requestor, String field, String defaultString,
                      Object[] params);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Object requestor, String field, int type,
                      String defaultString, Object[] params);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Class requestor, String field, String defaultString,
                      Object[] params);

    /**
     * Method to get a parameterized String, as if by MessageFormat.
     * 
     * @see java.text.MessageFormat
     */
    public String get(Class requestor, String field, int type,
                      String defaultString, Object[] params);

    //Methods fill setting the textual properties of common Swing
    // components:
    /////////////////////////////////////////////////////////////////////////

    /**
     * Set the textual properties from values in the appropriate
     * ResourceBundle.
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the <code>comp</code> parameter.
     * @param comp the component whose properties are being set.
     * @throws MissingResourceException if the data can't be found.
     */
    public void set(Object requestor, String field, JLabel comp);

    /**
     * Set the textual properties from values in the appropriate
     * ResourceBundle.
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the <code>comp</code> parameter.
     * @param comp the component whose properties are being set.
     * @throws MissingResourceException if the data can't be found.
     */
    public void set(Object requestor, String field, JButton comp);

    /**
     * Set the textual properties from values in the appropriate
     * ResourceBundle.
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the <code>comp</code> parameter.
     * @param comp the component whose properties are being set.
     * @throws MissingResourceException if the data can't be found.
     */
    public void set(Object requestor, String field, JMenu comp);

    /**
     * Set the textual properties from values in the appropriate
     * ResourceBundle.
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the <code>comp</code> parameter.
     * @param comp the component whose properties are being set.
     * @throws MissingResourceException if the data can't be found.
     */
    public void set(Object requestor, String field, JMenuItem comp);

    /**
     * Set the textual properties from values in the appropriate
     * ResourceBundle.
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the <code>comp</code> parameter.
     * @param comp the component whose properties are being set.
     * @throws MissingResourceException if the data can't be found.
     */
    public void set(Object requestor, String field, JDialog comp);

    /**
     * Set the textual properties from values in the appropriate
     * ResourceBundle.
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the <code>comp</code> parameter.
     * @param comp the component whose properties are being set.
     * @throws MissingResourceException if the data can't be found.
     */
    public void set(Object requestor, String field, JFrame comp);

    /**
     * Set the textual properties from values in the appropriate
     * ResourceBundle.
     * <P>
     * <B>Note: This method just looks for a TitledBorder on the
     * component. </B>
     * 
     * @param requestor object containing the code requesting the
     *        String (typically <code>this</code).
     * @param field the field the String belongs to.  Typically this will
     * be the variable name referring to the <code>comp</code> parameter.
     * @param comp the component whose properties are being set.
     * @throws MissingResourceException if the data can't be found.
     */
    public void set(Object requestor, String field, JComponent comp);

    //Methods for filling in strings using reflection:
    //////////////////////////////////////////////////

    /**
     * Set the textual properties on a Swing component that is a
     * member of a given class, from values in the ResourceBundle
     * associated with that class. Note that the field must contain an
     * object of a known type (see the other set(...) methods of this
     * class).
     * <P>
     * The setting of the values of this field will be accomplished by
     * calling the appropriate set(...) method on this class.
     * 
     * @param requestor object containing the code requesting the
     *        Component setup (typically <code>this</code).
     * @param field the variable name of the component being setup.
     * @throws MissingResourceException if the data can't be found.
     */
    public void set(Object requestor, String field);

    /**
     * Fill in all of the fields of the given object that are of known
     * types (see the set(...) methods of this class) with values
     * obtained from the appropriate ResourceBundle.
     * <P>
     * The setting of the values of this field will be accomplished by
     * calling the appropriate set(...) method on this class.
     */
    public void fill(Object requestor);

}