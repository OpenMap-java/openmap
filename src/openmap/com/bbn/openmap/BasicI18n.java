// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/BasicI18n.java,v $
// $RCSfile: BasicI18n.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/08 23:56:30 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.lang.reflect.Field;

import com.bbn.openmap.util.Debug;

/**
 * Basic implementation of internationalization support.
 * <P>
 * This class backs the I18n interface by I18n.property files that it
 * expects to find in each package.
 */
public class BasicI18n implements I18n {

    /**
     * Debug string, 'i18n'
     */
    public static final String DEBUG = "i18n";

    /**
     * All properties files containing string should be contained in a
     * file called I18N.properties.  This string defines the I18N.
     */
    public static final String ResourceFileNamePrefix = "I18N";

    private Locale loc;

    //Constructors:
    ///////////////

    /**
     * Create a BasicI18n object from the default locale.
     **/
    public BasicI18n(){
	this(Locale.getDefault());
    }

    /**
     * Create a BasicI18n object with given locale.
     **/
    public BasicI18n(Locale loc){
	this.loc = loc;
    }

    public String get(Object requestor,
		      String field, String defaultString) {
	return get(requestor, field, TEXT, defaultString);
    }

    public String get(Object requestor,
		      String field, int type,
		      String defaultString) {
	return get(requestor.getClass(), field, type, defaultString);
    }

    public String get(Class requestor,
		      String field, String defaultString) {
	return get(requestor, field, TEXT, defaultString);
    }

    public String get(Class requestor,
		      String field, int type,
		      String defaultString) {
	String ret = getInternal(requestor, field, type);
	if (ret == null) {
	    return defaultString;
	}
	return ret;
    }

    //Methods making it easier to use MessageFormat:
    ////////////////////////////////////////////////

    public String get(Object requestor, String field,
		      String defaultString, Object param1) {
	return get(requestor.getClass(), field, TEXT, defaultString, 
		   new Object[] { param1});
    }

    public String get(Object requestor, String field, int type, 
		      String defaultString, Object param1) {
	return get(requestor.getClass(), field, type, defaultString, 
		   new Object[] {param1});
    }

    public String get(Class requestor, String field,
		      String defaultString, Object param1) {
	return get(requestor, field, TEXT, defaultString,
		   new Object[] {param1});
    }

    public String get(Class requestor, String field, int type, 
		      String defaultString, Object param1) {
	return get(requestor, field, type, defaultString,
		   new Object[] {param1});
    }

    public String get(Object requestor, String field,
		      String defaultString, Object param1, Object param2) {
	return get(requestor.getClass(), field, TEXT, defaultString,
		   new Object[] {param1, param2});
    }

    public String get(Object requestor, String field, int type, 
		      String defaultString, Object param1, Object param2) {
	return get(requestor.getClass(), field, type, defaultString,
		   new Object[] {param1, param2});
    }

    public String get(Class requestor, String field,
		      String defaultString, Object param1, Object param2) {
	return get(requestor, field, TEXT, defaultString,
		   new Object[] {param1, param2});
    }

    public String get(Class requestor, String field, int type, 
		      String defaultString, Object param1, Object param2) {
	return get(requestor, field, type, defaultString,
		   new Object[] {param1, param2});
    }

    public String get(Object requestor, String field,
		      String defaultString, Object[] params) {
	return get(requestor.getClass(), field, TEXT, defaultString,
		   params);
    }

    public String get(Object requestor, String field, int type, 
		      String defaultString, Object[] params) {
	return get(requestor.getClass(), field, type, defaultString,
		   params);
    }

    public String get(Class requestor, String field,
		      String defaultString, Object[] params) {
	return get(requestor, field, TEXT, defaultString, params);
    }

    public String get(Class requestor, String field, int type, 
		      String defaultString, Object[] params) {
	return MessageFormat.format(get(requestor, field, type, defaultString),
				    params);
    }

