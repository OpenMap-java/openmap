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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/ComponentFactory.java,v $
// $RCSfile: ComponentFactory.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.util;

import java.lang.reflect.Constructor; 
import java.lang.reflect.InvocationTargetException; 
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.ProgressListener;
import com.bbn.openmap.event.ProgressSupport;

/**
 * The OpenMap ComponentFactory is a class that can construct objects
 * from class names, with the added capability of passing the new
 * object a Properties object to initialize itself.  The new object
 * may also receive a property prefix to use to scope its properties
 * from the Properties object.  It is sensitive to the OpenMap
 * paradigm of marker names in a list: That a list of objects can be
 * defined as a space separated names (marker names) within a String.
 * Those marker names can serve as a prefix for other properties
 * within a Properties object, as well as the prefix for a '.class'
 * property to define the class name for the new object.
 */
public class ComponentFactory {

    /**
     * The property to use for the class name of new objects -
     * ".class".  Expects that a prefix will be prepended to it. 
     */
    public static final String ClassNameProperty = ".class";

    /**
     * Given a Vector of marker name Strings, and a Properties object,
     * look in the Properties object for the markerName.class property
     * to get a class name to create each object.  Then, if the new
     * objects are PropertyConsumers, use the marker name as a
     * property prefix to get properties for that object out of the
     * Properties.
     *
     * @param markerNames String of space separated marker names.
     * @param properties Properties object containing the details.
     * @return Vector containing the new Objects.
     */
    public static Vector create(Vector markerNames, Properties properties) {
	return create(markerNames, properties, null);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object,
     * look in the Properties object for the markerName.class property
     * to get a class name to create each object.  Then, if the new
     * objects are PropertyConsumers, use the marker name as a
     * property prefix to get properties for that object out of the
     * Properties.
     *
     * @param markerNames String of space separated marker names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide
     * progress updates to.  It's OK if this is null to not have
     * progress events sent.
     * @return Vector containing the new Objects.  
     */
    public static Vector create(Vector markerNames, Properties properties,
				ProgressSupport progressSupport) {

	int size = markerNames.size();
	Vector vector = new Vector(size);

	if (progressSupport != null) {
	    progressSupport.fireUpdate(ProgressEvent.UPDATE,
				       "Creating Components", 100, 0);
	}

	for (int i = 0; i < size; i++) {
	    String componentName = (String) markerNames.elementAt(i);
	    String classProperty = componentName + ClassNameProperty;
	    String className = properties.getProperty(classProperty);

	    if (className == null) {
		Debug.error("ComponentFactory.create: Failed to locate property \"" + componentName + "\" with class \"" + classProperty + "\"\n  Skipping component \"" + componentName + "\"");
		continue;
	    }
 
	    if (progressSupport != null) {
		progressSupport.fireUpdate(ProgressEvent.UPDATE,
					   "Creating Components", size, i);
	    }

	    Object component = create(className, componentName, properties);

	    if (component != null) {
		vector.add(component);

		if (Debug.debugging("componentfactory")) {
		    Debug.output("ComponentFactory: [" + i + 
				 "] created - " + className);
		}
	    } else if (Debug.debugging("componentfactory")) {
		Debug.output("ComponentFactory: [" + i +
			     "] NOT created - " + className);
	    }
	}

	if (progressSupport != null) {
	    progressSupport.fireUpdate(ProgressEvent.UPDATE,
				       "Configuring...", size, size);
	}
	return vector;
    }

    /**
     * Create a single object.
     *
     * @param className Class name to instantiate.
     * @param properties Properties to use to initalize the object.
     */
    public static Object create(String className, Properties properties) {
	return create(className, null, properties);
    }

    /**
     * Create a single object.  If you want it to complain about
     * classes it can't find, then set the 'basic' debug flag.
     *
     * @param className Class name to instantiate.
     * @param prefix Properties prefix to use by the object to scope
     * its properties.
     * @param properties Properties to use to initalize the object.  
     */
    public static Object create(String className,
				String prefix, 
				Properties properties) {

	return create(className, (Object[])null, prefix, properties);

    }

    /**
     * Create a single object.  If you want it to complain about
     * classes it can't find, then set the 'basic' debug flag.
     *
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in
     * the constructor of the component.
     * @param prefix Properties prefix to use by the object to scope
     * its properties.
     * @param properties Properties to use to initalize the object.  
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className,
				Object[] constructorArgs,
				String prefix, 
				Properties properties) {

	String errorMessage = null;
	boolean DEBUG = false;
	try {
	    
	    if (Debug.debugging("componentfactory")) {
		DEBUG = true;
		Debug.output("ComponentFactory.create: " + className);
	    }

	    Class newObjClass = Class.forName(className);
	    if (DEBUG) Debug.output(" - got class for " + className);

	    Class[] argClasses = null;
	    if (constructorArgs != null && constructorArgs.length > 0) {
		argClasses = new Class[constructorArgs.length];
		for (int i = 0; i < argClasses.length; i++) {
		    argClasses[i] = constructorArgs[i].getClass();
		}
	    } else {
		// If empty, make null
		constructorArgs = null;
	    }

	    if (DEBUG) {
		Debug.output(" - created class arguments " + constructorArgs);
	    }

	    Constructor constructor = null;
	    Object obj = null;

	    try {
		constructor = newObjClass.getConstructor(argClasses);

		if (DEBUG) Debug.output(" - got constructor");

		// Create component
		obj = constructor.newInstance(constructorArgs);
		if (DEBUG) Debug.output(" - got object");

	    } catch (NoSuchMethodException nsmei) {
		// The argClasses may have subclasses of what the desired 
		// constructor needs, so we need to check explicitly.
		obj = createWithSubclassConstructorArgs(newObjClass, argClasses);
		if (DEBUG && obj != null) Debug.output(" - got object on try #2");
	    }

	    if (obj instanceof PropertyConsumer && properties != null) {
		if (Debug.debugging("componentfactory")) {
		    Debug.output("  setting properties with prefix \"" + prefix + "\"");
		}
		((PropertyConsumer)obj).setProperties(prefix, properties);
		if (DEBUG) Debug.output(" - set properties");
	    }
	    return obj;
	    
	} catch (NoSuchMethodException nsme) {
	    errorMessage = "NoSuchMethodException: " + nsme.getMessage();
	} catch (InstantiationException ie) {
	    errorMessage = "InstantiationException: " + ie.getMessage();
	} catch (IllegalAccessException iae) {
	    errorMessage = "IllegalAccessException: " + iae.getMessage();
	} catch (IllegalArgumentException iae2) {
	    errorMessage = "IllegalArgumentException: " + iae2.getMessage();
	} catch (InvocationTargetException ite) {
	    errorMessage = "InvocationTargetException: " + ite.getMessage();
	} catch (ClassNotFoundException cnfe) {
	    errorMessage = "ClassNotFoundException: " + cnfe.getMessage();
	}

	if (Debug.debugging("basic")) {
	    Debug.error("ComponentFactory.create: Failed to create \"" + className + 
			(prefix != null?"\" using component marker name \"" + prefix + "\"":"") + 
			" - error message: " + errorMessage);
	}

	return null;
    }

    protected static Object createWithSubclassConstructorArgs(Class newObjClass, 
							      Class[] argClasses) 
	throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

	boolean DEBUG = Debug.debugging("componentfactory");

	int numArgClasses = 0;

	if (argClasses != null) {
	    numArgClasses = argClasses.length;
	}

	Constructor[] constructors = newObjClass.getConstructors();
	int numConstructors = constructors.length;
	for (int i = 0; i < numConstructors; i++) {
	    Constructor constructor = constructors[i];

	    Class[] arguments = constructor.getParameterTypes();
	    int numArgs = arguments.length;

	    // First, check the number of arguments for a match
	    if (numArgs != numArgClasses) {
		if (DEBUG) {
		    Debug.output(" - constructor " + i + " with " + numArgs +
				 " arguments not a match");
		}

		continue;  // Nope, not it.
	    }

	    // OK, empty constructor desired, punch...
	    if (numArgs == 0) {
		if (DEBUG) {
		    Debug.output(" - constructor " + i + 
				 " with no arguments is a match");
		}
		return constructor;
	    }

	    // Check to see if the argument classes of the Constructor are 
	    // assignable to the desired argClasses being sought.
	    boolean good = false;
	    for (int j = 0; j < numArgs; j++) {
		if (arguments[j] == argClasses[j]) {
		    if (DEBUG) {
			Debug.output(" - direct arg class match, arg " + j);
		    }
		    good = true; // Maintain true...
		} else if (arguments[j].isAssignableFrom(argClasses[j])) {

		    // Doesn't work quite yet.  May have to check for
		    //  super-super class,etc, and we still get an
		    //  IllegalArgumentException due to argument type
		    //  mismatch.

		    argClasses[j] = argClasses[j].getSuperclass();
		    if (DEBUG) {
			Debug.output(" - superclass arg class match, arg " + j + 
				     " reassigning to " + argClasses[j].toString());
		    }
		    good = true; // Maintain true...
		} else {
		    if (DEBUG) {
			Debug.output(" - arg class mismatch on arg " + j + ", bailing");
		    }
		    good = false; // Punch with false
		    break;
		}
	    }

	    if (good) {
		if (DEBUG) {
		    Debug.debugging(" - creating object");
		}
		Object obj = constructor.newInstance(argClasses);
		if (DEBUG) {
		    Debug.debugging(" - created object");
		}
		return obj;
	    }
	}

	return null;
    }


}