    //Methods fill setting the textual properties of common Swing components:
    /////////////////////////////////////////////////////////////////////////

    public void set(Object requestor, String field, JLabel comp) {
	set(requestor, field, (JComponent)comp);
	comp.setText(getTEXT(requestor, field, comp.getText()));
	comp.setDisplayedMnemonic
	    (getMNEMONIC(requestor, field, 
			 comp.getDisplayedMnemonic(), false));
    }

    public void set(Object requestor, String field, JButton comp) {
	set(requestor, field, (JComponent)comp);
	comp.setText(getTEXT(requestor, field, comp.getText()));
	comp.setMnemonic
	    (getMNEMONIC(requestor, field, 
			 comp.getMnemonic(), false));
    }

    public void set(Object requestor, String field, JMenu comp) {
	set(requestor, field, (JComponent)comp);
	comp.setText(getTEXT(requestor, field, comp.getText()));
	comp.setMnemonic
	    (getMNEMONIC(requestor, field, 
			 comp.getMnemonic(), true));
    }

    public void set(Object requestor, String field, JMenuItem comp) {
	set(requestor, field, (JComponent)comp);
	comp.setText(getTEXT(requestor, field, comp.getText()));
	comp.setMnemonic
	    (getMNEMONIC(requestor, field, 
			 comp.getMnemonic(), true));
    }

    public void set(Object requestor, String field, JDialog comp) {
	comp.setTitle(getTITLE(requestor, field, comp.getTitle()));
    }

    public void set(Object requestor, String field, JFrame comp) {
	comp.setTitle(getTITLE(requestor, field, comp.getTitle()));
    }

    public void set(Object requestor, String field, JComponent comp) {
	setTOOLTIP(requestor, field, comp);	
	Border b = comp.getBorder();
	if (b instanceof TitledBorder) {
	    TitledBorder t = (TitledBorder)b;
	    t.setTitle(getTITLE(requestor, field, t.getTitle()));
	}
    }

    //Methods for filling in strings using reflection:
    //////////////////////////////////////////////////

    public void set(Object requestor, String field) {
	Class c = requestor.getClass();
	Field f = null;
	try {
	    f = c.getField(field);
	} catch (NoSuchFieldException e) {
	    //We'll try again below.
	} catch (SecurityException e) {
	    RuntimeException r = new MissingResourceException
		("SecurityException trying to reflect on field field",
		 c.getName(), field);
	    r.initCause(e);
	    throw r;
	}
	if (f == null) {
	    try {
		f = c.getDeclaredField(field);
	    } catch (NoSuchFieldException e) {
		RuntimeException r =  new MissingResourceException
		    ("Can't find field via reflection",
		     c.getName(), field);
		r.initCause(e);
		throw r;
	    } catch (SecurityException e) {
		RuntimeException r = new MissingResourceException
		("SecurityException trying to reflect on field field",
		 c.getName(), field);
		r.initCause(e);
		throw r;
	    }	    
	}
	//Try to set it accessible:
	try {
	    f.setAccessible(true);
	} catch (SecurityException e) {
	    Debug.message(DEBUG, "Coudn't set field " + field + " accessible");
	}
	//Ok, now try to get the data:
	Class type = f.getType();
	Object fd = null;
	try {
	    fd = f.get(requestor);
	} catch (IllegalArgumentException e) {
	    RuntimeException r = new MissingResourceException
		("Couldn't get field", c.getName(), field);
	    r.initCause(e);
	    throw r;
	} catch (IllegalAccessException e) {
	    RuntimeException r = new MissingResourceException
		("Couldn't access field", c.getName(), field);
	    r.initCause(e);
	    throw r;	    
	}
	//Now do the calls:
	if (JLabel.class.isInstance(type)) {
	    set(requestor, field, (JLabel)fd);
	} else if (JButton.class.isInstance(type)) {
	    set(requestor, field, (JButton)fd);
	} else if (JMenu.class.isInstance(type)) {
	    set(requestor, field, (JMenu)fd);
	} else if (JMenuItem.class.isInstance(type)) {
	    set(requestor, field, (JMenuItem)fd);
	} else if (JDialog.class.isInstance(type)) {
	    set(requestor, field, (JDialog)fd);
	} else if (JFrame.class.isInstance(type)) {
	    set(requestor, field, (JFrame)fd);
	} else if (JComponent.class.isInstance(type)) {
	    set(requestor, field, (JComponent)fd);
	} else {
	    Debug.message(DEBUG, "Couldn't assign data for unknown type: " +
			  type);
	}
    }

    public void fill(Object requestor) {

    }

    ////
    //// Implemenation Methods:
    ///////////////////////////
    ///////////////////////////

    /**
     * Set a tooltip on the given component if it has one.
     */
    protected void setTOOLTIP(Object requestor, String field,
				JComponent comp) {
	String tooltip = get(requestor, field, TOOLTIP, comp.getToolTipText());
	if (tooltip != null) {
	    comp.setToolTipText(tooltip);
	} else {
	    Debug.message(DEBUG, "No tooltip for: " + 
			  getKeyRef(requestor, field));
	}
    }

    /**
     * Get text for the given component.
     */
    protected String getTEXT(Object requestor, String field, 
			     String defaultString) {
	String text = get(requestor, field, TEXT, defaultString);
	if (text == null) {
	    throw new MissingResourceException("No TEXT resource",
					       requestor.getClass().getName(),
					       field);
	} 
	return text;
    }

    /**
     * Get title for the given component.
     */
    protected String getTITLE(Object requestor, String field, 
			      String defaultString) {
	String title = get(requestor, field, TITLE, defaultString);
	if (title == null) {
	    throw new MissingResourceException("No TITLE resource",
					       requestor.getClass().getName(),
					       field);
	} 
	return title;
    }

    /**
     * Get text for the given component.
     */
    protected int getMNEMONIC(Object requestor, String field, 
			      int defaultInt, boolean verbose) {
	String mn = get(requestor, field, MNEMONIC, 
			Character.toString((char)defaultInt));
	if (mn == null) {
	    if (verbose) {
		Debug.message(DEBUG, "No MNEMONIC resource for " +
			      getKeyRef(requestor, field));
	    } 
	    return defaultInt;
	}
	//Now parse this string into something useful:
	//For now, don't deal with virtual keys, though that is an obvious
	//extension:
	return Character.getNumericValue(mn.charAt(0));
    }

    /**
     * Obtain a String from the underlying data for the given class/field/type.
     * @return null if the data can't be found -- 
     * defaulting happens above here.
     */
    protected String getInternal(Class requestor, String field, int type) {
	ResourceBundle bundle = null;

	String bString = 
	    requestor.getPackage().getName() + "." + 
	    ResourceFileNamePrefix;

	try {
	    bundle = ResourceBundle.getBundle(bString, loc);
	} catch (MissingResourceException e) {
	    Debug.message(DEBUG, "Could not locate resource: " +
			  bString.replace('.','/') + ".properties");
	    return null;
	}
	String key = shortClassName(requestor) + "." + field;
	switch (type) {
	case TEXT:
	    //Do nothing.
	    break;
	case TITLE:
	    key += ".title";
	    break;
	case TOOLTIP:
	    key += ".tooltip";
	    break;
	case MNEMONIC:
	    key += ".mnemonic";
	    break;
	}
	try {
	    return bundle.getString(key);
	} catch (MissingResourceException e) {
	    Debug.message(DEBUG, "Could not locate string in resource: " +
			  requestor.getPackage().getName().replace('.','/') + 
			  ".properties for key: " + key);
	    return null;
	}
    }

    private static String shortClassName(Class c) {
	String name = c.getName();
	return name.substring(name.lastIndexOf(".")+1);
    }

    private static String getKeyRef(Object requestor, String field) {
	return requestor.getClass().getName() + "." + field;
    }
}
